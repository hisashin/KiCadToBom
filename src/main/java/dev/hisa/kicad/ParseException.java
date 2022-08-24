package dev.hisa.kicad;

@SuppressWarnings("serial")
public class ParseException extends Exception {

	String msg;
	
	public ParseException(String msg) {
		this.msg = msg;
	}
	
	@Override
	public String getMessage() {
		return this.msg;
	}

}
