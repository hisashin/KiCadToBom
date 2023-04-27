package dev.hisa.kicad.poi;

import java.io.IOException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.fasterxml.jackson.core.JsonProcessingException;

import dev.hisa.kicad.bom.BomLine;
import dev.hisa.kicad.box.PartsBox;
import dev.hisa.kicad.box.PartsBox.DuplicatePartInBox;
import dev.hisa.kicad.box.PartsBox.FindResponse;
import dev.hisa.kicad.market.MarketManager;
import dev.hisa.kicad.market.MarketResult;
import dev.hisa.kicad.market.checker.AbstractMarketChecker.NoStockFoundException;
import dev.hisa.kicad.market.checker.AbstractMarketChecker.NoUnitPriceFoundException;
import dev.hisa.kicad.market.checker.MarketCheckerFactory.NoCheckerFoundException;

public class PoiBom {
	
	public Workbook workbook;
	org.apache.poi.ss.usermodel.Sheet sheet;
	PartsBox partsBox;
	int rowIndex = 0;
	
	public PoiBom(PartsBox partsBox, String sheetName) {
		this.workbook = new XSSFWorkbook();
		this.sheet = this.workbook.createSheet(sheetName);
		this.partsBox = partsBox;
	}
	int addRow(String... values) {
		Row row = sheet.createRow(rowIndex);
		for(int i = 0; i < values.length ; i++) {
			Cell cell = row.createCell(i);
			cell.setCellValue(values[i]);
		}
		rowIndex++;
		return rowIndex - 1;
	}
	public int addHeader() {
		return addRow("designators",
			"Q",
			"pack",
			"designation",
			"sheets",
			"code",
			"type",
			"function",
			"stockAtHome",
			"label",
			"market",
			"stockInMarket",
			"datasheet",
			"unitPrice"
		);
	}
	public int addBar(String title) {
		return addRow(title);
	}
	public int addLine(BomLine line) throws DuplicatePartInBox, NoCheckerFoundException, NoUnitPriceFoundException, NoStockFoundException, JsonProcessingException, IOException {
		FindResponse response = partsBox == null ? null : partsBox.find(line.key.pack, line.key.designation);
		MarketResult result = response == null ? null : MarketManager.getInstance().add(response.variant.url, false);
		return addRow(
			line.designators.toString(),
			Integer.toString(line.subList.size()),
			line.key.pack,
			line.key.designation,
			line.getSheetNames().toString(),
			response == null ? "" : response.partInBox.code,
			response == null ? "" : response.partInBox.type.toString(),
			response == null ? "" : response.partInBox.label,
			response == null ? "" : response.variant.stock,
			result == null ? "" : result.label,
			result == null ? "" : result.market.toString(),
			result == null ? "" : Integer.toString(result.stock),
			result == null ? "" : result.datasheet,
			result == null ? "" : Double.toString(result.unitPriceFor100)
		);
	}
}