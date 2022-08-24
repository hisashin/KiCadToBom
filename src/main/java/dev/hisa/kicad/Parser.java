package dev.hisa.kicad;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

public class Parser {

	static Path samplePhotoDual = Paths.get("src/main/resources/sample_batch18_photo-dual.csv");
	static Path sampleMainl = Paths.get("src/main/resources/sample_batch18_qPCR-main.csv");
	
	static String digikeyClientId = System.getenv("DIGIKEY_CLIENT_ID");
	static String digikeyClientSecret = System.getenv("DIGIKEY_CLIENT_SECRET");

	public static void main(String[] args) throws IOException, ParseException {
		if(digikeyClientId == null)
			throw new ParseException("Set env DIGIKEY_CLIENT_ID");
		if(digikeyClientSecret == null)
			throw new ParseException("Set env DIGIKEY_CLIENT_SECRET");
		parse(samplePhotoDual);
		// parse(sampleMainl);
	}

	static void parse(Path bom) throws IOException {
		List<String> lines = Files.readAllLines(bom);
		System.out.println(bom.getFileName() + " has " + lines.size() + " lines.");
		for (String line : lines) {
			System.out.println(line);
		}
		List<Part> parts = new ArrayList<Part>();
		CsvMapper mapper = new CsvMapper().enable(CsvParser.Feature.IGNORE_TRAILING_UNMAPPABLE);
		CsvSchema schema = CsvSchema.emptySchema().withHeader().withColumnSeparator(';');
		MappingIterator<Part> it = mapper.readerFor(Part.class).with(schema).readValues(bom.toFile());
		parts = it.readAll();
		System.out.println("parts.size()=" + parts.size());
		for(Part part : parts)
			System.out.println(part);
	}

	public static class Part {
		@JsonProperty("Id")
		public int id;
		@JsonProperty("Designator")
		public String[] designator;
		@JsonProperty("Package")
		public String pack;
		@JsonProperty("Quantity")
		public int quantity;
		@JsonProperty("Designation")
		public String designation;
		@JsonProperty("Supplier and ref")
		public String supplier;
		
		@Override
		public String toString() {
			return "Part[id=" + id + ", designator=" + getDesignatorStr() + ", pack=" + pack + ", quantity=" + quantity + " designation=" + designation + ", supplier=" + supplier + "]";
		}
		public String getDesignatorStr() {
			StringBuffer buf = new StringBuffer();
			String sep = "";
			for(String value : designator) {
				buf.append(sep).append(value);
				sep = ",";
			}
			return buf.toString();
		}
	}

}
