package colors;

/**
 * @author klukas
 */
public enum ColorSpaceExt {
	XYZ, Yxy, YUV, YIQ, YCbCr, Luv, Lab, AC1C2, I1I2I3, Yuv, YQ1Q2, HSI, HSV, HSL, HCY, LCHLuv, LSHLuv, LCHLab, LSHLab, CMYK;
	// CMYK plates
	
	public ChannelExt[] getChannels() {
		switch (this) {
			case XYZ:
				return new ChannelExt[] { ChannelExt.HSI_H, ChannelExt.HSI_S,
						ChannelExt.HSI_I };
			case AC1C2:
				return new ChannelExt[] { ChannelExt.AC1C2_A, ChannelExt.AC1C2_C1,
						ChannelExt.AC1C2_C2 };
			case CMYK:
				return new ChannelExt[] { ChannelExt.CMYK_C, ChannelExt.CMYK_M,
						ChannelExt.CMYK_Y, ChannelExt.CMYK_K };
			case HCY:
				return new ChannelExt[] { ChannelExt.HCY_H, ChannelExt.HCY_C,
						ChannelExt.HCY_Y };
			case HSI:
				return new ChannelExt[] { ChannelExt.HSI_H, ChannelExt.HSI_S,
						ChannelExt.HSI_I };
			case HSV:
				return new ChannelExt[] { ChannelExt.HSV_H, ChannelExt.HSV_S,
						ChannelExt.HSV_V };
			case HSL:
				return new ChannelExt[] { ChannelExt.HSL_H, ChannelExt.HSL_S,
						ChannelExt.HSL_L };
			case I1I2I3:
				return new ChannelExt[] { ChannelExt.I1I2I3_I1,
						ChannelExt.I1I2I3_T2, ChannelExt.I1I2I3_T3 };
			case LCHLab:
				return new ChannelExt[] { ChannelExt.LCHLab_L, ChannelExt.LCHLab_C,
						ChannelExt.LCHLab_H };
			case LCHLuv:
				return new ChannelExt[] { ChannelExt.LCHLuv_L, ChannelExt.LCHLuv_C,
						ChannelExt.LCHLuv_H };
			case LSHLab:
				return new ChannelExt[] { ChannelExt.LSHLab_L, ChannelExt.LSHLab_S,
						ChannelExt.LSHLab_H };
			case LSHLuv:
				return new ChannelExt[] { ChannelExt.LSHLuv_L, ChannelExt.LSHLuv_S,
						ChannelExt.LSHLuv_H };
			case Luv:
				return new ChannelExt[] { ChannelExt.Luv_L, ChannelExt.Luv_u,
						ChannelExt.Luv_v };
			case Lab:
				return new ChannelExt[] { ChannelExt.Lab_L, ChannelExt.Lab_a,
						ChannelExt.Lab_b };
			case YCbCr:
				return new ChannelExt[] { ChannelExt.YCbCr_Y, ChannelExt.YCbCr_Cb,
						ChannelExt.YCbCr_Cr };
			case YIQ:
				return new ChannelExt[] { ChannelExt.YIQ_Y, ChannelExt.YIQ_I,
						ChannelExt.YIQ_Q };
			case YQ1Q2:
				return new ChannelExt[] { ChannelExt.YQ1Q2_Y, ChannelExt.YQ1Q2_Q1,
						ChannelExt.YQ1Q2_Q2 };
			case YUV:
				return new ChannelExt[] { ChannelExt.YUV_Y, ChannelExt.YUV_U,
						ChannelExt.YUV_Y };
			case Yuv:
				return new ChannelExt[] { ChannelExt.Yuv_Y, ChannelExt.Yuv_u,
						ChannelExt.Yuv_v };
			case Yxy:
				return new ChannelExt[] { ChannelExt.Yxy_Y, ChannelExt.Yxy_x,
						ChannelExt.Yxy_y };
			default:
				throw new RuntimeException(
						"internal error, operation not supported for this channel set");
				
		}
	}
	
	public String getID() {
		switch (this) {
			case XYZ:
				return "XYZ";
			case Yxy:
				return "Yxy";
			case YUV:
				return "YUV";
			case YIQ:
				return "YIQ";
			case YCbCr:
				return "YCbCr";
			case Luv:
				return "Luv";
			case Lab:
				return "Lab";
			case AC1C2:
				return "AC1C2";
			case I1I2I3:
				return "I1I2I3";
			case Yuv:
				return "Yuv";
			case YQ1Q2:
				return "YQ1Q2";
			case HSI:
				return "HSI";
			case HSV:
				return "HSV/HSB";
			case HSL:
				return "HSL";
			case HCY:
				return "HCY";
			case LCHLuv:
				return "LCHLuv";
			case LSHLuv:
				return "LSHLuv";
			case LCHLab:
				return "LCHLab";
			case LSHLab:
				return "LSHLab";
			case CMYK:
				return "CMYK";
				// "CMYK plates"
			default:
				throw new RuntimeException(
						"internal error, operation not supported for this channel set");
		}
	}
}
