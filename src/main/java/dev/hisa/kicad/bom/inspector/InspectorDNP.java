package dev.hisa.kicad.bom.inspector;

import java.util.ArrayList;
import java.util.List;

public class InspectorDNP {

	List<String> ignoreDesignators;
	
	public InspectorDNP(String... ignoreDesignators) {
		List<String> list = new ArrayList<String>();
		for(String designator : ignoreDesignators)
			list.add(designator);
		this.ignoreDesignators = list;
	}
	public void validate(String designators, String designation) throws InspectorDNPException {
		if(designation != null && "DNP".equals(designation))
			throw new InspectorDNPException();
		if(designators != null && ignoreDesignators.contains(designators))
			throw new InspectorDNPException();
	}
	@SuppressWarnings("serial")
	public static class InspectorDNPException extends InspectorException {}
}
