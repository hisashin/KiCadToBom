package dev.hisa.kicad.orm;

public class Footprint {

	public String pack;
	public String tags;
	public String reference;
	public String sheetfile;
	public String sheetname;
	public String sheetId;
	public String symbolId;
	
	@Override
	public String toString() {
		return "Footprint[pack=" + pack + ", tags=" + tags + ", reference=" + reference + ", sheetfile=" + sheetfile + ", sheetname=" + sheetname + ", sheetId=" + sheetId + ", symbolId=" + symbolId + "]";
 	}

}
