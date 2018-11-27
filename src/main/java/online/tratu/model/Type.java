package online.tratu.model;

public enum Type {
	EN_VI("english-vietnamese"), VI_EN("vietnamese-english"), TECHNOLOGY(null), MATH(null), CHEMISTRY(null), CONSTRUCTION(null), ECONOMIC(null), TRASMISION(null);
	
	private String uri;
	Type(String uri) {
		this.uri = uri;
	}
	
	public static Type getType(String uri) {
		for (Type item: Type.values()) {
			if (uri.contains(item.uri)) {
				return item;
			}
		}
		
		return null;
	}
}
