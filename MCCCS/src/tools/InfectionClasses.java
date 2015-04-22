package tools;

enum InfectionClasses implements ImageTypes_MacroBot {
	
	RGBplusUV, UV, BROWN, NONDISEASE;
	
	@Override
	public String getNiceName() {
		switch (this) {
			case BROWN:
				return "Brown_Spots";
			case RGBplusUV:
				return "RGB_Visible";
			case UV:
				return "UV_Visible";
			case NONDISEASE:
				return "Non_Disease";
			default:
				return "";
		}
	}
}