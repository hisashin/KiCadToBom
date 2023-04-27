package dev.hisa.kicad.poi;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class PoiManager {

	public static void main(String[] args) throws IOException {
		Path path = Paths.get("/Users/shingo/Desktop/test_" + System.currentTimeMillis() + ".xlsx");
		PoiManager.write(path);
	}
	
	public static void write(Path xlsxPath) throws IOException {
		Workbook workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet();
		Row row = sheet.createRow(0);
		Cell cell = row.createCell(0);
		cell.setCellValue("test");
		write(xlsxPath, workbook);
	}

	public static void write(Path xlsxPath, Workbook workbook) throws IOException {
		OutputStream out = Files.newOutputStream(xlsxPath);
		workbook.write(out);
		workbook.close();
		System.out.println("Saved as " + xlsxPath.toAbsolutePath().toString());
	}
	
}
