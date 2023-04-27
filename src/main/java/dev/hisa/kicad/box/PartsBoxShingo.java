package dev.hisa.kicad.box;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PartsBoxShingo extends PartsBox {

	static Path jsonPath = Paths.get("src/main/resources/partsBoxShingo.json");
	
	static PartsBoxShingo instance = null;
	public static PartsBoxShingo getInstance() throws DuplicatePartInBox {
		if(instance == null)instance = new PartsBoxShingo();
		return instance;
	}
	
	PartsBoxShingo() throws DuplicatePartInBox {
		super(jsonPath);
	}

}
