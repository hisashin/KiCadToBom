package dev.hisa.kicad.market.checker;

public class MarketCheckerFactory {

	static AbstractMarketChecker DIGIKEY = new MarketCheckerDigikey();
	static AbstractMarketChecker ALIEXPRESS = new MarketCheckerAliexpress();
	static AbstractMarketChecker TI = new MarketCheckerTI();
	
	public static AbstractMarketChecker getChecker(String url) throws NoCheckerFoundException {
		if(url == null)
			throw new NoCheckerFoundException();
		if(url.contains("www.digikey.com"))
			return DIGIKEY;
		//if(url.contains("aliexpress"))
		//	return ALIEXPRESS;
		//if(url.contains("www.ti.com"))
		//	return TI;
		throw new NoCheckerFoundException();
	}
	
	@SuppressWarnings("serial")
	public static class NoCheckerFoundException extends Exception {}
	
}
