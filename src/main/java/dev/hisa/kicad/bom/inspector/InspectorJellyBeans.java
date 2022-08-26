package dev.hisa.kicad.bom.inspector;

public class InspectorJellyBeans {

	public InspectorJellyBeans(String pack) throws InspectorJellyBeansException {
		if(pack == null)return;
		if(pack.startsWith("Ninja-qPCR:Raspberry_Pi")
				|| pack.startsWith("Connector_Hirose:Hirose_")
				|| "Ninja-qPCR:TB_SeeedOPL_320110028".equals(pack)
				|| "Ninja-qPCR:FFC_60_Ali_HUISHUNFA".equals(pack)
				|| "Ninja-qPCR:ATX8".equals(pack)
				|| "Ninja-qPCR:EP2-3L3SAb".equals(pack)
				|| "Ninja-qPCR:SOT95P240X112-3N".equals(pack)
				|| "Ninja-qPCR:LITE_LCD".equals(pack)
				|| "Connector_BarrelJack:BarrelJack_Horizontal".equals(pack)
			)
			throw new InspectorJellyBeansException();
	}
	@SuppressWarnings("serial")
	public static class InspectorJellyBeansException extends InspectorException {}
}
