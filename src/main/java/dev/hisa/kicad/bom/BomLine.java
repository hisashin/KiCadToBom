package dev.hisa.kicad.bom;

import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import dev.hisa.kicad.bom.AbstractBom.MapSymbolsKey;
import dev.hisa.kicad.box.PartsBox;
import dev.hisa.kicad.box.StockStatus;
import dev.hisa.kicad.box.PartsBox.NotUniquePackOrDesignationException;
import dev.hisa.kicad.orm.Sheet;
import dev.hisa.kicad.orm.Symbol;

public class BomLine {
	
	public MapSymbolsKey key;
	public List<Symbol> subList;
	public Collection<Sheet> sheets;
	public StringBuffer designators;
	public StockStatus stockStatus;
	
	BomLine(MapSymbolsKey key, List<Symbol> subList, Collection<Sheet> sheets, PartsBox partsBox, boolean isJellyBeans, boolean isPins) throws NotUniquePackOrDesignationException {
		this.key = key;
		this.subList = subList;
		this.sheets = sheets;
		this.designators = getDesinators();
		this.stockStatus = partsBox == null ? null : partsBox.getStatus(subList, sheets, isJellyBeans, isPins, isAllResistor(), isAllCapacitor());
	}
	static Pattern patternDesignatorResistor = Pattern.compile("^R[0-9]+$");
	static Pattern patternDesignatorCapacitor = Pattern.compile("^C[0-9]+$");
	
	public boolean isAllResistor() {
		for(Symbol symbol : subList)
			if(!patternDesignatorResistor.matcher(symbol.designator).find())
				return false;
		return true;
	}
	public boolean isAllCapacitor() {
		for(Symbol symbol : subList)
			if(!patternDesignatorCapacitor.matcher(symbol.designator).find())
				return false;
		return true;
	}
	public StringBuffer getDesinators() {
		StringBuffer buf = new StringBuffer();
		String sep = "";
		for(Symbol symbol : subList) {
			buf.append(sep).append(symbol.designator);
			sep = ",";
		}
		return buf;
	}
	public StringBuffer getSheetNames() {
		StringBuffer buf = new StringBuffer();
		String sep = "";
		for(Sheet sheet : sheets) {
			buf.append(sep).append("@").append(sheet.label);
			sep = ",";
		}
		return buf;
	}
	public StringBuffer toStringBuffer() {
		StringBuffer buf = new StringBuffer(designators)
				.append("\t").append(subList.size())
				.append("\t").append(key.pack)
				//.append("\t").append(key.pack.replaceAll("^.*:", ""))
				.append("\t").append(key.designation)
				.append("\t").append(getSheetNames());
		return buf;
	}
}