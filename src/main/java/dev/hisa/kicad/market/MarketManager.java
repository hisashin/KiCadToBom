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
import dev.hisa.kicad.market.checker.AbstractMarketChecker;
import dev.hisa.kicad.market.checker.MarketCheckerDigikey.NoStockFoundException;
import dev.hisa.kicad.market.checker.MarketCheckerDigikey.NoUnitPriceFoundException;
import dev.hisa.kicad.market.checker.MarketCheckerFactory;
import dev.hisa.kicad.market.checker.MarketCheckerFactory.NoCheckerFoundException;

public class MarketManager {

	static String SELENIUM_SERVER = "http://localhost:4444/wd/hub";
	static Path jsonPath = Paths.get("src/main/resources/market.json");
	static boolean updateAll = false;
	
	public static void main(String[] args) throws JsonProcessingException, IOException, NoCheckerFoundException, NoUnitPriceFoundException, NoStockFoundException {
		String url = "https://www.digikey.jp/ja/products/detail/texas-instruments/LMP7717MF-NOPB/1658219";
		WebDriver driver = getDriver();
		MarketManager manager = new MarketManager();
		manager.add(driver, url);
		manager.save();
	}
	
	static MarketManager instance = null;
	public static MarketManager getInstance() {
		if(instance == null)instance = new MarketManager();
		return instance;
	}
	
	Map<String, MarketResult> map = new TreeMap<String, MarketResult>();
	
	private MarketManager() {
		load();
	}
	void load() {
		try {
			String json = new String(Files.readAllBytes(jsonPath));
			System.out.println(json);
			ObjectMapper mapper = new ObjectMapper();
			MarketResultContainer container = mapper.readValue(json, MarketResultContainer.class);
			System.out.println("container.size()=" + container.size());
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
	public void add(WebDriver driver, String url) throws NoCheckerFoundException, NoUnitPriceFoundException, NoStockFoundException {
		map.put(url, update(driver, url));
	}
	public void save() throws JsonProcessingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		Files.write(jsonPath, mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(map.values()));
		System.out.println("Saved as " + jsonPath.toAbsolutePath().toString());
	}
	
	@SuppressWarnings("serial")
	static class MarketResultContainer extends ArrayList<MarketResult> {
	}
	static WebDriver getDriver() {
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
		return driver;
	}

	static long updateIntervalInMillis = 7 * 24 * 3600 * 1000; // week
	public void update() throws NoCheckerFoundException, NoUnitPriceFoundException, NoStockFoundException {
		long limit = System.currentTimeMillis() - updateIntervalInMillis;
		WebDriver driver = getDriver();
		for(MarketResult result : map.values()) {
			if(!updateAll && limit < result.updateTime.getTime())continue;
			map.put(result.url, update(driver, result.url));
		}
	}
	
	static MarketResult update(WebDriver driver, String url) throws NoCheckerFoundException, NoUnitPriceFoundException, NoStockFoundException {
		AbstractMarketChecker checker = MarketCheckerFactory.getChecker(url);
		return checker.update(driver, url);
	}
}
