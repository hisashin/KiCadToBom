package dev.hisa.kicad.market.checker;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import dev.hisa.kicad.market.MarketResult;

public abstract class AbstractMarketChecker {

	public abstract MarketResult update(WebDriver driver, String url) throws NoUnitPriceFoundException, NoStockFoundException;

	@SuppressWarnings("serial")
	public static class NoStockFoundException extends Exception {}
	@SuppressWarnings("serial")
	public static class NoUnitPriceFoundException extends Exception {}
	@SuppressWarnings("serial")
	public static class NotEnglishException extends Exception {}
	@SuppressWarnings("serial")
	public static class NotUSDException extends Exception {}

	protected static WebElement waitClickable(WebDriver driver, By by, long waitTimeInMillis) {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(waitTimeInMillis));
		return wait.until(ExpectedConditions.elementToBeClickable(by));
	}
	protected static WebElement waitVisible(WebDriver driver, By by, long waitTimeInMillis) {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(waitTimeInMillis));
		return wait.until(ExpectedConditions.visibilityOfElementLocated(by));
	}
	protected static boolean waitInvisible(WebDriver driver, By by, long waitTimeInMillis) {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(waitTimeInMillis));
		return wait.until(ExpectedConditions.invisibilityOfElementLocated(by));
	}
}
