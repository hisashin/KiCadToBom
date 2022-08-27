package dev.hisa.kicad.stock.crawler;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractCrawler {

	abstract void login();
	abstract CrawlerResult crawl(String url) throws CrawlerException;
	
	public final List<CrawlerResult> crawl(List<String> listUrl) {
		List<CrawlerResult> list = new ArrayList<CrawlerResult>();
		login();
		for(String url : listUrl) {
			try {
				list.add(crawl(url));
			} catch (CrawlerException e) {
				e.printStackTrace();
			}
		}
		return list;
	}
	
}
