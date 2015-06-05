package colors;

import ij.ImagePlus;

/**
 * @author klukas
 */
public enum ChannelExt {
	XYZ_X, XYZ_Y, XYZ_Z, Yxy_Y, Yxy_x, Yxy_y, YUV_Y, YUV_U, YUV_V, YIQ_Y, YIQ_I, YIQ_Q, YCbCr_Y, YCbCr_Cb, YCbCr_Cr, Luv_L, Luv_u, Luv_v, Lab_L, Lab_a, Lab_b, AC1C2_A, AC1C2_C1, AC1C2_C2, I1I2I3_I1, I1I2I3_T2, I1I2I3_T3, Yuv_Y, Yuv_u, Yuv_v, YQ1Q2_Y, YQ1Q2_Q1, YQ1Q2_Q2, HSI_H, HSI_S, HSI_I, HSV_H, HSV_S, HSV_V, HSL_H, HSL_S, HSL_L, HCY_H, HCY_C, HCY_Y, LCHLuv_L, LCHLuv_C, LCHLuv_H, LSHLuv_L, LSHLuv_S, LSHLuv_H, LCHLab_L, LCHLab_C, LCHLab_H, LSHLab_L, LSHLab_S, LSHLab_H, CMYK_C, CMYK_M, CMYK_Y, CMYK_K;
	// CMYKplates

	public ChannelExtSets getChannelExtSet() {
		switch (this) {
		case XYZ_X:
		case XYZ_Y:
		case XYZ_Z:
			return ChannelExtSets.XYZ;
		case Yxy_Y:
		case Yxy_x:
		case Yxy_y:
			return ChannelExtSets.Yxy;
		case YUV_Y:
		case YUV_U:
		case YUV_V:
			return ChannelExtSets.YUV;
		case YIQ_Y:
		case YIQ_I:
		case YIQ_Q:
			return ChannelExtSets.YIQ;
		case YCbCr_Y:
		case YCbCr_Cb:
		case YCbCr_Cr:
			return ChannelExtSets.YCbCr;
		case Luv_L:
		case Luv_u:
		case Luv_v:
			return ChannelExtSets.Luv;
		case Lab_L:
		case Lab_a:
		case Lab_b:
			return ChannelExtSets.Lab;
		case AC1C2_A:
		case AC1C2_C1:
		case AC1C2_C2:
			return ChannelExtSets.AC1C2;
		case I1I2I3_I1:
		case I1I2I3_T2:
		case I1I2I3_T3:
			return ChannelExtSets.I1I2I3;
		case Yuv_Y:
		case Yuv_u:
		case Yuv_v:
			return ChannelExtSets.Yuv;
		case YQ1Q2_Y:
		case YQ1Q2_Q1:
		case YQ1Q2_Q2:
			return ChannelExtSets.YQ1Q2;
		case HSI_H:
		case HSI_S:
		case HSI_I:
			return ChannelExtSets.HSI;
		case HSV_H:
		case HSV_S:
		case HSV_V:
			return ChannelExtSets.HSV;
		case HSL_H:
		case HSL_S:
		case HSL_L:
			return ChannelExtSets.HSL;
		case HCY_H:
		case HCY_C:
		case HCY_Y:
			return ChannelExtSets.HCY;
		case LCHLuv_L:
		case LCHLuv_C:
		case LCHLuv_H:
			return ChannelExtSets.LCHLuv;
		case LSHLuv_L:
		case LSHLuv_S:
		case LSHLuv_H:
			return ChannelExtSets.LSHLuv;
		case LCHLab_L:
		case LCHLab_C:
		case LCHLab_H:
			return ChannelExtSets.LCHLab;
		case LSHLab_L:
		case LSHLab_S:
		case LSHLab_H:
			return ChannelExtSets.LSHLab;
		case CMYK_C:
		case CMYK_M:
		case CMYK_Y:
		case CMYK_K:
			return ChannelExtSets.CMYK;
		default:
			throw new RuntimeException(
					"internal error, operation not supported for this channel");
		}
	}

	public ImagePlus getImage() {
		Color_Transformer_2 ct = new Color_Transformer_2();
		// todo
		return null;
	}
}
