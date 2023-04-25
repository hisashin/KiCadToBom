package dev.hisa.kicad.bom;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.hisa.kicad.bom.inspector.InspectorException;
import dev.hisa.kicad.bom.inspector.InspectorJumper;
import dev.hisa.kicad.bom.inspector.InspectorJumper.InspectorJumperException;
import dev.hisa.kicad.bom.inspector.InspectorPins;
import dev.hisa.kicad.bom.inspector.InspectorPins.InspectorPinsException;
import dev.hisa.kicad.box.PartsBox;
import dev.hisa.kicad.box.PartsBox.DuplicatePartInBox;
import dev.hisa.kicad.box.PartsBox.NotUniquePackOrDesignationException;
import dev.hisa.kicad.box.PartsBox.PartInBox;
import dev.hisa.kicad.box.PartsBoxShingo;
import dev.hisa.kicad.box.StockStatus;
import dev.hisa.kicad.orm.Footprint;
import dev.hisa.kicad.orm.Project;
import dev.hisa.kicad.orm.Sheet;
import dev.hisa.kicad.orm.Symbol;

public abstract class AbstractBom {

	@SuppressWarnings("serial")
	public static class BomException extends Exception {
		String msg;
		public BomException(String msg) {
			this.msg = msg;
		}
		@Override
		public String getMessage() {
			return this.msg;
		}
	}
	
	protected Path path;
	protected boolean separateBySheetId;
	
	protected AbstractBom(Path path, boolean separateBySheetId) throws StreamReadException, DatabindException, IOException, BomException {
		if(path == null || path.getFileName().endsWith(".kicad_pro"))
			throw new BomException("use .kicad_pro");
		this.path = path;
		this.separateBySheetId = separateBySheetId;
		//System.out.println(this);
		//detect("00000000-0000-0000-0000-000061a99482", "J19", "fb8e20ae-ac6e-4056-b641-224f6c6653ae", "f4945194-36e4-417a-821b-2c19298110e8", "8e9c9f85-ab7c-4d0a-8da5-8d5feecdc210");
		prepareSheets(path);
		parsePcb();
		parseSheets();
	}
	
	void detect(String... values) throws IOException {
		if(values == null || values.length <= 0)return;
		Iterator<Path> ite = Files.list(path.getParent()).iterator();
		Path p;
		List<String> list;
		while(ite.hasNext()) {
			p = ite.next();
			if(Files.isDirectory(p) || p.getFileName().toString().endsWith(".zip") || ".DS_Store".equals(p.getFileName().toString()))continue;
			list = contains(p, values);
			for(String line : list)
				System.out.println("!Found@" + p.getFileName() + " : " + line);
		}
	}
	static List<String> contains(Path path, String... values) throws IOException {
		List<String> list = new ArrayList<String>();
		List<String> lines = Files.readAllLines(path);
		for(String line : lines)
			for(String value : values)
				if(line.contains(value))
					list.add(line);
		return list;
	}
	
	void prepareSheets(Path path) throws StreamReadException, DatabindException, IOException {
		Project project = Project.getInstance(path);
		mapSheet = new HashMap<String, Sheet>();
		for(String[] sheet : project.sheetsTmp)
			mapSheet.put(sheet[0], new Sheet(sheet[0], sheet[1]));
	}
	
	public Map<String, Sheet> mapSheet = null;
	public Collection<Sheet> getSheets() {
		return mapSheet.values();
	}
	public Sheet getSheetTop() {
		for(Sheet sheet : getSheets())
			if(sheet.top)
				return sheet;
		return null;
	}
	
	public Map<String, List<Footprint>> mapFootprints = new HashMap<String, List<Footprint>>();
	public void add(Footprint footprint) {
		if(footprint.sheetId == null)return;
		Sheet sheet = mapSheet.get(footprint.sheetId);
		List<Footprint> subList = mapFootprints.get(sheet.id);
		if(subList == null) {
			subList = new ArrayList<Footprint>();
			mapFootprints.put(sheet.id, subList);
		}
		subList.add(footprint);
	}
	public List<Footprint> getFootprints() {
		List<Footprint> list = new ArrayList<Footprint>();
		for(List<Footprint> subList : mapFootprints.values())
			list.addAll(subList);
		return list;
	}
	public List<Footprint> getFootprints(String sheetId) {
		return mapFootprints.get(sheetId);
	}
	
	Map<MapSymbolsKey, List<Symbol>> mapSymbols = new HashMap<MapSymbolsKey, List<Symbol>>();
	static class MapSymbolsKey {
		public String pack;
		public String designation;
		public String sheetId;
		MapSymbolsKey(Symbol symbol, boolean separateBySheetId) {
			this.pack = symbol.pack;
			this.designation = symbol.designation;
			this.sheetId = separateBySheetId ? symbol.sheetId : null;
		}
		@Override
		public int hashCode() {
			return Objects.hash(pack, designation, sheetId);
		}
	    @Override
		public boolean equals(Object _obj) {
			if(_obj == null || !(_obj instanceof MapSymbolsKey))return false;
			MapSymbolsKey obj = (MapSymbolsKey)_obj;
			return equals(this.pack, obj.pack) && equals(this.designation, obj.designation) && equals(this.sheetId, obj.sheetId);
		}
		static boolean equals(String value1, String value2) {
			if(value1 == null && value2 == null)return true;
			if(value1 == null || value2 == null)return false;
			return value1.equals(value2);
		}
	}
	public void add(Symbol symbol) {
		MapSymbolsKey key = new MapSymbolsKey(symbol, separateBySheetId);
		List<Symbol> subList = mapSymbols.get(key);
		if(subList == null) {
			subList = new ArrayList<Symbol>();
			mapSymbols.put(key, subList);
		}
		for(Symbol exist : subList)
			if(exist.designator.equals(symbol.designator))
				return;
		subList.add(symbol);
	}
	public List<Symbol> getSymbols() {
		List<Symbol> list = new ArrayList<Symbol>();
		for(List<Symbol> subList : mapSymbols.values())
			list.addAll(subList);
		return list;
	}

	public void parsePcb() throws IOException {
		for(Path p : getPathList("kicad_pcb"))
			parsePcb(p);
	}
	
	static Pattern patternFootprintStart = Pattern.compile("^  \\(footprint \"(.*)\" \\(layer \".*\"\\)$");
	static Pattern patternFootprintTags = Pattern.compile("\\(tags \"(.*)\"\\)");
	static Pattern patternFootprintReference = Pattern.compile("\\(fp_text reference \"([^\"]*)\"");
	static Pattern patternFootprintSheetFile = Pattern.compile("\\(property \"Sheetfile\" \"(.*)\"\\)");
	static Pattern patternFootprintSheetName = Pattern.compile("\\(property \"Sheetname\" \"(.*)\"\\)");
	static Pattern patternFootprintPathOthers = Pattern.compile("\\(path \"/(.*)/(.*)\"\\)");
	static Pattern patternFootprintPathTop = Pattern.compile("\\(path \"/(.*)\"\\)");
	static Pattern patternFootprintClose = Pattern.compile("^  \\)$");
	void parsePcb(Path path) throws IOException {
		System.out.println("Reading " + path.toAbsolutePath().toString());
		//print(path);
		List<String> lines = Files.readAllLines(path);
		Matcher m;
		boolean footprint = false;
		Footprint obj = null;
		StringBuffer buf = null;
		for(String line : lines) {
			if(!footprint) {
				m = patternFootprintStart.matcher(line);
				if(m.find()) {
					obj = new Footprint();
					buf = new StringBuffer();
					obj.pack = m.group(1);
					footprint = true;
				}
			} else if (obj != null) {
				if(buf != null)
					buf.append(line).append("\n");
				m = patternFootprintTags.matcher(line);
				if(m.find()) {
					obj.tags = m.group(1);
				} else {
					m = patternFootprintReference.matcher(line);
					if(m.find()) {
						obj.reference = m.group(1);
					} else {
						m = patternFootprintSheetFile.matcher(line);
						if(m.find()) {
							obj.sheetfile = m.group(1);
						} else {
							m = patternFootprintSheetName.matcher(line);
							if(m.find()) {
								String sheetname = m.group(1);
								if(sheetname.length() <= 0)
									sheetname = Sheet.getDefaultLabel();
								obj.sheetname = sheetname;
							} else {
								m = patternFootprintPathOthers.matcher(line);
								if(m.find()) {
									obj.sheetId = m.group(1);
									obj.symbolId = m.group(2);
								} else {
									m = patternFootprintPathTop.matcher(line);
									if(m.find()) {
										obj.sheetId = getSheetTop().id;
										obj.symbolId = m.group(1);
									} else {
										m = patternFootprintClose.matcher(line);
										if(m.find()) {
											add(obj);
											if(obj.sheetId != null) {
												Sheet sheet = mapSheet.get(obj.sheetId);
												if(sheet != null && sheet.filename == null)
													sheet.filename = obj.sheetfile;
											}
											buf = null;
											obj = null;
											footprint = false;
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	void parseSheets() throws IOException {
		for(Path p : getPathList("kicad_sch")) {
			for(Sheet sheet : mapSheet.values())
				if(sheet.filename.equals(p.getFileName().toString())) {
					parseSheet(sheet, p);
				}
		}
	}

	public Collection<Sheet> getSheets(List<Symbol> symbols) {
		Map<String, Sheet> map = new HashMap<String, Sheet>();
		for(Symbol symbol : symbols) {
			if(map.containsKey(symbol.sheetId))continue;
			map.put(symbol.sheetId, mapSheet.get(symbol.sheetId));
		}
		return map.values();
	}
	
	static Pattern patternSheetStart = Pattern.compile("^  \\(symbol \\(lib_id \"(.*)\"\\)");
	static Pattern patternSheetId = Pattern.compile("^    \\(uuid (.*)\\)");
	static Pattern patternSheetReference = Pattern.compile("^    \\(property \"Reference\" \"(.*)\"");
	static Pattern patternSheetValue = Pattern.compile("^    \\(property \"Value\" \"(.*)\"");
	static Pattern patternSheetFootprint = Pattern.compile("^    \\(property \"Footprint\" \"(.*)\"");
	static Pattern patternSheetClose = Pattern.compile("^  \\)$");
	void parseSheet(Sheet sheet, Path path) throws IOException {
		//System.out.println("Reading " + path.toAbsolutePath().getFileName());
		//print(p);
		List<String> lines = Files.readAllLines(path);
		Matcher m;
		boolean symbol = false;
		Symbol obj = null;
		StringBuffer buf = null;
		for(String line : lines) {
			if(!symbol) {
				m = patternSheetStart.matcher(line);
				if(m.find()) {
					obj = new Symbol();
					buf = new StringBuffer();
					obj.pack = m.group(1);
					symbol = true;
				}
			} else if (obj != null) {
				if(buf != null)
					buf.append(line).append("\n");
				m = patternSheetId.matcher(line);
				if(m.find()) {
					obj.sheetId = sheet.id;
					obj.id = m.group(1);
				} else {
					m = patternSheetReference.matcher(line);
					if(m.find()) {
						obj.designator = m.group(1);
						if(obj.designator.startsWith("#")) {
							buf = null;
							obj = null;
							symbol = false;
						}
					} else {
						m = patternSheetValue.matcher(line);
						if(m.find()) {
							obj.designation = m.group(1);
						} else {
							m = patternSheetFootprint.matcher(line);
							if(m.find()) {
								obj.pack = m.group(1);
							} else {
								m = patternSheetClose.matcher(line);
								if(m.find()) {
									add(obj);
									//System.out.println(obj);
									buf = null;
									obj = null;
									symbol = false;
								}
							}
						}
					}
				}
			}
		}
		return;
	}

	List<Path> getPathList(String extension) throws IOException {
		List<Path> list = new ArrayList<Path>();
		Iterator<Path> ite = Files.list(path.getParent()).iterator();
		Path p;
		while(ite.hasNext()) {
			p = ite.next();
			if(!p.getFileName().toString().endsWith("." + extension))continue;
			list.add(p);
		}
		return list;
	}
	static void print(Path path) throws IOException {
		System.out.println("----" + path.getFileName() + " start ----");
		List<String> lines = Files.readAllLines(path);
		for(String line : lines)
			System.out.println(line);
		System.out.println("----" + path.getFileName() + " end ----");
	}
	
	boolean includeJellyBeans = true;
	boolean includeRegulator = true;
	boolean includeCapacitor = true;
	boolean includePins = true;
	AbstractBom setIncludeJellyBeans(boolean includeJellyBeans) {
		this.includeJellyBeans = includeJellyBeans;
		return this;
	}
	AbstractBom setIncludeRegulator(boolean includeRegulator) {
		this.includeRegulator = includeRegulator;
		return this;
	}
	AbstractBom setIncludeCapacitor(boolean includeCapacitor) {
		this.includeCapacitor = includeCapacitor;
		return this;
	}
	AbstractBom setIncludePins(boolean includePins) {
		this.includePins = includePins;
		return this;
	}
	@Override
	public String toString() {
		try {
			return toString(null);
		} catch (NotUniquePackOrDesignationException e) {
			e.printStackTrace();
			return null;
		}
	}
	static class MapContainer {
		Map<String, BomLine> mapSuperStar = new TreeMap<String, BomLine>();
		Map<String, BomLine> mapJellyBeans = new TreeMap<String, BomLine>();
		Map<String, BomLine> mapRegulator = new TreeMap<String, BomLine>();
		Map<String, BomLine> mapCapacitor = new TreeMap<String, BomLine>();
		Map<String, BomLine> mapPins = new TreeMap<String, BomLine>();
		MapContainer(AbstractBom bom) throws NotUniquePackOrDesignationException {
			PartsBox partsBox = null;
			boolean isJellyBeans, isPins;
			Iterator<MapSymbolsKey> iteTmp = bom.mapSymbols.keySet().iterator();
			while(iteTmp.hasNext()) {
				MapSymbolsKey key = iteTmp.next();
				List<Symbol> subList = bom.mapSymbols.get(key);
				Collection<Sheet> sheets = bom.getSheets(subList);
				try {
					new InspectorJumper(key.pack);
				} catch (InspectorJumperException e) {
					continue;
				}
				try {
					Sheet sheet = bom.mapSheet.get(key.sheetId);
					bom.validateJellyBeans(key.pack, key.designation, sheet);
					isJellyBeans = false;
				} catch (InspectorJellyBeansException e) {
					isJellyBeans = true;
				}
				try {
					new InspectorPins(key.pack);
					isPins = false;
				} catch (InspectorPinsException e) {
					isPins = true;
				}
				BomLine line = new BomLine(key, subList, sheets, partsBox, isJellyBeans, isPins);
				if(isPins)
					mapPins.put(line.designators.toString(), line);
				else if(line.isAllResistor())
					mapRegulator.put(line.designators.toString(), line);
				else if(line.isAllCapacitor())
					mapCapacitor.put(line.designators.toString(), line);
				else if(isJellyBeans)
					mapJellyBeans.put(line.designators.toString(), line);
				else
					mapSuperStar.put(line.designators.toString(), line);
			}
		}
	}
	public String toString(PartsBox partsBox) throws NotUniquePackOrDesignationException {
		MapContainer container = new MapContainer(this);
		StringBuffer buf = new StringBuffer();
		String sep = "\n";
		buf.append("[SuperStar]");
		for(BomLine line : container.mapSuperStar.values())
			buf.append(sep).append(line.toStringBuffer());
		if(includeJellyBeans) {
			buf.append(sep).append("[JellyBeans]");
			for(BomLine line : container.mapJellyBeans.values())
				buf.append(sep).append(line.toStringBuffer());
		}
		if(includeRegulator) {
			buf.append(sep).append("[R]");
			for(BomLine line : container.mapRegulator.values())
				buf.append(sep).append(line.toStringBuffer());
		}
		if(includeCapacitor) {
			buf.append(sep).append("[C]");
			for(BomLine line : container.mapCapacitor.values())
				buf.append(sep).append(line.toStringBuffer());
		}
		if(includePins) {
			buf.append(sep).append("[Pins]");
			for(BomLine line : container.mapPins.values())
				buf.append(sep).append(line.toStringBuffer());
		}
		return buf.toString();
	}
	public String toPartsBoxTemplate() throws NotUniquePackOrDesignationException, DuplicatePartInBox, JsonProcessingException {
		PartsBox box = PartsBoxShingo.getInstance();
		MapContainer container = new MapContainer(this);
		for(BomLine line : container.mapSuperStar.values())
			add(box, line);
		for(BomLine line : container.mapJellyBeans.values())
			add(box, line);
		for(BomLine line : container.mapRegulator.values())
			add(box, line);
		for(BomLine line : container.mapCapacitor.values())
			add(box, line);
		for(BomLine line : container.mapPins.values())
			add(box, line);
		ObjectMapper mapper = new ObjectMapper();
		return new String(mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(box.getList()));
	}
	void add(PartsBox box, BomLine line) throws DuplicatePartInBox {
		String pack = line.key.pack;
		String designation = line.key.designation;
		PartInBox partInBox = box.find(pack, designation);
		if(partInBox != null)return;
		partInBox = new PartInBox("x", "function", pack, designation, "https://...", "0");
		box.add(partInBox);
	}
	static class BomLine {
		MapSymbolsKey key;
		List<Symbol> subList;
		Collection<Sheet> sheets;
		StringBuffer designators;
		StockStatus stockStatus;
		BomLine(MapSymbolsKey key, List<Symbol> subList, Collection<Sheet> sheets, PartsBox partsBox, boolean isJellyBeans, boolean isPins) throws NotUniquePackOrDesignationException {
			this.key = key;
			this.subList = subList;
			this.sheets = sheets;
			this.designators = getDesinators();
			this.stockStatus = partsBox == null ? null : partsBox.getStatus(subList, sheets, isJellyBeans, isPins, isAllResistor(), isAllCapacitor());
		}
		static Pattern patternDesignatorResistor = Pattern.compile("^R[0-9]+$");
		static Pattern patternDesignatorCapacitor = Pattern.compile("^C[0-9]+$");
		boolean isAllResistor() {
			for(Symbol symbol : subList)
				if(!patternDesignatorResistor.matcher(symbol.designator).find())
					return false;
			return true;
		}
		boolean isAllCapacitor() {
			for(Symbol symbol : subList)
				if(!patternDesignatorCapacitor.matcher(symbol.designator).find())
					return false;
			return true;
		}
		StringBuffer getDesinators() {
			StringBuffer buf = new StringBuffer();
			String sep = "";
			for(Symbol symbol : subList) {
				buf.append(sep).append(symbol.designator);
				sep = ",";
			}
			return buf;
		}
		StringBuffer getSheetNames() {
			StringBuffer buf = new StringBuffer();
			String sep = "";
			for(Sheet sheet : sheets) {
				buf.append(sep).append("@").append(sheet.label);
				sep = ",";
			}
			return buf;
		}
		StringBuffer toStringBuffer() {
			StringBuffer buf = new StringBuffer(designators)
					.append("\t").append(subList.size())
					.append("\t").append(key.pack)
					//.append("\t").append(key.pack.replaceAll("^.*:", ""))
					.append("\t").append(key.designation)
					.append("\t").append(getSheetNames());
			return buf;
		}
	}
	
	String[] getCommonJellyBeansPackStartsWith() {
		return new String[] {
			"Ninja-qPCR:Raspberry_Pi",
			"Connector_Hirose:Hirose_",
			"Ninja-qPCR:TB_SeeedOPL_320110028",
			"Ninja-qPCR:FFC_60_Ali_HUISHUNFA",
			"Ninja-qPCR:ATX8",
			"Ninja-qPCR:EP2-3L3SAb",
			"Ninja-qPCR:SOT95P240X112-3N",
			"Ninja-qPCR:LITE_LCD",
			"Ninja-qPCR:ALI_USB",
			"Ninja-qPCR:REG_TO252-3",
			"Connector_BarrelJack:BarrelJack_Horizontal",
			"Ninja:SOT95P240X112-3N",
			"Ninja:REG_TO252-3",
		};
	}
	protected String[] getCustomJellyBeansPackStartsWith() {
		return null;
	}
	String[] getCommonJellyBeansDesignationStartsWith() {
		return new String[] {
			"TACTILE",
			"Screw_Terminal",
			"ALI_USB",
			"LMP7717MF_NOPB",
		};
	}
	protected String[] getCustomJellyBeansDesignationStartsWith() {
		return null;
	}
	static void validate(String value, String[] arrayStartsWith) throws InspectorJellyBeansException {
		if(value == null || arrayStartsWith == null || arrayStartsWith.length <= 0)return;
		for(String tmp : arrayStartsWith)
			if(value.startsWith(tmp))
				throw new InspectorJellyBeansException();
	}
	@SuppressWarnings("serial")
	public static class InspectorJellyBeansException extends InspectorException {}
	protected void validateJellyBeans(String pack, String designation, Sheet sheet) throws InspectorJellyBeansException {
		validate(pack, getCommonJellyBeansPackStartsWith());
		validate(designation, getCommonJellyBeansDesignationStartsWith());
		validate(pack, getCustomJellyBeansPackStartsWith());
		validate(designation, getCustomJellyBeansDesignationStartsWith());
	}
	
}
