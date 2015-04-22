package tools;

enum Masks implements ImageTypes_MacroBot {
	
	ROI, FOREGROUND;
	
	@Override
	public String getNiceName() {
		switch (this) {
			case ROI:
				return "Borders";
			case FOREGROUND:
				return "Foreground";
			default:
				return "";
		}
	}
}