package colors;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

/**
 * Color_Transformer.java Converts an RGB colour image into a colour space
 * represented by a stack
 *
 * @author Maria E. Barilla-Perez
 * @author Birmingham University, United Kingdom
 * @author barillame@yahoo.co.uk and perezm@eee.bham.ac.uk
 * @version 2.02 RWCottrell modifications 2014-Jan-25: The original plugin used
 *          sRGB as the RGB color space. The revision by WAHeeschen normalized
 *          L*a*b* to the D65 white reference. I have added Adobe RGB, ProPhoto
 *          RGB, and eciRGB v2. Applies to XYZ, Yxy, Luv, Lab, LCHLuv, LSHLuv,
 *          LCHLab. L* white references are specified by the RGB-XYZ conversion
 *          matrix. Applies to Luv, Lab, LCHLuv, LSHLuv, LCHLab. Custom RGB may
 *          be added by entering the encoding, matrix, and white reference
 *          below; some examples are included. Added YCbCr and two CMYK
 *          conversions. Added optional (recommended) conversion to RGB color
 *          and full 16-bit range display. Corrected some of the channel labels
 *          that appear to have been incorrect (such as VHS for HSV). Comments
 *          include my initials RWC. Inspiration taken from
 *          Color_Space_Converter, www.brucelindbloom.com, and
 *          www.equasys.de/colorconversion.html. More information at
 *          www.russellcottrell.com/photo. 2014-Jul-18: Corrected the label for
 *          LCHLab from LSHLab. 2014-Aug-3: Corrected the hue calculations for
 *          LCHLuv, LSHLuv, LCHLab. Added HCY and LSHLab color spaces.
 */

public class Color_Transformer_2 implements PlugInFilter {
	private ImagePlus imp; // Original image
	private ImageStack sstack; // Stack result
	private String title; // Name of the original image
	private int width; // Width of the original image
	private int height; // Height of the original image
	private int size; // Total number of pixels
	private float[] rf, gf, bf; // r, g, b values
	public float[] c1, c2, c3, c4; // Colour space values
	private int[] d1, d2, d3, d4; // Integer values for CMYK plates
	public String n1, n2, n3, n4; // Names for every layer on the stack
	private String RGBcolourspace; // RGB colour space chosen
	private String colourspace; // Destination colour space chosen
	private float[] Lwhite; // L* white reference
	private boolean toRGBcolor = true; // Convert to RGB color (8 bits per
	// channel)
	private boolean full16 = true; // Display the full 16-bit range (0-65535)

	/**
	 * @author klukas
	 */
	public void setUp(String RGBcolourspace, String targetColorSpace, int w, int h, float[] r01, float[] g01, float[] b01) {
		this.RGBcolourspace = RGBcolourspace;
		this.colourspace = targetColorSpace;
		width = w;
		height = h;
		size = width * height;
		rf = r01;
		gf = g01;
		bf = b01;
		c1 = new float[size];
		c2 = new float[size];
		c3 = new float[size];
		c4 = new float[size];
	}

	@Override
	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		if (arg.equals("about")) {
			showAbout();
			return DONE;
		}
		return DOES_ALL;
	}

	public boolean showDialog() {

		String[] RGBcolourspaces = { "sRGB", "Adobe RGB", "ProPhoto RGB", "eciRGB v2", "Custom" };

		String[] colourspaces = { "XYZ", "Yxy", "YUV", "YIQ", "YCbCr", "Luv", "Lab", "AC1C2", "I1I2I3", "Yuv", "YQ1Q2", "HSI", "HSV/HSB", "HSL", "HCY", "LCHLuv", "LSHLuv",
				"LCHLab", "LSHLab", "CMYK", "CMYK plates" };

		GenericDialog gd = new GenericDialog("Color Transformer 2");
		gd.addChoice("From color space:", RGBcolourspaces, RGBcolourspaces[0]);
		gd.addChoice("To color space:", colourspaces, colourspaces[0]);
		gd.addCheckbox(" Convert images to RGB color (8 bpc)", toRGBcolor);
		gd.addCheckbox(" Display full 16-bit range (0-65535)", full16);
		gd.showDialog();
		if (gd.wasCanceled())
			return false;
		RGBcolourspace = RGBcolourspaces[gd.getNextChoiceIndex()];
		colourspace = colourspaces[gd.getNextChoiceIndex()];
		toRGBcolor = gd.getNextBoolean();
		full16 = gd.getNextBoolean();
		return true;
	}

	@Override
	public void run(ImageProcessor ip) {
		if (!showDialog())
			return;
		int offset, i;

		if ((imp.getBitDepth() == 16) && full16) {
			imp.setSlice(3);
			IJ.setMinAndMax(imp, 0, 65535);
			imp.setSlice(2);
			IJ.setMinAndMax(imp, 0, 65535);
			imp.setSlice(1);
			IJ.setMinAndMax(imp, 0, 65535);
		}

		if ((imp.getBitDepth() != 24) && toRGBcolor) {
			imp.unlock();
			IJ.run(imp, "RGB Color", "");
			imp.lock();
			ImagePlus imp2 = WindowManager.getCurrentImage();
			ip = imp2.getProcessor();
		}

		width = ip.getWidth();
		height = ip.getHeight();
		size = width * height;
		rf = new float[size];
		gf = new float[size];
		bf = new float[size];
		c1 = new float[size];
		c2 = new float[size];
		c3 = new float[size];
		c4 = new float[size];

		for (int row = 0; row < height; row++) {
			offset = row * width;
			for (int col = 0; col < width; col++) {
				i = offset + col;
				int c = ip.getPixel(col, row);
				rf[i] = ((c & 0xff0000) >> 16) / 255f; // R 0..1
				gf[i] = ((c & 0x00ff00) >> 8) / 255f; // G 0..1
				bf[i] = (c & 0x0000ff) / 255f; // B 0..1
			}
		}

		title = imp.getTitle().replace(" (RGB)", "");
		sstack = new ImageStack(width, height); // Create new float stack for
		// output

		if (colourspace.equals("XYZ")) {
			n1 = "X";
			n2 = "Y";
			n3 = "Z";
			getXYZ();
		} else if (colourspace.equals("Yxy")) {
			n1 = "Y";
			n2 = "x";
			n3 = "y";
			getYxy();
		} else if (colourspace.equals("YUV")) {
			n1 = "Y";
			n2 = "U";
			n3 = "V";
			getYUV();
		} else if (colourspace.equals("YCbCr")) {
			n1 = "Y";
			n2 = "Cb";
			n3 = "Cr";
			getYCbCr();
		} else if (colourspace.equals("YIQ")) {
			n1 = "Y";
			n2 = "I";
			n3 = "Q";
			getYIQ();
		} else if (colourspace.equals("AC1C2")) {
			n1 = "A";
			n2 = "C1";
			n3 = "C2";
			getAC1C2();
		} else if (colourspace.equals("Luv")) {
			n1 = "L";
			n2 = "u";
			n3 = "v";
			getLuv();
		} else if (colourspace.equals("Lab")) {
			n1 = "L";
			n2 = "a";
			n3 = "b";
			getLab();
		} else if (colourspace.equals("I1I2I3")) {
			n1 = "I1";
			n2 = "I2";
			n3 = "I3";
			getI1I2I3();
		} else if (colourspace.equals("Yuv")) {
			n1 = "Y";
			n2 = "u";
			n3 = "v";
			getYuv();
		} else if (colourspace.equals("YQ1Q2")) {
			n1 = "Y";
			n2 = "Q1";
			n3 = "Q2";
			getYQ1Q2();
		} else if (colourspace.equals("HSI")) {
			n1 = "H";
			n2 = "S";
			n3 = "I";
			getHSI();
		} else if (colourspace.equals("HSV/HSB")) {
			n1 = "H";
			n2 = "S";
			n3 = "V/B";
			getHSV();
		} else if (colourspace.equals("HSL")) {
			n1 = "H";
			n2 = "S";
			n3 = "L";
			getHSL();
		} else if (colourspace.equals("HCY")) {
			n1 = "H";
			n2 = "C";
			n3 = "Y";
			getHCY();
		} else if (colourspace.equals("LCHLuv")) {
			n1 = "L";
			n2 = "C";
			n3 = "H";
			getLCHLuv();
		} else if (colourspace.equals("LSHLuv")) {
			n1 = "L";
			n2 = "S";
			n3 = "H";
			getLSHLuv();
		} else if (colourspace.equals("LCHLab")) {
			n1 = "L";
			n2 = "C";
			n3 = "H";
			getLCHLab();
		} else if (colourspace.equals("LSHLab")) {
			n1 = "L";
			n2 = "S";
			n3 = "H";
			getLSHLab();
		} else if (colourspace.equals("CMYK")) {
			n1 = "C";
			n2 = "M";
			n3 = "Y";
			n4 = "K";
			getCMYK();
		} else if (colourspace.equals("CMYK plates")) {
			n1 = "cyan plate";
			n2 = "magenta plate";
			n3 = "yellow plate";
			n4 = "black plate";
			getCMYKplates();
		}

		if (c1.length == size) {

			ImagePlus imc1 = NewImage.createFloatImage(n1, width, height, 1, NewImage.FILL_BLACK);
			ImageProcessor ipc1 = imc1.getProcessor();
			ipc1.setPixels(c1);
			sstack.addSlice(n1, ipc1);

			ImagePlus imc2 = NewImage.createFloatImage(n2, width, height, 1, NewImage.FILL_BLACK);
			ImageProcessor ipc2 = imc2.getProcessor();
			ipc2.setPixels(c2);
			sstack.addSlice(n2, ipc2);

			ImagePlus imc3 = NewImage.createFloatImage(n3, width, height, 1, NewImage.FILL_BLACK);
			ImageProcessor ipc3 = imc3.getProcessor();
			ipc3.setPixels(c3);
			sstack.addSlice(n3, ipc3);

			if (colourspace.equals("CMYK")) {
				ImagePlus imc4 = NewImage.createFloatImage(n4, width, height, 1, NewImage.FILL_BLACK);
				ImageProcessor ipc4 = imc4.getProcessor();
				ipc4.setPixels(c4);
				sstack.addSlice(n4, ipc4);
			}
		}

		ImagePlus imluv = new ImagePlus(title + " (" + colourspace + ")", sstack);
		imluv.show();
		IJ.resetMinAndMax();

	}

	// RWC: white reference values and most RGB-XYZ matrices are from
	// www.brucelindbloom.com.

	private float[] D50 = { 96.422f, 100.0f, 82.521f };
	private float[] D55 = { 95.682f, 100.0f, 92.149f };
	private float[] D65 = { 95.047f, 100.0f, 108.883f };

	public void getXYZ() {
		// http://www.easyrgb.com/math.html

		String encoding = "";
		float[][] matrix = new float[3][3];

		// RWC: RGB-XYZ encodings, matrices, white references

		if (RGBcolourspace.startsWith("sRGB")) {
			encoding = "sRGB";

			matrix = new float[][] { { 0.4124564f, 0.3575761f, 0.1804375f }, { 0.2126729f, 0.7151522f, 0.0721750f }, { 0.0193339f, 0.1191920f, 0.9503041f } };

			Lwhite = D65;
		}

		else if (RGBcolourspace.startsWith("Adobe RGB")) {
			encoding = "gamma22";

			matrix = new float[][] { { 0.5767309f, 0.1855540f, 0.1881852f }, { 0.2973769f, 0.6273491f, 0.0752741f }, { 0.0270343f, 0.0706872f, 0.9911085f } };

			Lwhite = D65;
		}

		else if (RGBcolourspace.startsWith("ProPhoto RGB")) {
			encoding = "gamma18";

			matrix = new float[][] { { 0.7976749f, 0.1351917f, 0.0313534f }, { 0.2880402f, 0.7118741f, 0.0000857f }, { 0.0000000f, 0.0000000f, 0.8252100f } };

			Lwhite = D50;
		}

		else if (RGBcolourspace.startsWith("eciRGB v2")) {
			encoding = "Lstar";

			matrix = new float[][] { { 0.6502043f, 0.1780774f, 0.1359384f }, { 0.3202499f, 0.6020711f, 0.0776791f }, { 0.0000000f, 0.0678390f, 0.7573710f } };

			Lwhite = D50;
		}

		else if (RGBcolourspace.startsWith("Custom")) { // ProPhoto 2.2 5500
			encoding = "gamma22";

			matrix = new float[][] { { 0.7857983f, 0.1360043f, 0.0350196f }, // ProPhoto
																				// with
																				// a
					// D55 white
					// reference
					{ 0.2837516f, 0.7161527f, 0.0000957f }, { 0.0000000f, 0.0000000f, 0.9217041f } };

			Lwhite = D55;
		}

		for (int q = 0; q < size; q++) {

			float X = 0, Y = 0, Z = 0;

			if (encoding.equals("sRGB")) {
				rf[q] = (rf[q] > 0.04045f) ? (new Double(Math.exp(Math.log((rf[q] + 0.055) / 1.055) * 2.4))).floatValue() : rf[q] / 12.92f;
				gf[q] = (gf[q] > 0.04045f) ? (new Double(Math.exp(Math.log((gf[q] + 0.055) / 1.055) * 2.4))).floatValue() : gf[q] / 12.92f;
				bf[q] = (bf[q] > 0.04045f) ? (new Double(Math.exp(Math.log((bf[q] + 0.055) / 1.055) * 2.4))).floatValue() : bf[q] / 12.92f;
			}

			else if (encoding.equals("gamma22")) {
				rf[q] = new Double(Math.exp(Math.log(rf[q]) * 2.2)).floatValue();
				gf[q] = new Double(Math.exp(Math.log(gf[q]) * 2.2)).floatValue();
				bf[q] = new Double(Math.exp(Math.log(bf[q]) * 2.2)).floatValue();
			}

			else if (encoding.equals("gamma18")) {
				rf[q] = new Double(Math.exp(Math.log(rf[q]) * 1.8)).floatValue();
				gf[q] = new Double(Math.exp(Math.log(gf[q]) * 1.8)).floatValue();
				bf[q] = new Double(Math.exp(Math.log(bf[q]) * 1.8)).floatValue();
			}

			else if (encoding.equals("Lstar")) {
				rf[q] = (rf[q] > 0.08f) ? (new Double(Math.exp(Math.log((rf[q] + 0.16) / 1.16) * 3))).floatValue() : rf[q] / 9.033f;
				gf[q] = (gf[q] > 0.08f) ? (new Double(Math.exp(Math.log((gf[q] + 0.16) / 1.16) * 3))).floatValue() : gf[q] / 9.033f;
				bf[q] = (bf[q] > 0.08f) ? (new Double(Math.exp(Math.log((bf[q] + 0.16) / 1.16) * 3))).floatValue() : bf[q] / 9.033f;
			}

			X = matrix[0][0] * rf[q] + matrix[0][1] * gf[q] + matrix[0][2] * bf[q];
			Y = matrix[1][0] * rf[q] + matrix[1][1] * gf[q] + matrix[1][2] * bf[q];
			Z = matrix[2][0] * rf[q] + matrix[2][1] * gf[q] + matrix[2][2] * bf[q];

			c1[q] = X * 100f;
			c2[q] = Y * 100f;
			c3[q] = Z * 100f;

		}

		/*
		 * // custom RGB examples: else if (RGBcolourspace.startsWith("Custom"))
		 * { // ColorMatch RGB encoding = "gamma18"; matrix = new float[][]
		 * {{0.5093439f, 0.3209071f, 0.1339691f}, {0.2748840f, 0.6581315f,
		 * 0.0669845f}, {0.0242545f, 0.1087821f, 0.6921735f}}; Lwhite = D50; }
		 * else if (RGBcolourspace.startsWith("Custom")) { // Wide Gamut RGB
		 * encoding = "gamma22"; matrix = new float[][] {{0.7161046f,
		 * 0.1009296f, 0.1471858f}, {0.2581874f, 0.7249378f, 0.0168748f},
		 * {0.0000000f, 0.0517813f, 0.7734287f}}; Lwhite = D50; } else if
		 * (RGBcolourspace.startsWith("Custom")) { // Beta RGB encoding =
		 * "gamma22"; matrix = new float[][] {{0.6712537f, 0.1745834f,
		 * 0.1183829f}, {0.3032726f, 0.6637861f, 0.0329413f}, {0.0000000f,
		 * 0.0407010f, 0.7845090f}}; Lwhite = D50; } else if
		 * (RGBcolourspace.startsWith("Custom")) { // Russell RGB encoding =
		 * "gamma22"; matrix = new float[][] {{0.7015731f, 0.1554157f,
		 * 0.0998333f}, {0.3151995f, 0.6648338f, 0.0199667f}, {0.0000000f,
		 * 0.0431710f, 0.8785331f}}; Lwhite = D55; } else if
		 * (RGBcolourspace.startsWith("Custom")) { // Melissa RGB encoding =
		 * "sRGB"; matrix = new float[][] {{0.7976749f, 0.1351917f, 0.0313534f},
		 * {0.2880402f, 0.7118741f, 0.0000857f}, {0.0000000f, 0.0000000f,
		 * 0.8252100f}}; Lwhite = D50; }
		 */
	}

	public void getYxy() {
		// @ARTICLE{TrussellHJ05colour:art,
		// author = {H. J. Trussell and E. Saber and M. Vrhel},
		// title = {Color image processing [basics and special issue overview]},
		// journal = {IEEE Signal Processing Magazine},
		// year = {2005},
		// volume = {22},
		// pages = {14 - 22},
		// number = {1},
		// month = {January},
		// }

		getXYZ();

		for (int q = 0; q < size; q++) {

			float X = c1[q];
			float Y = c2[q];
			float Z = c3[q];

			float xx = (X == 0f && Y == 0f && Z == 0f) ? 0f : (X / ((X + Y + Z)));
			float yy = (X == 0f && Y == 0f && Z == 0f) ? 0f : (Y / ((X + Y + Z)));

			c1[q] = Y;
			c2[q] = xx;
			c3[q] = yy;
		}
	}

	public void getYUV() {
		// @ARTICLE{ChengHD00:art,
		// author = {H.D. Cheng and X.H. Jiang and Y. Sun and J.L. Wang},
		// title = {Color Image Segmentation: Advances and Prospects},
		// journal = {Pattern Recognition},
		// year = {2000},
		// volume = {34},
		// pages = {2259-2281},
		// month = {September},
		// }
		for (int q = 0; q < size; q++) {
			float Y = 0.299f * rf[q] + 0.587f * gf[q] + 0.114f * bf[q];
			float U = -0.147f * rf[q] - 0.289f * gf[q] + 0.437f * bf[q];
			float V = 0.615f * rf[q] - 0.515f * gf[q] - 0.100f * bf[q];

			c1[q] = Y;
			c2[q] = U;
			c3[q] = V;
		}
	}

	public void getYIQ() {
		// @ARTICLE{ChengHD00:art,
		// author = {H.D. Cheng and X.H. Jiang and Y. Sun and J.L. Wang},
		// title = {Color Image Segmentation: Advances and Prospects},
		// journal = {Pattern Recognition},
		// year = {2000},
		// volume = {34},
		// pages = {2259-2281},
		// month = {September},
		// }
		for (int q = 0; q < size; q++) {
			float Y = 0.299f * rf[q] + 0.587f * gf[q] + 0.114f * bf[q];
			float I = 0.596f * rf[q] - 0.274f * gf[q] - 0.322f * bf[q];
			float Q = 0.211f * rf[q] - 0.253f * gf[q] - 0.312f * bf[q];

			c1[q] = Y;
			c2[q] = I;
			c3[q] = Q;
		}
	}

	// RWC; see www.equasys.de/colorconversion.html.
	// This is the matrix for full-range (0-255) YCbCr colors

	public void getYCbCr() {
		for (int q = 0; q < size; q++) {

			rf[q] *= 255;
			gf[q] *= 255;
			bf[q] *= 255;

			float Y = 0.299f * rf[q] + 0.587f * gf[q] + 0.114f * bf[q];
			float Cb = -0.169f * rf[q] - 0.331f * gf[q] + 0.500f * bf[q] + 128;
			float Cr = 0.500f * rf[q] - 0.419f * gf[q] - 0.081f * bf[q] + 128;

			c1[q] = Y;
			c2[q] = Cb;
			c3[q] = Cr;
		}
	}

	public void getAC1C2() {
		// @ARTICLE{FaugerasO79:art,
		// author = {O. Faugeras},
		// title = {Digital color image processing within the framework of a
		// human visual model},
		// journal = {IEEE Transactions on Acoustics, Speech, and Signal
		// Processing},
		// year = {1979},
		// volume = {27},
		// pages = {380- 393},
		// number = {4},
		// month = {August},
		// }
		for (int q = 0; q < size; q++) {
			float L, M, S;
			float A, C1, C2;
			L = 0.3634f * rf[q] + 0.6102f * gf[q] + 0.0264f * bf[q];
			M = 0.1246f * rf[q] + 0.8138f * gf[q] + 0.0616f * bf[q];
			S = 0.0009f * rf[q] + 0.0602f * gf[q] + 0.9389f * bf[q];

			L = new Double(Math.log(L)).floatValue();
			M = new Double(Math.log(M)).floatValue();
			S = new Double(Math.log(S)).floatValue();

			A = 13.8312f * L + 8.3394f * M + 0.4294f * S;
			C1 = 64.0000f * L - 64.000f * M + 0.0000f * S;
			C2 = 10.0000f * L + 0.0000f * M - 10.000f * S;

			c1[q] = A;
			c2[q] = C1;
			c3[q] = C2;
		}
	}

	public void getLuv() {
		// http://www.easyrgb.com/math.html AND
		// @INBOOK{RonnierLuoM98colour:chapterbook,
		// chapter = {Colour science},
		// pages = {27-65},
		// title = {The Colour Image Processing Handbook},
		// publisher = {Springer},
		// year = {1998},
		// editor = {R. E.N. Horne},
		// author = {Ronnier Luo},
		// }

		getXYZ();

		float x = 0, y = 0, z = 0;

		// RWC white reference

		x = Lwhite[0];
		y = Lwhite[1];
		z = Lwhite[2];

		float yn = 1f;

		/** un' corresponding to Yn */
		float unp = (4 * x) / (x + 15 * y + 3 * z);

		/** vn' corresponding to Yn */
		float vnp = (9 * y) / (x + 15 * y + 3 * z);

		for (int q = 0; q < size; q++) {

			float X = c1[q];
			float Y = c2[q];
			float Z = c3[q];

			// As yn = 1.0, we will just consider Y value as yyn
			// yyn = (Y/yn);
			float f_yyn = Y / 100f;

			if (f_yyn > 0.008856f) {
				f_yyn = new Double(Math.exp(Math.log(f_yyn) / 3f)).floatValue();
			} else {
				f_yyn = ((7.787f * f_yyn) + (16f / 116f));
			}

			float up = (X == 0f && Y == 0f && Z == 0f) ? 0f : (4f * X / ((X + 15f * Y + 3f * Z)));
			float vp = (X == 0f && Y == 0f && Z == 0f) ? 0f : (9f * Y / ((X + 15f * Y + 3f * Z)));

			float l = (116f * f_yyn) - 16f;
			float u = 13f * l * (up - unp);
			float v = 13f * l * (vp - vnp);

			c1[q] = l;
			c2[q] = u;
			c3[q] = v;
		}
	}

	public void getLab() {
		// http://www.easyrgb.com/math.html AND
		// @INBOOK{RonnierLuoM98colour:chapterbook,
		// chapter = {Colour science},
		// pages = {27-65},
		// title = {The Colour Image Processing Handbook},
		// publisher = {Springer},
		// year = {1998},
		// editor = {R. E.N. Horne},
		// author = {Ronnier Luo},
		// }

		getXYZ();

		for (int q = 0; q < size; q++) {
			float l, a, b;
			float fX, fY, fZ;
			float La, aa, bb;
			float X = 0, Y = 0, Z = 0;

			// RWC white reference

			X = c1[q] / Lwhite[0];
			Y = c2[q] / Lwhite[1];
			Z = c3[q] / Lwhite[2];

			if (X > 0.008856f)
				fX = (new Double(Math.exp(Math.log(X) / 3f))).floatValue();
			else
				fX = ((7.787f * X) + (16f / 116f));

			if (Y > 0.008856f)
				fY = (new Double(Math.exp(Math.log(Y) / 3f))).floatValue();
			else
				fY = ((7.787f * Y) + (16f / 116f));

			if (Z > 0.008856f)
				fZ = (new Double(Math.exp(Math.log(Z) / 3f))).floatValue();
			else
				fZ = ((7.787f * Z) + (16f / 116f));

			l = (116f * fY) - 16f;
			a = 500f * (fX - fY);
			b = 200f * (fY - fZ);

			c1[q] = l;
			c2[q] = a;
			c3[q] = b;
		}
	}

	public void getI1I2I3() {
		// @ARTICLE{Ohta80:art,
		// author = {Y. Ohta and T. Kanade and T. Sakai},
		// title = {Color Information for Region Segmentation},
		// journal = {Computer Graphics and Image Processing},
		// year = {1980},
		// volume = {13},
		// pages = {222 - 241},
		// number = {3},
		// month = {July},
		// }
		for (int q = 0; q < size; q++) {
			float I1 = (rf[q] + gf[q] + bf[q]) / 3f;
			float I2 = (rf[q] - bf[q]) / 2f;
			float I3 = (2f * gf[q] - rf[q] - bf[q]) / 4f;
			c1[q] = I1;
			c2[q] = I2;
			c3[q] = I3;
		}
	}

	public void getYuv() {
		// @ARTICLE{LittmanE97colour:art,
		// author = {E. Littmann and H. Ritter},
		// title = {Adaptive Color Segmentation - A comparison of Neural and
		// Statistical Methods},
		// journal = {IEEE Transactions on neural networks},
		// year = {1997},
		// volume = {8},
		// pages = {175-185},
		// number = {1},
		// month = {January},
		// }
		for (int q = 0; q < size; q++) {
			float u, v;
			float Y = (rf[q] + gf[q] + bf[q]) / 3f;
			if (Y != 0f) {
				u = 3f * (rf[q] - bf[q]) / 2f * Y;
				v = (new Double(Math.sqrt(3.0)).floatValue()) * (2f * gf[q] - rf[q] - bf[q]) / 2f * Y;
			} else {
				u = 0f;
				v = 0f;
			}
			c1[q] = Y;
			c2[q] = u;
			c3[q] = v;
		}
	}

	public void getYQ1Q2() {
		// @ARTICLE{LittmanE97colour:art,
		// author = {E. Littmann and H. Ritter},
		// title = {Adaptive Color Segmentation - A comparison of Neural and
		// Statistical Methods},
		// journal = {IEEE Transactions on neural networks},
		// year = {1997},
		// volume = {8},
		// pages = {175-185},
		// number = {1},
		// month = {January},
		// }
		for (int q = 0; q < size; q++) {
			float Y = (rf[q] + gf[q] + bf[q]) / 3f;
			float Q1 = ((rf[q] + gf[q]) != 0f) ? rf[q] / (rf[q] + gf[q]) : 0f;
			float Q2 = ((rf[q] + bf[q]) != 0f) ? rf[q] / (rf[q] + bf[q]) : 0f;
			c1[q] = Y;
			c2[q] = Q1;
			c3[q] = Q2;
		}
	}

	public void getRGB() {
		c1 = rf;
		c2 = gf;
		c3 = bf;
	}

	public void getHSI() {
		// @BOOK{MalacaraD01:book,
		// title = {Color Vision and Colorimetry, Theory and Applications},
		// publisher = {SPIE International Society for Optical Engineering},
		// year = {2001},
		// author = {D. Malacara},
		// address = {Bellingham, Washington USA},
		// }
		for (int q = 0; q < size; q++) {
			float var_Min = Math.min(rf[q], gf[q]); // Min. value of RGB
			var_Min = Math.min(var_Min, bf[q]);
			float var_Max = Math.max(rf[q], gf[q]); // Max. value of RGB
			var_Max = Math.max(var_Max, bf[q]);
			float del_Max = var_Max - var_Min; // Delta RGB value

			c3[q] = (rf[q] + gf[q] + bf[q]) / 3f;

			if (del_Max == 0f) { // This is a gray, no chroma...
				c1[q] = 0f; // HSL results = 0 ? 1
				c2[q] = 0f;
			} else { // Chromatic data...
				c2[q] = 1 - (var_Min / c3[q]);

				float del_R = (((var_Max - rf[q]) / 6f) + (del_Max / 2f)) / del_Max;
				float del_G = (((var_Max - gf[q]) / 6f) + (del_Max / 2f)) / del_Max;
				float del_B = (((var_Max - bf[q]) / 6f) + (del_Max / 2f)) / del_Max;

				if (rf[q] == var_Max)
					c1[q] = del_B - del_G;
				else if (gf[q] == var_Max)
					c1[q] = (1f / 3f) + del_R - del_B;
				else if (bf[q] == var_Max)
					c1[q] = (2f / 3f) + del_G - del_R;

				if (c1[q] < 0)
					c1[q] += 1;
				if (c1[q] > 1)
					c1[q] -= 1;
			}
		}
	}

	public void getHSL() {
		// http://www.easyrgb.com/math.html
		for (int q = 0; q < size; q++) {
			float H = 0, S = 0, L = 0;
			float var_Min = Math.min(rf[q], gf[q]); // Min. value of RGB
			var_Min = Math.min(var_Min, bf[q]);
			float var_Max = Math.max(rf[q], gf[q]); // Max. value of RGB
			var_Max = Math.max(var_Max, bf[q]);
			float del_Max = var_Max - var_Min; // Delta RGB value

			L = (var_Max + var_Min) / 2;

			if (del_Max == 0f) { // This is a gray, no chroma...
				H = 0f; // HSL results = 0 ? 1
				S = 0f;
			} else { // Chromatic data...
				if (L < 0.5f)
					S = del_Max / (var_Max + var_Min);
				else
					S = del_Max / (2f - var_Max - var_Min);

				float del_R = (((var_Max - rf[q]) / 6f) + (del_Max / 2f)) / del_Max;
				float del_G = (((var_Max - gf[q]) / 6f) + (del_Max / 2f)) / del_Max;
				float del_B = (((var_Max - bf[q]) / 6f) + (del_Max / 2f)) / del_Max;

				if (rf[q] == var_Max)
					H = del_B - del_G;
				else if (gf[q] == var_Max)
					H = (1f / 3f) + del_R - del_B;
				else if (bf[q] == var_Max)
					H = (2f / 3f) + del_G - del_R;

				if (H < 0f)
					H += 1f;
				if (H > 1f)
					H -= 1f;
			}
			c1[q] = H;
			c2[q] = S;
			c3[q] = L;
		}
	}

	public void getHSV() { // HSV_Stack Plugin (HSV colour space is also known
		// as HSB where B means brightness)
		// http://www.easyrgb.com/math.html
		for (int q = 0; q < size; q++) { // http://www.easyrgb.com/
			float H = 0, S = 0, V = 0;
			float var_Min = Math.min(rf[q], gf[q]); // Min. value of RGB
			var_Min = Math.min(var_Min, bf[q]);
			float var_Max = Math.max(rf[q], gf[q]); // Max. value of RGB
			var_Max = Math.max(var_Max, bf[q]);
			float del_Max = var_Max - var_Min; // Delta RGB value

			V = var_Max * 1f;
			if (del_Max == 0) { // This is a gray, no chroma...
				H = 0f; // HSV results = 0 ? 1
				S = 0f;
			} else { // Chromatic data...
				S = del_Max / var_Max;
				float del_R = (((var_Max - rf[q]) / 6f) + (del_Max / 2f)) / del_Max;
				float del_G = (((var_Max - gf[q]) / 6f) + (del_Max / 2f)) / del_Max;
				float del_B = (((var_Max - bf[q]) / 6f) + (del_Max / 2f)) / del_Max;

				if (rf[q] == var_Max)
					H = del_B - del_G;
				else if (gf[q] == var_Max)
					H = (1f / 3f) + del_R - del_B;
				else if (bf[q] == var_Max)
					H = (2f / 3f) + del_G - del_R;

				if (H < 0)
					H += 1;
				if (H > 1)
					H -= 1;
			}
			c1[q] = H;
			c2[q] = S;
			c3[q] = V;
		}

	}

	// RWC. The combination of Hue, Chroma, and Luma (HCY) fills a niche.
	// Previously proposed by Kuzma Shapran; see
	// http://chilliant.blogspot.com/2012/08/rgbhcy-in-hlsl.html.

	public void getHCY() {
		for (int q = 0; q < size; q++) {
			float var_Min = Math.min(rf[q], gf[q]); // Min. value of RGB
			var_Min = Math.min(var_Min, bf[q]);
			float var_Max = Math.max(rf[q], gf[q]); // Max. value of RGB
			var_Max = Math.max(var_Max, bf[q]);
			float del_Max = var_Max - var_Min; // Delta RGB value

			if (del_Max == 0f) // This is a gray, no chroma...
				c1[q] = 0f; // HSL results = 0 ? 1
			else { // Chromatic data...
				float del_R = (((var_Max - rf[q]) / 6f) + (del_Max / 2f)) / del_Max;
				float del_G = (((var_Max - gf[q]) / 6f) + (del_Max / 2f)) / del_Max;
				float del_B = (((var_Max - bf[q]) / 6f) + (del_Max / 2f)) / del_Max;

				if (rf[q] == var_Max)
					c1[q] = del_B - del_G;
				else if (gf[q] == var_Max)
					c1[q] = (1f / 3f) + del_R - del_B;
				else if (bf[q] == var_Max)
					c1[q] = (2f / 3f) + del_G - del_R;

				if (c1[q] < 0)
					c1[q] += 1;
				if (c1[q] > 1)
					c1[q] -= 1;
			}
			c2[q] = del_Max;
			c3[q] = 0.299f * rf[q] + 0.587f * gf[q] + 0.114f * bf[q];
		}
	}

	public void getLCHLuv() {
		// @BOOK{MalacaraD01:book,
		// title = {Color Vision and Colorimetry, Theory and Applications},
		// publisher = {SPIE International Society for Optical Engineering},
		// year = {2001},
		// author = {D. Malacara},
		// address = {Bellingham, Washington USA},
		// }

		getXYZ();

		float x = 0, y = 0, z = 0;

		// RWC white reference

		x = Lwhite[0];
		y = Lwhite[1];
		z = Lwhite[2];

		float yn = 1f;

		/** un' corresponding to Yn */
		float unp = (4 * x) / (x + 15 * y + 3 * z);

		/** vn' corresponding to Yn */
		float vnp = (9 * y) / (x + 15 * y + 3 * z);

		for (int q = 0; q < size; q++) {
			float L = 0f, C = 0f, H = 0f;

			float X = c1[q];
			float Y = c2[q];
			float Z = c3[q];

			// As yn = 1.0, we will just consider Y value as yyn
			// yyn = (Y/yn);
			float f_yyn = Y / 100f;

			if (f_yyn > 0.008856f) {
				f_yyn = new Double(Math.exp(Math.log(f_yyn) / 3f)).floatValue();
			} else {
				f_yyn = ((7.787f * f_yyn) + (16f / 116f));
			}

			float up = (X == 0f && Y == 0f && Z == 0f) ? 0f : (4f * X / ((X + 15f * Y + 3f * Z)));
			float vp = (X == 0f && Y == 0f && Z == 0f) ? 0f : (9f * Y / ((X + 15f * Y + 3f * Z)));

			float l = (116f * f_yyn) - 16f;
			float u = 13f * l * (up - unp);
			float v = 13f * l * (vp - vnp);

			L = l;
			C = new Double(Math.sqrt((u * u) + (v * v))).floatValue();

			H = new Double((Math.atan2(v, u) + (2f * Math.PI)) % (2f * Math.PI)).floatValue(); // angle
																								// from
																								// 0
																								// to
																								// 2
																								// pi
			H = new Double((H / (2f * Math.PI)) * 100f).floatValue(); // normalize
			// to
			// 0-100

			c1[q] = L;
			c2[q] = C;
			c3[q] = H;
		}
	}

	public void getLSHLuv() {
		// @BOOK{MalacaraD01:book,
		// title = {Color Vision and Colorimetry, Theory and Applications},
		// publisher = {SPIE International Society for Optical Engineering},
		// year = {2001},
		// author = {D. Malacara},
		// address = {Bellingham, Washington USA},
		// }

		getXYZ();

		float x = 0, y = 0, z = 0;

		// RWC white reference

		x = Lwhite[0];
		y = Lwhite[1];
		z = Lwhite[2];

		float yn = 1f;

		/** un' corresponding to Yn */
		float unp = (4 * x) / (x + 15 * y + 3 * z);

		/** vn' corresponding to Yn */
		float vnp = (9 * y) / (x + 15 * y + 3 * z);

		for (int q = 0; q < size; q++) {
			float L = 0f, S = 0f, H = 0f;

			float X = c1[q];
			float Y = c2[q];
			float Z = c3[q];

			// As yn = 1.0, we will just consider Y value as yyn
			// yyn = (Y/yn);
			float f_yyn = Y / 100f;

			if (f_yyn > 0.008856f) {
				f_yyn = new Double(Math.exp(Math.log(f_yyn) / 3f)).floatValue();
			} else {
				f_yyn = ((7.787f * f_yyn) + (16f / 116f));
			}

			float up = (X == 0f && Y == 0f && Z == 0f) ? 0f : (4f * X / ((X + 15f * Y + 3f * Z)));
			float vp = (X == 0f && Y == 0f && Z == 0f) ? 0f : (9f * Y / ((X + 15f * Y + 3f * Z)));

			float l = (116f * f_yyn) - 16f;
			float u = 13f * l * (up - unp);
			float v = 13f * l * (vp - vnp);

			L = l;
			S = (new Double(13 * Math.sqrt(((u - up) * (u - up)) + ((v - vp) * (v - vp))))).floatValue();

			H = new Double((Math.atan2(v, u) + (2f * Math.PI)) % (2f * Math.PI)).floatValue(); // angle
																								// from
																								// 0
																								// to
																								// 2
																								// pi
			H = new Double((H / (2f * Math.PI)) * 100f).floatValue(); // normalize
			// to
			// 0-100

			c1[q] = L;
			c2[q] = S;
			c3[q] = H;
		}
	}

	public void getLCHLab() {
		// @BOOK{MalacaraD01:book,
		// title = {Color Vision and Colorimetry, Theory and Applications},
		// publisher = {SPIE International Society for Optical Engineering},
		// year = {2001},
		// author = {D. Malacara},
		// address = {Bellingham, Washington USA},
		// }

		getXYZ();

		for (int q = 0; q < size; q++) {
			float L = 0f, C = 0f, H = 0f;
			float fX, fY, fZ;
			float X = 0, Y = 0, Z = 0;

			// RWC white reference

			X = c1[q] / Lwhite[0];
			Y = c2[q] / Lwhite[1];
			Z = c3[q] / Lwhite[2];

			if (X > 0.008856f)
				fX = (new Double(Math.exp(Math.log(X) / 3f))).floatValue();
			else
				fX = ((7.787f * X) + (16f / 116f));

			if (Y > 0.008856f)
				fY = (new Double(Math.exp(Math.log(Y) / 3f))).floatValue();
			else
				fY = ((7.787f * Y) + (16f / 116f));

			if (Z > 0.008856f)
				fZ = (new Double(Math.exp(Math.log(Z) / 3f))).floatValue();
			else
				fZ = ((7.787f * Z) + (16f / 116f));

			float l = (116f * fY) - 16f;
			float a = 500f * (fX - fY);
			float b = 200f * (fY - fZ);

			L = l;
			C = new Double(Math.sqrt((a * a) + (b * b))).floatValue();

			H = new Double((Math.atan2(b, a) + (2f * Math.PI)) % (2f * Math.PI)).floatValue(); // angle
																								// from
																								// 0
																								// to
																								// 2
																								// pi
			H = new Double((H / (2f * Math.PI)) * 100f).floatValue(); // normalize
			// to
			// 0-100

			c1[q] = L;
			c2[q] = C;
			c3[q] = H;
		}
	}

	// RWC

	public void getLSHLab() {
		// @ARTICLE{L�bbeE13colour:art,
		// author = {Eva L�bbe},
		// title = {Experimental Evidence of the Formula of Saturation},
		// journal = {Journal of Physical Science and Application},
		// year = {2013},
		// volume = {3},
		// pages = {79-81},
		// number = {2},
		// month = {February},
		// }

		getXYZ();

		for (int q = 0; q < size; q++) {
			float L = 0f, C = 0f, S = 0f, H = 0f;
			float fX, fY, fZ;
			float X = 0, Y = 0, Z = 0;

			// RWC white reference

			X = c1[q] / Lwhite[0];
			Y = c2[q] / Lwhite[1];
			Z = c3[q] / Lwhite[2];

			if (X > 0.008856f)
				fX = (new Double(Math.exp(Math.log(X) / 3f))).floatValue();
			else
				fX = ((7.787f * X) + (16f / 116f));

			if (Y > 0.008856f)
				fY = (new Double(Math.exp(Math.log(Y) / 3f))).floatValue();
			else
				fY = ((7.787f * Y) + (16f / 116f));

			if (Z > 0.008856f)
				fZ = (new Double(Math.exp(Math.log(Z) / 3f))).floatValue();
			else
				fZ = ((7.787f * Z) + (16f / 116f));

			float l = (116f * fY) - 16f;
			float a = 500f * (fX - fY);
			float b = 200f * (fY - fZ);

			L = l;
			C = new Double(Math.sqrt((a * a) + (b * b))).floatValue();
			if ((L == 0) && (C == 0))
				S = 100f;
			else
				S = new Double(C / Math.sqrt(C * C + L * L)).floatValue() * 100f;
			S = Math.min(S, 100f);

			H = new Double((Math.atan2(b, a) + (2f * Math.PI)) % (2f * Math.PI)).floatValue(); // angle
																								// from
																								// 0
																								// to
																								// 2
																								// pi
			H = new Double((H / (2f * Math.PI)) * 100f).floatValue(); // normalize
			// to
			// 0-100

			c1[q] = L;
			c2[q] = S;
			c3[q] = H;
		}
	}

	// RWC. This is an idealized transformation using full gray component
	// replacement.
	// Because CMYK is subtractive, invert the image to see the appearance of
	// the plates.

	public void getCMYK() {
		for (int q = 0; q < size; q++) {

			float K = 1 - Math.max(Math.max(rf[q], gf[q]), bf[q]);

			float C = 0;
			float M = 0;
			float Y = 0;

			if (K != 1) {
				C = (1 - rf[q] - K) / (1 - K);
				M = (1 - gf[q] - K) / (1 - K);
				Y = (1 - bf[q] - K) / (1 - K);
			}

			c1[q] = C * 100f;
			c2[q] = M * 100f;
			c3[q] = Y * 100f;
			c4[q] = K * 100f;
		}
	}

	// RWC. A very simple GCR conversion for illustrative purposes.

	public void getCMYKplates() {

		// Loosely based on:
		// @BOOK{SharmaG02:book,
		// title = {Digital Color Imaging Handbook},
		// publisher = {CRC Press},
		// year = {2002},
		// editor = {Gaurav Sharma and Raja Bala},
		// address = {Boca Raton, Florida USA},
		// }

		c1 = new float[0];
		c2 = new float[0];
		c3 = new float[0];
		c4 = new float[0];

		d1 = new int[size];
		d2 = new int[size];
		d3 = new int[size];
		d4 = new int[size];

		for (int q = 0; q < size; q++) {

			float L = 0.299f * rf[q] + 0.587f * gf[q] + 0.114f * bf[q]; // simplifying;
			// L*
			// would
			// be
			// better
			float D = 1 - L; // darkness; opposite of lightness
			float Kstart = 0.2f; // D at which to start adding black
			float Klimit = 0.9f; // maximum amount of black
			float g = 1.7f; // exponent for the GCR curve
			float fD = 0f; // amount of GCR to add
			if (D > Kstart)
				fD = Klimit * (new Double(Math.exp(Math.log((D - Kstart) / (1 - Kstart)) * g))).floatValue();

			float K = fD * (1 - Math.max(Math.max(rf[q], gf[q]), bf[q]));

			float C = 0;
			float M = 0;
			float Y = 0;

			if (K != 1) {
				C = (1 - rf[q] - K) / (1 - K);
				M = (1 - gf[q] - K) / (1 - K);
				Y = (1 - bf[q] - K) / (1 - K);
			}

			d1[q] = (Math.round((1 - C) * 255f) << 16) + (255 << 8) + 255;
			d2[q] = (255 << 16) + (Math.round((1 - M) * 255f) << 8) + 255;
			d3[q] = (255 << 16) + (255 << 8) + Math.round((1 - Y) * 255f);
			int k = Math.round((1 - K) * 255f);
			d4[q] = (k << 16) + (k << 8) + k;
		}

		ImagePlus imc1 = NewImage.createRGBImage(n1, width, height, 1, NewImage.FILL_BLACK);
		ImageProcessor ipc1 = imc1.getProcessor();
		ipc1.setPixels(d1);
		sstack.addSlice(n1, ipc1);

		ImagePlus imc2 = NewImage.createRGBImage(n2, width, height, 1, NewImage.FILL_BLACK);
		ImageProcessor ipc2 = imc2.getProcessor();
		ipc2.setPixels(d2);
		sstack.addSlice(n2, ipc2);

		ImagePlus imc3 = NewImage.createRGBImage(n3, width, height, 1, NewImage.FILL_BLACK);
		ImageProcessor ipc3 = imc3.getProcessor();
		ipc3.setPixels(d3);
		sstack.addSlice(n3, ipc3);

		ImagePlus imc4 = NewImage.createRGBImage(n4, width, height, 1, NewImage.FILL_BLACK);
		ImageProcessor ipc4 = imc4.getProcessor();
		ipc4.setPixels(d4);
		sstack.addSlice(n4, ipc4);
	}

	void showAbout() {
		IJ.showMessage("About Colour_transform...", "It converts an RGB image into a colour space stack.\n");
	}

	void error() {
		IJ.showMessage("Colour_transform", "This plugin converts an RGB image into \n" + "a colour space stack.");
	}

}
