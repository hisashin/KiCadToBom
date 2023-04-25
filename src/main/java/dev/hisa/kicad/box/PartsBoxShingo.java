package dev.hisa.kicad.box;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PartsBoxShingo extends PartsBox {

	static Path jsonPath = Paths.get("src/main/resources/partsBoxShingo.json");
	
	static PartsBoxShingo instance = null;
	public static PartsBoxShingo getInstance() throws DuplicatePartInBox {
		if(instance == null)instance = new PartsBoxShingo();
		return instance;
	}
	
	Map<String, PartInBox> map = new TreeMap<String, PartInBox>();
	
	PartsBoxShingo() throws DuplicatePartInBox {
		super();
		load();
	}
	void load() {
		try {
			String json = new String(Files.readAllBytes(jsonPath));
			System.out.println(json);
			ObjectMapper mapper = new ObjectMapper();
			PartsInBoxContainer container = mapper.readValue(json, PartsInBoxContainer.class);
			System.out.println("container.size()=" + container.size());
			for(PartInBox obj : container)
				map.put(obj.code, obj);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@SuppressWarnings("serial")
	public static class PartsInBoxContainer extends ArrayList<PartInBox> {}

}
