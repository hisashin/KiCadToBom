package dev.hisa.kicad.market;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(value = { "tooOld" })
public class MarketResult {
	
	static long updateIntervalInMillis = 7 * 24 * 3600 * 1000; // week
	
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
	
	public boolean isTooOld() {
		if(updateTime == null)return false;
		long limit = System.currentTimeMillis() - updateIntervalInMillis;
		return limit > updateTime.getTime();
	}
}
