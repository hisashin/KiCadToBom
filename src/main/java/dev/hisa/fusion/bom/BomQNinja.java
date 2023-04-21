package dev.hisa.fusion.bom;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

/*
 * BOM can be exported from DRAWING->Parts List
 */
public class BomQNinja {

	static Path path = Paths.get("/Users/shingo/github/KiCadBomParser/src/main/java/dev/hisa/fusion/bom/bomQNinja.csv");

	public static void main(String[] args) throws IOException {
		BomQNinja bom = new BomQNinja(path.toFile());
		if(true) {
			for(Part part : bom.mapTree.values())
				System.out.println(part.toString(0, false));
		}
		int quantity;
		System.out.println("---- PCB ----");
		for(String key : bom.mapPCB.keySet()) {
			quantity = 0;
			for(Part part : bom.mapPCB.get(key))
				quantity += part.quantity;
			System.out.println(key + "\t" + quantity);
		}
		System.out.println("---- Metal3dprint ----");
		System.out.println("// TODO add manually if nothing listed");
		for(String key : bom.mapMetal3dprint.keySet()) {
			quantity = 0;
			for(Part part : bom.mapMetal3dprint.get(key))
				quantity += part.quantity;
			System.out.println(key + "\t" + quantity);
		}
		System.out.println("---- SheetMetal ----");
		for(String key : bom.mapSheetMetal.keySet()) {
			quantity = 0;
			for(Part part : bom.mapSheetMetal.get(key))
				quantity += part.quantity;
			System.out.println(key + "\t" + quantity);
		}
		System.out.println("---- Optical ----");
		System.out.println("// TODO add manually if nothing listed");
		for(String key : bom.mapOptical.keySet()) {
			quantity = 0;
			for(Part part : bom.mapOptical.get(key))
				quantity += part.quantity;
			System.out.println(key + "\t" + quantity);
		}
		System.out.println("---- Thermal ----");
		for(String key : bom.mapThermal.keySet()) {
			quantity = 0;
			for(Part part : bom.mapThermal.get(key))
				quantity += part.quantity;
			System.out.println(key + "\t" + quantity);
		}
		System.out.println("---- 3dprint ----");
		for(String key : bom.map3dprint.keySet()) {
			quantity = 0;
			for(Part part : bom.map3dprint.get(key))
				quantity += part.quantity;
			System.out.println(key + "\t" + quantity);
		}
		System.out.println("---- Screws ----");
		for(String key : bom.mapScrews.keySet()) {
			quantity = 0;
			for(Part part : bom.mapScrews.get(key))
				quantity += part.quantity;
			System.out.println(key + "\t" + quantity);
		}
		System.out.println("---- Woods ----");
		for(String key : bom.mapWoods.keySet()) {
			quantity = 0;
			for(Part part : bom.mapWoods.get(key))
				quantity += part.quantity;
			System.out.println(key + "\t" + quantity);
		}
		System.out.println("---- Misc ----");
		for(String key : bom.mapMisc.keySet()) {
			quantity = 0;
			for(Part part : bom.mapMisc.get(key))
				quantity += part.quantity;
			System.out.println(key + "\t" + quantity);
		}
		System.out.println("---------------");
	}
	
	boolean ignore(Part part) {
		String number = part.number;
		return "Parts List".equals(number)
			|| number.startsWith("Unit_tubes")
		;
	}
	boolean ignoreChildren(Part part) {
		String number = part.number;
		return number.startsWith("Heatsink")
				|| number.startsWith("SLBNR3")
				|| number.startsWith("RubberFoot")
				|| "Well_heater".equals(number)
				|| part.isPCB()
			;
	}
	
	Map<String, Part> mapTree = new TreeMap<String, Part>();
	Map<String, Part> mapAll = new TreeMap<String, Part>();
	Map<String, List<Part>> mapPCB = new TreeMap<String, List<Part>>();
	Map<String, List<Part>> mapMetal3dprint = new TreeMap<String, List<Part>>();
	Map<String, List<Part>> mapSheetMetal = new TreeMap<String, List<Part>>();
	Map<String, List<Part>> mapOptical = new TreeMap<String, List<Part>>();
	Map<String, List<Part>> mapThermal = new TreeMap<String, List<Part>>();
	Map<String, List<Part>> mapWoods = new TreeMap<String, List<Part>>();
	Map<String, List<Part>> mapScrews = new TreeMap<String, List<Part>>();
	Map<String, List<Part>> map3dprint = new TreeMap<String, List<Part>>();
	Map<String, List<Part>> mapMisc = new TreeMap<String, List<Part>>();
	BomQNinja(File file) throws IOException {
		parse(path.toFile());
		for(Part part : mapAll.values()) {
			if(part.hasChildren())continue;
			if(part.isPCB())
				add(mapPCB, part);
			if(part.isMetal3dprint())
				add(mapMetal3dprint, part);
			else if(part.isSheetMetal())
				add(mapSheetMetal, part);
			else if(part.isOptical())
				add(mapOptical, part);
			else if(part.isThermal())
				add(mapThermal, part);
			else if(part.isWood())
				add(mapWoods, part);
			else if(part.isScrew())
				add(mapScrews, part);
			else if(part.is3dprint())
				add(map3dprint, part);
			else if(part.isMisc())
				add(mapMisc, part);
		}
	}
	static void add(Map<String, List<Part>> map, Part part) {
		List<Part> subList = map.get(part.getLabel());
		if(subList == null) {
			subList = new ArrayList<Part>();
			map.put(part.getLabel(), subList);
		}
		subList.add(part);
	}
	
	static String HEADER_ITEM = "Item";
	static String HEADER_QUANTITY = "Qty";
	static String HEADER_NUMBER = "Part Number";
	static String HEADER_DESCRIPTION = "Description";
	static String HEADER_MATERIAL = "Material";
	Collection<Part> parse(File file) throws IOException {
		Reader in = new FileReader(file);
		final CSVFormat csvFormat = CSVFormat.Builder.create()
				.setHeader(HEADER_ITEM, HEADER_QUANTITY, HEADER_NUMBER, HEADER_DESCRIPTION, HEADER_MATERIAL)
				.setAllowMissingColumnNames(true)
				.build();
		final Iterable<CSVRecord> records = csvFormat.parse(in);
		String parentItem;
		for(CSVRecord record : records) {
			Part part = new Part(record);
			if("Parts List".equals(part.item) || "Item".equals(part.item) || ignore(part))continue;
			parentItem = part.getParentItem();
			if(parentItem != null) {
				Part parent = mapAll.get(parentItem);
				if(parent != null && !ignoreChildren(parent))
					parent.add(part);
			} else {
				mapTree.put(part.item, part);
			}
			mapAll.put(part.item, part);
		}
		return mapTree.values();
	}
	static class Part {
		
		String item;
		int quantity;
		String number;
		String description;
		String material;
		
		Part parent = null;
		List<Part> children = new ArrayList<Part>();
		void add(Part part) {
			this.children.add(part);
			part.parent = this;
		}
		boolean hasChildren() {
			return this.children.size() > 0;
		}
		
		Part(CSVRecord record) {
			this.item = record.get(HEADER_ITEM);
			this.quantity = parseInt(record.get(HEADER_QUANTITY));
			this.number = record.get(HEADER_NUMBER);
			this.description = record.get(HEADER_DESCRIPTION);
			this.material = record.get(HEADER_MATERIAL);
		}
		static int parseInt(String value) {
			try {
				return Integer.parseInt(value);
			} catch(NumberFormatException ex) {
				return -1;
			}
		}
		@Override
		public String toString() {
			return toString(0, false);
		}
		String toString(int indent, boolean skipSubs) {
			StringBuffer buf = new StringBuffer();
			buf.append(getIndent(indent)).append(hasChildren() ? "x" : "").append("Part[item=" + item + ", quantity=" + quantity + ", number=" + number + ", description=" + description + ", material=" + material + ", isPCB=" + isPCB() + ", isM3d=" + isMetal3dprint() + ", isSM=" + isSheetMetal() + ", isOpt=" + isOptical() + ", isTh=" + isThermal() + ", is3dprint=" + is3dprint() + ", isScrew=" + isScrew() + ", isWood=" + isWood() + ", isMisc=" + isMisc() + "]");
			for(Part child : children) {
				if(skipSubs)
					if(child.isPCB() || child.isMetal3dprint() || child.isSheetMetal() || child.isOptical() || child.isThermal() || child.is3dprint() || child.isScrew() || child.isWood() || child.isMisc())continue;
				buf.append("\n");
				buf.append(child.toString(indent + 1, skipSubs));
			}
			return buf.toString();
		}
		static StringBuffer getIndent(int indent) {
			StringBuffer buf = new StringBuffer();
			for(int i = 0 ; i < indent ; i++)
				buf.append(" ");
			return buf;
		}
		String getParentItem() {
			if(!item.contains("."))return null;
			return item.substring(0, item.lastIndexOf("."));
		}
		
		boolean isPCB() {
			return "QPCR-main-qPCR-main".equals(number)
				|| "QPCR-photo".equals(number)
				|| "QPCR-led-north".equals(number)
				|| "QPCR-led-south".equals(number)
				|| "QPCR-holder".equals(number)
			;
		}
		boolean isMetal3dprint() {
			// ツリー下層にヒーターやラバーがあるのでこれだけ手動で追加した方が良い
			//return number.startsWith("Tube_holder");
			return false;
		}
		boolean isSheetMetal() {
			return "Metal_heating".equals(number);
		}
		boolean isOptical() {
			return false;
		}
		boolean isThermal() {
			return "Well_heater".equals(number)
				|| "HeatRubber".equals(number);
		}
		boolean is3dprint() {
			return hasParent("3dprint", false, false)
				|| number.startsWith("Led_cover")
				|| number.startsWith("Pd_cover")
				|| number.startsWith("Optics_support")
			;
		}
		boolean isScrew() {
			return hasParent("screws", true, false);
		}
		boolean isWood() {
			return number.toLowerCase().startsWith("wood");
		}
		boolean isMisc() {
			return number.startsWith("N306_latch")
					|| number.startsWith("Raspberry PI")
					|| number.startsWith("Ras Pi")
					|| number.startsWith("Heatsink")
					|| number.startsWith("JLT_7inch")
					|| number.startsWith("FanGuard")
				;
		}
		boolean hasParent(String value, boolean contains, boolean caseSensitive) {
			Part tmp = this;
			do {
				if(contains) {
					if(caseSensitive && tmp.number.contains(value))
						return true;
					if(!caseSensitive && tmp.number.toLowerCase().contains(value))
						return true;
				}
				if(!contains) {
					if(caseSensitive && tmp.number.equals(value))
						return true;
					if(!caseSensitive && tmp.number.toLowerCase().equals(value))
						return true;
				}
				tmp = tmp.parent;
			} while(tmp != null);
			return false;
		}
		
		String getLabel() {
			if(isScrew()) {
				/*
					Screw M2 8mm
					Screw M3 6mm
					Nut M2
					Nut M3
					Colar M2orM3 3mm
					Spacer M3 10mm
					Spacer M3 12mm
				 */
				if("BKJ3-10".equals(number))return "Screw M3 10mm";
				if("CSH-STU-M5-40 v1".equals(number))return "CSH-STU-M5-40(Hex Screw M5 40mm)";
				if("CSPBD-ST-M3-12 v1 (1)".equals(number))return "Screw M3 12mm";
				if("CSPELH-SUS-M3-8 v2".equals(number))return "CSPELH-SUS-M3-8(LowHead Screw M3 8mm)";
				if(number.startsWith("CSPPN-ST-M3-15 "))return "Screw M3 15mm";
				if(number.startsWith("CSPPN-ST-M3-20 "))return "Screw M3 20mm";
				if(number.startsWith("CSPPN-ST-M3-30 "))return "Screw M3 30mm";
				if("Collar_M3_20 v2".equals(number))return "Colar M3 20mm";
				if("Collar_M3_3 v1".equals(number))return "Colar M3 3mm";
				if(number.startsWith("Collar_M3_5 "))return "Colar M3 5mm";
				if("Hirosugi C-520 v2".equals(number))return "Hirosugi C-520";
				if("N301_M6_TopKnob コピー1".equals(number))return "Knob M6 15mm";
				if("NKJ2_5-10 v1".equals(number))return "Screw M2.5 30mm";
				if("NKJ2_5-4 v2".equals(number))return "Screw M2.5 4mm";
				if("NKJ3-6 v1".equals(number))return "Screw M3 6mm";
				if("NKJ4-12 v1".equals(number))return "Screw M4 12mm";
				if("RubberFoot v1".equals(number))return "RubberFoot";
				if(number.startsWith("SFBJ3-10"))return "SFBJ3-10(FlatPlate Screw M3 10mm)";
				if(number.startsWith("SLBNR2_5"))return "Nut M2.5";
				if(number.startsWith("SLBNR3"))return "Nut M3";
				if(number.startsWith("SLBNR4"))return "Nut M4";
				if(number.startsWith("SLBNR5"))return "Nut M5";
				if(number.startsWith("SLBNR6"))return "Nut M6";
				if("Spacer_M2.5_12 v2".equals(number))return "Spacer M2.5 12mm";
				if("Spacer_M3_10 v2 (1)".equals(number))return "Spacer M3 10mm";
				if("Spacer_M3_15 v2 (1)".equals(number))return "Spacer M3 15mm";
				if("Spacer_M3_15 v2 (2)".equals(number))return "Spacer M3 15mm";
				if("Spacer_M3_60 v2".equals(number))return "Spacer M3 60mm";
				if("Spacer_M3_60_MF v2".equals(number))return "Spacer M3 60mm(MF)";
				if("Washer_M3 v2".equals(number))return "Washer M3";
			}
			return number;
		}
	}

}
