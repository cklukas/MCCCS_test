package colors;

/**
 * @author klukas
 */
public enum RgbColorSpaceExt {
	sRGB_D65, AdobeRGB_D65, ProPhotoRGB_D50, eciRGBv2_D50, ProPhoto2_2_5500_D55;
	
	// "sRGB"
	// "Adobe RGB"
	// "ProPhoto RGB"
	// "eciRGB v2"
	// "Custom" // // ProPhoto 2.2 5500 with D55 white
	
	public String getEncoding() {
		switch (this) {
			case AdobeRGB_D65:
				return "sRGB";
			case ProPhoto2_2_5500_D55:
				return "gamma22";
			case ProPhotoRGB_D50:
				return "gamma18";
			case eciRGBv2_D50:
				return "Lstar";
			case sRGB_D65:
				return "sRGB";
			default:
				throw new RuntimeException(
						"internal error, unsupported color space");
		}
	}
	
	public String getID() {
		switch (this) {
			case AdobeRGB_D65:
				return "Adobe RGB";
			case ProPhoto2_2_5500_D55:
				return "Custom";
			case ProPhotoRGB_D50:
				return "ProPhoto RGB";
			case eciRGBv2_D50:
				return "eciRGB v2";
			case sRGB_D65:
				return "sRGB";
			default:
				throw new RuntimeException(
						"internal error, unsupported color space");
		}
	}
}
