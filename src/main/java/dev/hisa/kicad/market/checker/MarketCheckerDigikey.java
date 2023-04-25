package dev.hisa.kicad.market.checker;

import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import dev.hisa.kicad.market.MarketResult;
import dev.hisa.kicad.market.MarketResult.Market;

public class MarketCheckerDigikey extends AbstractMarketChecker {

	public static void main(String[] args) throws NotEnglishException {
		String value = "6,780 In Stock";
		Matcher m;
		m = patternNonUS.matcher(value);
		if(m.find())
			throw new NotEnglishException();
	}
	@Override
	public MarketResult update(WebDriver driver, String url) throws NoStockFoundException, NoUnitPriceFoundException {
		System.out.println("MarketCheckerDigikey.update : " + url);
		driver.get(url.replace("/ja/", "/en/"));
		Integer stock = null;
		Double unitPricePer100 = null;
		try {
			stock = getStock(driver);
			unitPricePer100 = getUnitPricePer100(driver);
		} catch (NotEnglishException e) {
			changeLocale(driver);
			try {
				stock = getStock(driver);
			} catch (NotEnglishException e1) {
				e1.printStackTrace();
			}
			try {
				unitPricePer100 = getUnitPricePer100(driver);
			} catch (NotUSDException e1) {
				e1.printStackTrace();
			}
		} catch (NotUSDException e) {
			changeLocale(driver);
			try {
				stock = getStock(driver);
			} catch (NotEnglishException e1) {
				e1.printStackTrace();
			}
			try {
				unitPricePer100 = getUnitPricePer100(driver);
			} catch (NotUSDException e1) {
				e1.printStackTrace();
			}
		}
		WebElement elLabel = driver.findElement(By.tagName("h1"));
		WebElement elSupplier = driver.findElement(By.xpath("//a[@ref_page_event='Link to Supplier']"));
		WebElement elDatasheet = driver.findElement(By.xpath("//a[@data-testid='datasheet-download']"));
		String label = elLabel.getText();
		String supplier = elSupplier.getText();
		String datasheet = elDatasheet.getAttribute("href");
		System.out.println("label=" + label + ", supplier=" + supplier + ", datasheet=" + datasheet + ", stock=" + stock + ", unitPricePer100=" + unitPricePer100);
		MarketResult obj = new MarketResult();
		obj.market = Market.Digikey;
		obj.url = url;
		obj.stock = stock;
		obj.unitPriceFor100 = unitPricePer100;
		obj.label = label;
		obj.supplier = supplier;
		obj.datasheet = datasheet;
		obj.updateTime = new Date();
		return obj;
	}
	void changeLocale(WebDriver driver) {
		driver.findElement(By.xpath("//a[@id='header__open-settings']")).click();
		waitClickable(driver, By.xpath("//span[@data-code='en']"), 3000).click();
		waitClickable(driver, By.xpath("//span[@data-code='en']"), 3000).click();
		waitClickable(driver, By.xpath("//span[@data-code='USD']"), 3000).click();
		waitVisible(driver, By.xpath("//button[@class='dk-btn__primary']"), 5000).click();
		waitInvisible(driver, By.xpath("//button[@class='dk-btn__primary']"), 10000);
	}
	static Pattern patternNonUS = Pattern.compile("[^,\\.0-9a-zA-Z ]");
	int getStock(WebDriver driver) throws NotEnglishException, NoStockFoundException {
		WebElement elStock = waitVisible(driver, By.xpath("//div[@data-testid='price-and-procure-title']"), 5000);
		String value = elStock.getText();
		System.out.println("stock : '" + value + "'");
		Matcher m;
		m = patternNonUS.matcher(value);
		if(m.find())
			throw new NotEnglishException();
		value = value.replaceAll("[^0-9]", "");
		try {
			return Integer.parseInt(value);
		} catch(NumberFormatException ex) {
			throw new NoStockFoundException();
		}
	}
	double getUnitPricePer100(WebDriver driver) throws NoUnitPriceFoundException, NotUSDException {
		List<WebElement> prices = driver.findElements(By.xpath("//td[contains(@class,'MuiTableCell-body') and (contains(@class, 'jss251') or (contains(@class,'jss243') and contains(@class,'jss244')))]"));
		String lastValue = null, value;
		for(WebElement el : prices) {
			value = el.getText();
			if("100".equals(lastValue)) {
				System.out.println("unitPrice : '" + value + "'");
				if(!value.startsWith("$"))
					throw new NotUSDException();
				value = value.replaceAll("[$,]", "");
				return Double.parseDouble(value);
			}
			lastValue = value;
		}
		throw new NoUnitPriceFoundException();
	}
	@SuppressWarnings("serial")
	public static class NoStockFoundException extends Exception {}
	@SuppressWarnings("serial")
	public static class NoUnitPriceFoundException extends Exception {}
	@SuppressWarnings("serial")
	public static class NotEnglishException extends Exception {}
	@SuppressWarnings("serial")
	public static class NotUSDException extends Exception {}
	
	boolean isLogin(WebDriver driver) {
		WebElement elLogin = driver.findElement(By.className("my-account-line-1"));
		String text = elLogin.getText();
		System.out.println("text=" + text);
		return !(text != null && (text.contains("ログイン") || text.contains("Login")));
	}

}
