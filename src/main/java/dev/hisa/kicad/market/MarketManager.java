package dev.hisa.kicad.market;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.hisa.kicad.box.PartsBox;
import dev.hisa.kicad.box.PartsBox.DuplicatePartInBox;
import dev.hisa.kicad.box.PartsBoxShingo;
import dev.hisa.kicad.market.checker.AbstractMarketChecker;
import dev.hisa.kicad.market.checker.AbstractMarketChecker.NoStockFoundException;
import dev.hisa.kicad.market.checker.AbstractMarketChecker.NoUnitPriceFoundException;
import dev.hisa.kicad.market.checker.MarketCheckerFactory;
import dev.hisa.kicad.market.checker.MarketCheckerFactory.NoCheckerFoundException;

public class MarketManager {

	static String SELENIUM_SERVER = "http://localhost:4444/wd/hub";
	static Path jsonPath = Paths.get("src/main/resources/market.json");
	static boolean updateAll = false;
	
	public static void main(String[] args) throws JsonProcessingException, IOException, NoCheckerFoundException, NoUnitPriceFoundException, NoStockFoundException, DuplicatePartInBox {
		PartsBox box = PartsBoxShingo.getInstance();
		List<String>list = box.getUrlList();
		MarketManager manager = MarketManager.getInstance();
		String url;
		for(int i = 0 ; i < list.size() ; i++) {
			System.out.println("MarketManager.main : i=" + i);
			url = list.get(i);
			manager.add(url, false);
		}
		manager.save();
	}
	
	static MarketManager instance = null;
	public static MarketManager getInstance() {
		if(instance == null)instance = new MarketManager();
		return instance;
	}
	
	Map<String, MarketResult> map = new TreeMap<String, MarketResult>();
	WebDriver _driver = null;
	
	private MarketManager() {
		load();
	}
	void load() {
		try {
			String json = new String(Files.readAllBytes(jsonPath));
			//System.out.println(json);
			ObjectMapper mapper = new ObjectMapper();
			MarketResultContainer container = mapper.readValue(json, MarketResultContainer.class);
			//System.out.println("container.size()=" + container.size());
			for(MarketResult obj : container)
				map.put(obj.url, obj);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	static String readJson(String jsonPath) {
		InputStream is = PartsBox.class.getResourceAsStream(jsonPath);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		return br.lines().collect(Collectors.joining(System.lineSeparator()));
	}
	public MarketResult add(String url, boolean forceUpdate) throws NoCheckerFoundException, NoUnitPriceFoundException, NoStockFoundException, JsonProcessingException, IOException {
		MarketResult exist = map.get(url);
		if(!forceUpdate && exist != null && !exist.isTooOld()) {
			System.out.println("MarketManager.add : Use cache for " + exist.label + " : " + url);
			return exist;
		}
		MarketResult result = update(url);
		if(result != null) {
			map.put(url, result);
			save();
		}
		return result;
	}
	public void save() throws JsonProcessingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		Files.write(jsonPath, mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(map.values()));
		System.out.println("Saved as " + jsonPath.toAbsolutePath().toString());
	}
	
	@SuppressWarnings("serial")
	static class MarketResultContainer extends ArrayList<MarketResult> {
	}
	WebDriver driver = null;
	public WebDriver getDriver() {
		if(driver == null) {
			ChromeOptions chromeOptions = new ChromeOptions();
			chromeOptions.setPageLoadStrategy(PageLoadStrategy.NORMAL);
			chromeOptions.addArguments(new String[] {"--no-sandbox", "--disable-dev-shm-usage", "--disable-gpu"});
			WebDriver driver = null;
			try {
				//driver = new ChromeDriver(chromeOptions);
				driver = new RemoteWebDriver(new URL(SELENIUM_SERVER), chromeOptions);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			this.driver = driver;
		}
		return driver;
	}

	public void update() throws NoCheckerFoundException, NoUnitPriceFoundException, NoStockFoundException {
		for(MarketResult result : map.values()) {
			if(!updateAll && !result.isTooOld())continue;
			result = update(result.url);
			if(result == null)continue;
			map.put(result.url, result);
		}
	}
	
	MarketResult update(String url) throws NoUnitPriceFoundException, NoStockFoundException {
		try {
			AbstractMarketChecker checker = MarketCheckerFactory.getChecker(url);
			return checker.update(getDriver(), url);
		} catch (NoCheckerFoundException e) {
			System.out.println("MarketManager.update : skip " + url);
			return null;
		}
	}
}
