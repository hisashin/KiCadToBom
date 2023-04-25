package dev.hisa.kicad.market.checker;

public class MarketCheckerFactory {

	static AbstractMarketChecker DIGIKEY = new MarketCheckerDigikey();
	
	public static AbstractMarketChecker getChecker(String url) throws NoCheckerFoundException {
		return DIGIKEY;
	}
	
	public static class NoCheckerFoundException extends Exception {}
	
}
