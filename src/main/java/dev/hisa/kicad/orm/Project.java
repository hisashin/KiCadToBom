package dev.hisa.kicad.orm;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonIgnoreProperties(ignoreUnknown=true)
public class Project {

	private Project() {}
	public static Project getInstance(Path path) throws StreamReadException, DatabindException, IOException {
		System.out.println("Reading " + path.getFileName());
		//print(path);
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(Files.readAllBytes(path), Project.class);
	}
	
	@JsonProperty("meta")
	public Meta meta;
	@JsonProperty("sheets")
	public String[][] sheetsTmp;
	
	@JsonIgnoreProperties(ignoreUnknown=true)
	public static class Meta {
		@JsonProperty("filename")
		String filename;
	}
	
	public String getFilenameWithoutExtension() {
		return this.meta.filename.replaceAll(".kicad_pro", "");
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("Project[");
		if(meta != null)
			buf.append("meta.filename=").append(meta.filename).append(", ");
		if(sheetsTmp != null) {
			buf.append("sheets=[");
			for(String[] sheet : sheetsTmp) {
				buf.append("{");
				for(String str : sheet)
					buf.append(str).append(",");
				buf.append("}");
			}
			buf.append("]");
		}
		buf.append("]");
		return buf.toString();
	}
}
