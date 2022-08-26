package dev.hisa.kicad.bom.inspector;

import java.util.regex.Pattern;

public class InspectorPins {

	static Pattern patternPackPins1 = Pattern.compile("^Connector_Pin(Header|Socket)_");
	static Pattern patternPackPins2 = Pattern.compile("^Pin_(Header|Socket)s:");
	
	public InspectorPins(String pack) throws InspectorPinsException {
		if(pack == null)return;
		if(patternPackPins1.matcher(pack).find() || patternPackPins2.matcher(pack).find())
			throw new InspectorPinsException();
	}
	@SuppressWarnings("serial")
	public static class InspectorPinsException extends InspectorException {}
}
