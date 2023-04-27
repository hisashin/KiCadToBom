package dev.hisa.kicad.box;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.hisa.kicad.orm.Sheet;
import dev.hisa.kicad.orm.Symbol;

public class PartsBox {

	Map<String, PartInBox> map = new TreeMap<String, PartInBox>();
	
	protected PartsBox(Path jsonPath) {
		try {
			String json = new String(Files.readAllBytes(jsonPath));
			//System.out.println(json);
			ObjectMapper mapper = new ObjectMapper();
			PartsInBoxContainer container = mapper.readValue(json, PartsInBoxContainer.class);
			//System.out.println("container.size()=" + container.size());
			for(PartInBox obj : container) {
				for(PartVariant variant : obj.variants) {
					variant.clean();
				}
				map.put(obj.code, obj);
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@SuppressWarnings("serial")
	public static class PartsInBoxContainer extends ArrayList<PartInBox> {}
	public Collection<PartInBox> getPartList() {
		return map.values();
	}
	public List<String> getUrlList() {
		List<String> list = new ArrayList<String>();
		for(PartInBox part : getPartList()) {
			for(PartVariant variant : part.variants) {
				if(variant.url == null || list.contains(variant.url))continue;
				list.add(variant.url);
			}
		}
		return list;
	}
	public void add(PartInBox partInBox) throws DuplicatePartInBox {
		for(PartVariant variant : partInBox.variants) {
			FindResponse response = find(variant.pack, variant.designation);
			if(response != null)
				throw new DuplicatePartInBox(variant.pack, variant.designation);
		}
		map.put(partInBox.code, partInBox);
	}
	public static class FindResponse {
		public PartInBox partInBox;
		public PartVariant variant;
		FindResponse(PartInBox partInBox, PartVariant variant) {
			this.partInBox = partInBox;
			this.variant = variant;
		}
	}
	// もしDuplicatePartInBoxが投げられる場合はfindのパラメタ自体を変える必要がある
	public FindResponse find(String pack, String designation) throws DuplicatePartInBox {
		//System.out.println("PartsBox.find : pack=" + pack + ", designation=" + designation);
		List<FindResponse> match = new ArrayList<FindResponse>();
		for(PartInBox obj : map.values()) {
			//System.out.println("PartsBox.find : obj.code=" + obj.code);
			PartVariant variant = obj.getVariant(pack, designation);
			if(variant != null)
				match.add(new FindResponse(obj, variant));
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
		List<FindResponse> match;
		public DuplicatePartInBox(String pack, String designation) {
			this.pack = pack;
			this.designation = designation;
		}
		DuplicatePartInBox(String pack, String designation, List<FindResponse> match) {
			this.pack = pack;
			this.designation = designation;
			this.match = match;
		}
		@Override
		public String getMessage() {
			return "Duplicate (pack, designation)=('" + pack + "', '" + designation + "')";
		}
	}
	
	public static enum Type {
		DIP,
		SMD
		;
	}
	public static class PartInBox {
		public String code;
		public String label;
		public String codeOld;
		public Type type;
		public PartVariant[] variants;
		PartInBox(){}
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
		PartVariant getVariant(String pack, String designation) {
			for(PartVariant variant : variants) {
				if(!pack.equals(variant.pack) || !designation.equals(variant.designation))continue;
				return variant;
			}
			return null;
		}
	}
	public static class PartVariant {
		public String pack;
		public String designation;
		public String url;
		public String stock;
		PartVariant(){}
		
		public void clean() {
			if(url != null) {
				url = url.replace("www.digikey.jp/ja/", "www.digikey.com/en/");
				url = url.replace("www.digikey.com/ja/", "www.digikey.com/en/");
			}
		}
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
