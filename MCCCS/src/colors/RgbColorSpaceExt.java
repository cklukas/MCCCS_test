package colors;

/**
 * @author klukas
 */
public enum RgbColorSpaceExt {
	sRGB_D65, AdobeRGB_D65, ProPhotoRGB_D50, eciRGBv2_D50, ProPhoto2_2_5500_D55;
	
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
	
	private float[] D50 = { 96.422f, 100.0f, 82.521f };
	private float[] D55 = { 95.682f, 100.0f, 92.149f };
	private float[] D65 = { 95.047f, 100.0f, 108.883f };
	
	public float[] getWhitePoint() {
		switch (this) {
			case AdobeRGB_D65:
				return D65;
			case ProPhoto2_2_5500_D55:
				return D55;
			case ProPhotoRGB_D50:
				return D50;
			case eciRGBv2_D50:
				return D50;
			case sRGB_D65:
				return D65;
			default:
				throw new RuntimeException(
						"internal error, unsupported color space");
		}
	}
	
	public String getID() {
		return getEncoding();
	}
}
