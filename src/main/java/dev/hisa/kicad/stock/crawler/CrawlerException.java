package dev.hisa.kicad.stock.crawler;

@SuppressWarnings("serial")
public class CrawlerException extends Exception {
	String msg;
	CrawlerException(String msg) {
		this.msg = msg;
	}
	@Override
	public String getMessage() {
		return msg;
	}
}