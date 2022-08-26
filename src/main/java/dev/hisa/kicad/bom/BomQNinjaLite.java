package dev.hisa.kicad.bom;

import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;

public class BomQNinjaLite extends AbstractBom {

	BomQNinjaLite() throws StreamReadException, DatabindException, IOException {
		super(Paths.get("/Users/shingo/github/NinjaLite/kicad/NinjaLite/batch03/main-pdb/qLAMP-main.kicad_pro"));
	}

	public static void main(String[] args) throws IOException, ParseException {
		BomQNinjaLite bom = new BomQNinjaLite();
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
		System.out.println(bom.toString(true, true, true, true));
		System.out.println("----------------");
		System.out.println(bom.getFootprints().size() + " footprints and " + bom.getSymbols().size() + " symbols found");
	}
	
}
