package dev.hisa.kicad.stock;

import java.util.HashMap;
import java.util.Map;

import dev.hisa.kicad.stock.crawler.CrawlerResult;

public class Chip {

	Code code;
	String[] urlArray;
	Map<String, CrawlerResult> results = new HashMap<String, CrawlerResult>();

}
