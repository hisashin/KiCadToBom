package dev.hisa.kicad.bom;

import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;

import dev.hisa.kicad.orm.Sheet;

public class BomQNinja extends AbstractBom {

	static boolean separateBySheetId = false;
	BomQNinja() throws StreamReadException, DatabindException, IOException, BomException {
		super(Paths.get("/Users/shingo/github/Ninja/kicad/Ninja/batch19/main/qPCR-main.kicad_pro"), separateBySheetId);
	}

	public static void main(String[] args) throws IOException, ParseException, BomException {
		BomQNinja bom = new BomQNinja();
		/*
		System.out.println("----------------");
		for(Sheet sheet : project.mapSheet.values())
			System.out.println("@" + sheet.id + " : " + sheet.label);
		System.out.println("----------------");
		for(String sheetId : project.mapFootprints.keySet()) {
			System.out.println("@" + sheetId);
			for(Footprint footprint : project.mapFootprints.get(sheetId))
				System.out.println(" " + footprint);
		}
		System.out.println("----------------");
		for(MapSymbolsKey key : project.mapSymbols.keySet()) {
			System.out.println("@" + key.pack + " / " + key.designation);
			for(Symbol symbol : project.mapSymbols.get(key))
				System.out.println(" " + symbol);
		}
		*/
		System.out.println("----------------");
		System.out.println(bom.toString());
		System.out.println("----------------");
		System.out.println(bom.getFootprints().size() + " footprints and " + bom.getSymbols().size() + " symbols found");
	}
	
	@Override
	protected void validateJellyBeans(String pack, String designation, Sheet sheet) throws InspectorJellyBeansException {
		super.validateJellyBeans(pack, designation, sheet);
	}
}
