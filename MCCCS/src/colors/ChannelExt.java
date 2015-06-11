package colors;

import org.StringManipulationTools;

/**
 * @author klukas
 */
public enum ChannelExt {
	RGB_R, RGB_G, RGB_B, XYZ_X, XYZ_Y, XYZ_Z, Yxy_Y, Yxy_x, Yxy_y, YUV_Y, YUV_U, YUV_V, YIQ_Y, YIQ_I, YIQ_Q, YCbCr_Y, YCbCr_Cb, YCbCr_Cr, Luv_L, Luv_u, Luv_v, Lab_L, Lab_a, Lab_b, AC1C2_A, AC1C2_C1, AC1C2_C2, I1I2I3_I1, I1I2I3_T2, I1I2I3_T3, Yuv_Y, Yuv_u, Yuv_v, YQ1Q2_Y, YQ1Q2_Q1, YQ1Q2_Q2, HSI_H, HSI_S, HSI_I, HSV_H, HSV_S, HSV_V, HSL_H, HSL_S, HSL_L, HCY_H, HCY_C, HCY_Y, LCHLuv_L, LCHLuv_C, LCHLuv_H, LSHLuv_L, LSHLuv_S, LSHLuv_H, LCHLab_L, LCHLab_C, LCHLab_H, LSHLab_L, LSHLab_S, LSHLab_H, CMYK_C, CMYK_M, CMYK_Y, CMYK_K;
	// CMYKplates

	// public ChannelExt()

	@Override
	public String toString() {
		String r = this.name();
		r = r.toLowerCase();
		r = r.replace('_', '.');
		return r;
	}

	public ColorSpaceExt getColorSpace() {
		switch (this) {
		case RGB_R:
		case RGB_G:
		case RGB_B:
			return ColorSpaceExt.RGB;
		case XYZ_X:
		case XYZ_Y:
		case XYZ_Z:
			return ColorSpaceExt.XYZ;
		case Yxy_Y:
		case Yxy_x:
		case Yxy_y:
			return ColorSpaceExt.Yxy;
		case YUV_Y:
		case YUV_U:
		case YUV_V:
			return ColorSpaceExt.YUV;
		case YIQ_Y:
		case YIQ_I:
		case YIQ_Q:
			return ColorSpaceExt.YIQ;
		case YCbCr_Y:
		case YCbCr_Cb:
		case YCbCr_Cr:
			return ColorSpaceExt.YCbCr;
		case Luv_L:
		case Luv_u:
		case Luv_v:
			return ColorSpaceExt.Luv;
		case Lab_L:
		case Lab_a:
		case Lab_b:
			return ColorSpaceExt.Lab;
		case AC1C2_A:
		case AC1C2_C1:
		case AC1C2_C2:
			return ColorSpaceExt.AC1C2;
		case I1I2I3_I1:
		case I1I2I3_T2:
		case I1I2I3_T3:
			return ColorSpaceExt.I1I2I3;
		case Yuv_Y:
		case Yuv_u:
		case Yuv_v:
			return ColorSpaceExt.Yuv;
		case YQ1Q2_Y:
		case YQ1Q2_Q1:
		case YQ1Q2_Q2:
			return ColorSpaceExt.YQ1Q2;
		case HSI_H:
		case HSI_S:
		case HSI_I:
			return ColorSpaceExt.HSI;
		case HSV_H:
		case HSV_S:
		case HSV_V:
			return ColorSpaceExt.HSV;
		case HSL_H:
		case HSL_S:
		case HSL_L:
			return ColorSpaceExt.HSL;
		case HCY_H:
		case HCY_C:
		case HCY_Y:
			return ColorSpaceExt.HCY;
		case LCHLuv_L:
		case LCHLuv_C:
		case LCHLuv_H:
			return ColorSpaceExt.LCHLuv;
		case LSHLuv_L:
		case LSHLuv_S:
		case LSHLuv_H:
			return ColorSpaceExt.LSHLuv;
		case LCHLab_L:
		case LCHLab_C:
		case LCHLab_H:
			return ColorSpaceExt.LCHLab;
		case LSHLab_L:
		case LSHLab_S:
		case LSHLab_H:
			return ColorSpaceExt.LSHLab;
		case CMYK_C:
		case CMYK_M:
		case CMYK_Y:
		case CMYK_K:
			return ColorSpaceExt.CMYK;
		default:
			throw new RuntimeException("internal error, operation not supported for this channel");
		}
	}

	public String getID() {
		if (this == RGB_R || this == RGB_G || this == RGB_B) {
			String r = StringManipulationTools.stringReplace(StringManipulationTools.stringReplace(this.name(), "_", "."), "RGB.", "RedGreenBlue.");
			return r;
		} else
			return StringManipulationTools.stringReplace(this.name(), "_", ".");
	}
}
