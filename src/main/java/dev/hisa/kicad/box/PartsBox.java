package dev.hisa.kicad.box;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import dev.hisa.kicad.orm.Sheet;
import dev.hisa.kicad.orm.Symbol;

public class PartsBox {

	protected PartInBox[] getPartInBoxArray() {
		return null;
	}
	
	List<PartInBox> list = new ArrayList<PartInBox>();
	
	public PartsBox() throws DuplicatePartInBox {
		PartInBox[] array = getPartInBoxArray();
		if(array != null)
			for(PartInBox obj : array)
				add(obj);
	}
	public List<PartInBox> getList() {
		return list;
	}
	public void add(PartInBox partInBox) throws DuplicatePartInBox {
		for(PartVariant variant : partInBox.variants) {
			PartInBox part = find(variant.pack, variant.designation);
			if(part != null)
				throw new DuplicatePartInBox(variant.pack, variant.designation);
		}
		this.list.add(partInBox);
	}
	// もしDuplicatePartInBoxが投げられる場合はfindのパラメタ自体を変える必要がある
	public PartInBox find(String pack, String designation) throws DuplicatePartInBox {
		List<PartInBox> match = new ArrayList<PartInBox>();
		for(PartInBox obj : list) {
			if(obj.hasVariant(pack, designation))
				match.add(obj);
		}
		if(match.size() == 0)
			return null;
		if(match.size() == 1)
			return match.get(0);
		throw new DuplicatePartInBox(pack, designation, match);
	}
	@SuppressWarnings("serial")
	public static class DuplicatePartInBox extends Exception {
		String pack;
		String designation;
		List<PartInBox> match;
		public DuplicatePartInBox(String pack, String designation) {
			this.pack = pack;
			this.designation = designation;
		}
		DuplicatePartInBox(String pack, String designation, List<PartInBox> match) {
			this.pack = pack;
			this.designation = designation;
			this.match = match;
		}
		@Override
		public String getMessage() {
			return "Duplicate (pack, designation)=('" + pack + "', '" + designation + "')";
		}
	}
	
	public static class PartInBox {
		public String code;
		public String label;
		public String codeOld;
		public PartVariant[] variants;
		public PartInBox(String code, String label, String pack, String designation, String url, String stock) {
			this.code = code;
			this.label = label;
			this.variants = new PartVariant[] {new PartVariant(pack, designation, url, stock)};
		}
		public PartInBox(String code, String label, PartVariant... variants) {
			this.code = code;
			this.label = label;
			this.variants = variants;
		}
		boolean hasVariant(String pack, String designation) {
			for(PartVariant variant : variants)
				if(pack.equals(variant.pack) && designation.equals(variant.designation))
					return true;
			return false;
		}
	}
	public static class PartVariant {
		public String pack;
		public String designation;
		public String url;
		public String stock;
		public PartVariant(String pack, String designation, String url, String stock) {
			this.pack = pack;
			this.designation = designation;
			this.url = url;
			this.stock = stock;
		}
		@Override
		public String toString() {
			return "PartVariant[url=" + url + ", stock=" + stock + "]";
		}
	}
	public StockStatus getStatus(List<Symbol> symbols, Collection<Sheet> sheets, boolean isJellyBeans, boolean isPins, boolean isAllResistor, boolean isAllCapacitor) throws NotUniquePackOrDesignationException {
		String pack = null;
		String designation = null;
		for(Symbol symbol : symbols) {
			if(pack != null && !pack.equals(symbol.pack))
				throw new NotUniquePackOrDesignationException(symbol.designator + "'s pack " + symbol.pack + " is not equal to " + pack);
			if(designation != null && !designation.equals(symbol.designation))
				throw new NotUniquePackOrDesignationException(symbol.designator + "'s designation " + symbol.designation + " is not equal to " + designation);
			if(pack == null)
				pack = symbol.pack;
			if(designation == null)
				designation = symbol.designation;
		}
		return getStatus(pack, designation, isJellyBeans, isPins, isAllResistor, isAllCapacitor);
	}
	StockStatus getStatus(String pack, String designation, boolean isJellyBeans, boolean isPins, boolean isAllResistor, boolean isAllCapacitor) {
		StockStatus status = null;
		System.out.println("PartsBox.getStatus : pack=" + pack + ", designation=" + designation + ", isJellyBeans=" + isJellyBeans + ", isPins=" + isPins + ", isAllResistor=" + isAllResistor + ", isAllCapacitorstatus=" + isAllCapacitor + ", status= " + status);
		return status;
	}
	@SuppressWarnings("serial")
	public static class NotUniquePackOrDesignationException extends Exception {
		String msg;
		NotUniquePackOrDesignationException(String msg) {
			this.msg = msg;
		}
		@Override
		public String getMessage() {
			return this.msg;
		}
	}

}
