package colors;

import ij.ImagePlus;
import de.ipk.ag_ba.image.structures.Image;

/**
 * @author klukas
 */
public class ChannelProcessingExt {
	private float[] r01;
	private float[] g01;
	private float[] b01;
	private int w;
	private int h;
	
	public ChannelProcessingExt(int w, int h, float[] r01, float[] g01, float[] b01) {
		this.w = w;
		this.h = h;
		this.r01 = r01;
		this.g01 = g01;
		this.b01 = b01;
	}
	
	public ImagePlus[] getImage(RgbColorSpaceExt rgbSourceColorSpace, ColorSpaceExt colorSpaceExt) {
		Color_Transformer_2 ct = new Color_Transformer_2();
		ct.setUp(rgbSourceColorSpace.getID(), colorSpaceExt.getID(),
				w, h, r01, g01, b01);
		switch (colorSpaceExt) {
			case AC1C2:
				ct.getAC1C2();
				break;
			case CMYK:
				ct.getCMYK();
				break;
			case HCY:
				ct.getHCY();
				break;
			case HSI:
				ct.getHSI();
				break;
			case HSL:
				ct.getHSL();
				break;
			case HSV:
				ct.getHSV();
				break;
			case I1I2I3:
				ct.getI1I2I3();
				break;
			case LCHLab:
				ct.getLCHLab();
				break;
			case LCHLuv:
				ct.getLCHLuv();
				break;
			case LSHLab:
				ct.getLSHLab();
				break;
			case LSHLuv:
				ct.getLSHLuv();
				break;
			case Lab:
				ct.getLab();
				break;
			case Luv:
				ct.getLuv();
				break;
			case XYZ:
				ct.getXYZ();
				break;
			case YCbCr:
				ct.getYCbCr();
				break;
			case YIQ:
				ct.getYIQ();
				break;
			case YQ1Q2:
				ct.getYQ1Q2();
				break;
			case YUV:
				ct.getYUV();
				break;
			case Yuv:
				ct.getYuv();
				break;
			case Yxy:
				ct.getYxy();
				break;
			default:
				throw new RuntimeException(
						"internal error, unsupported color space");
		}
		ImagePlus imc1 = new Image(w, h, ct.c1).getAsImagePlus();
		ImagePlus imc2 = new Image(w, h, ct.c2).getAsImagePlus();
		ImagePlus imc3 = new Image(w, h, ct.c3).getAsImagePlus();
		
		if (colorSpaceExt == ColorSpaceExt.CMYK) {
			ImagePlus imc4 = new Image(w, h, ct.c4).getAsImagePlus();
			
			return new ImagePlus[] { imc1, imc2, imc3, imc4 };
		} else {
			return new ImagePlus[] { imc1, imc2, imc3 };
		}
	}
}
