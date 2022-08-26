package dev.hisa.kicad.orm;

public class Sheet {

	public String id;
	public String filename;
	public String label;
	public boolean top;
	
	public static final String getDefaultLabel() { return "Top"; }
	
	public Sheet(String id, String label) {
		this.id = id;
		if(label == null || label.length() <= 0) {
			this.top = true;
			this.label = getDefaultLabel();
		} else {
			this.top = false;
			this.label = label;
		}
	}
	
	@Override
	public String toString() {
		return "Sheet[id=" + id + ", label=" + label + "]";
	}

}
