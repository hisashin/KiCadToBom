package dev.hisa.kicad.market;

import java.util.Date;

public class MarketResult {
	
	public static enum Market {
		Digikey,
		Aliexpress,
		Akizuki,
		TI,
		;
	}
	public Market market;
	public String url;
	public String label;
	public Integer stock;
	public Double unitPriceFor100;
	public String supplier;
	public String datasheet;
	public Date updateTime;

}
