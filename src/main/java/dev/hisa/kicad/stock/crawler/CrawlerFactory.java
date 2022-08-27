package dev.hisa.kicad.stock.crawler;

import java.util.ArrayList;
import java.util.List;

public class CrawlerFactory {

	static AbstractCrawler crawlerDigikey = new CrawlerDigikey();
	static AbstractCrawler crawlerTI = new CrawlerTI();
	
	public final List<CrawlerResult> crawl(String... urlArray) throws CrawlerException {
		List<String> urlDigikey = new ArrayList<String>();
		List<String> urlTI = new ArrayList<String>();
		for(String url : urlArray) {
			if(CrawlerDigikey.isMyUrl(url))
				urlDigikey.add(url);
			else if(CrawlerTI.isMyUrl(url))
				urlTI.add(url);
			else
				throw new CrawlerException("");
		}
		List<CrawlerResult> list = new ArrayList<CrawlerResult>();
		if(urlDigikey.size() > 0)
			list.addAll(crawlerDigikey.crawl(urlDigikey));
		if(urlTI.size() > 0)
			list.addAll(crawlerTI.crawl(urlTI));
		return list;
	}

}
