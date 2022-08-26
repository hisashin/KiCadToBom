package dev.hisa.kicad.bom.inspector;

import java.util.regex.Pattern;

public class InspectorJumper {

	static Pattern patternPackJumper = Pattern.compile("^Jumper:");
	
	public InspectorJumper(String pack) throws InspectorJumperException {
		if(pack != null && patternPackJumper.matcher(pack).find())
			throw new InspectorJumperException();
	}
	@SuppressWarnings("serial")
	public static class InspectorJumperException extends InspectorException {}
}
