package dev.hisa.kicad.stock;

import dev.hisa.kicad.orm.Footprint;
import dev.hisa.kicad.orm.Symbol;

public enum Code {

	
	a1("ADC", new Validator() {
		@Override
		public void validate(Symbol symbol, Footprint footprint) throws NotMineException {
			// TODO Auto-generated method stub
		}
	}),
	b2("3V3A生成LowNoise", new Validator() {
		@Override
		public void validate(Symbol symbol, Footprint footprint) throws NotMineException {
			// TODO Auto-generated method stub
		}
	}),
	;
	
	@SuppressWarnings("serial")
	static class NotMineException extends Exception{}
	static interface Validator {
		void validate(Symbol symbol, Footprint footprint) throws NotMineException;
	}
	
	String label;
	Validator validator;
	Code(String label, Validator validator) {
		this.label = label;
		this.validator = validator;
	}

	@SuppressWarnings("serial")
	public static class UnknownChipException extends Exception{
		public Symbol symbol;
		public Footprint footprint;
		UnknownChipException(Symbol symbol, Footprint footprint) {
			this.symbol = symbol;
			this.footprint = footprint;
		}
	}
	public static Code find(Symbol symbol, Footprint footprint) throws UnknownChipException {
		for(Code code : Code.values()) {
			try {
				code.validator.validate(symbol, footprint);
				return code;
			} catch (NotMineException e) {}
		}
		throw new UnknownChipException(symbol, footprint);
	}
}
