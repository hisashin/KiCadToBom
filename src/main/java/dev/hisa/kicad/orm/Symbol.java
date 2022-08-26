package dev.hisa.kicad.orm;

public class Symbol {
	
	public String sheetId;
	public String id;
	
	public String designator;
	public String pack;
	public String designation;
	
	@Override
	public String toString() {
		return "Symbol[id=" + id +", designator=" + designator + ", pack=" + pack + ", designation=" + designation + ", sheetId=" + sheetId + "]";
	}
}