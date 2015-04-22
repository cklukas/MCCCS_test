package tools;

enum MacroBotOutputTypes implements ImageTypes_MacroBot {
	
	R, G, B, UV;
	
	@Override
	public String getNiceName() {
		switch (this) {
			case B:
				return "blue";
			case G:
				return "green";
			case R:
				return "red";
			case UV:
				return "uv";
			default:
				return "";
		}
	}
}