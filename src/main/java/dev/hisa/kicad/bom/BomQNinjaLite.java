package dev.hisa.kicad.bom;

import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;

import dev.hisa.kicad.box.PartsBox.DuplicatePartInBox;
import dev.hisa.kicad.box.PartsBox.NotUniquePackOrDesignationException;
import dev.hisa.kicad.orm.Sheet;

public class BomQNinjaLite extends AbstractBom {

	static boolean separateBySheetId = false;
	BomQNinjaLite() throws StreamReadException, DatabindException, IOException, BomException {
		super(Paths.get("/Users/shingo/github/NinjaLite/kicad/NinjaLite/batch07/main/qLAMP-main.kicad_pro"), separateBySheetId);
	}

	public static void main(String[] args) throws IOException, ParseException, BomException, NotUniquePackOrDesignationException, DuplicatePartInBox {
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
		System.out.println(bom.toPartsBoxTemplate());
		System.out.println("----------------");
		System.out.println(bom.toString());
		System.out.println("----------------");
		System.out.println(bom.getFootprints().size() + " footprints and " + bom.getSymbols().size() + " symbols found");
	}

	@Override
	protected String[] getCustomJellyBeansDesignationStartsWith() {
		return new String[] {
		};
	}
	@Override
	protected void validateJellyBeans(String pack, String designation, Sheet sheet) throws InspectorJellyBeansException {
		super.validateJellyBeans(pack, designation, sheet);
		//if ("Package_TO_SOT_SMD:SOT-23-5".equals(pack) && "Power".equals(sheet.label))
		//	throw new InspectorJellyBeansException();
	}
}
