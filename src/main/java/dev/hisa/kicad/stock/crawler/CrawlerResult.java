package dev.hisa.kicad.stock.crawler;

import java.util.Date;

public class CrawlerResult {
	
	public static enum Status {
		SUCCESS("success"),
		FAIL("fail"),
		;
		String label;
		Status(String label) {
			this.label = label;
		}
		public static Status find(String label) {
			for(Status status : Status.values())
				if(status.label.equals(label))
					return status;
			return null;
		}
	}
	
	String url;
	String status;
	int countInStock;
	double priceInUsdFor100pce;
	Date updateDate;
	
}