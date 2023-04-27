package dev.hisa.kicad.bom;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;

import dev.hisa.kicad.bom.inspector.InspectorDNP;
import dev.hisa.kicad.box.PartsBox.DuplicatePartInBox;
import dev.hisa.kicad.box.PartsBox.NotUniquePackOrDesignationException;
import dev.hisa.kicad.market.checker.AbstractMarketChecker.NoStockFoundException;
import dev.hisa.kicad.market.checker.AbstractMarketChecker.NoUnitPriceFoundException;
import dev.hisa.kicad.market.checker.MarketCheckerFactory.NoCheckerFoundException;
import dev.hisa.kicad.orm.Sheet;
import dev.hisa.kicad.poi.PoiManager;

public class BomQNinjaLite extends AbstractBom {

	static boolean separateBySheetId = false;
	BomQNinjaLite() throws StreamReadException, DatabindException, IOException, BomException {
		super(Paths.get("/Users/shingo/github/NinjaLite/kicad/NinjaLite/batch08/main/qLAMP-main.kicad_pro"), separateBySheetId);
	}
	@Override
	protected InspectorDNP getInspectorDNP() {
		return new InspectorDNP(
			"U2",
			"JLN1,JLS1",
			"J10",
			"J6",
			"J9",
			"TEXT1,TLID1,TEXT2,TAIR1,TWELL1,TEXT3,J4",
			"J13,J16",
			"J25,J24"
		);
	}

	public static void main(String[] args) throws IOException, ParseException, BomException, NotUniquePackOrDesignationException, DuplicatePartInBox, NoCheckerFoundException, NoUnitPriceFoundException, NoStockFoundException {
		Path xlsxPath = Paths.get("/Users/shingo/Desktop/qNinjaLite_" + System.currentTimeMillis() + ".xlsx");
		BomQNinjaLite bom = new BomQNinjaLite();
		PoiManager.write(xlsxPath, bom.toWorkbook());
		//System.out.println("----------------");
		//System.out.println(bom.toPartsBoxTemplate());
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
