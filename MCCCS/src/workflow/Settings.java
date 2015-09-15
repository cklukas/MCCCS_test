package workflow;

import java.awt.Color;

import org.SystemAnalysis;
import org.color.ColorUtil;

import de.ipk.ag_ba.image.operation.ImageOperation;

/**
 * @author Christian Klukas
 */
public class Settings {
	
	public static int sampleSize = 100;
	public static int foreground = Color.WHITE.getRGB();
	public static int back = getBackground();
	public static float back_16 = 0f;
	public static boolean debug_IO = false;
	// public static int numberOfChannels = 4;
	public static int numberOfClasses = 2;
	public static boolean print_IO = false;
	
	public Settings() {
		this(false);
	}
	
	public Settings(boolean invertFGBG) {
		SystemAnalysis.simulateHeadless = true;
		
		boolean debug = false;
		
		if (System.getenv("BACKGROUNDFLIP") != null) {
			invertFGBG = Boolean.parseBoolean(System.getenv("BACKGROUNDFLIP"));
		}
		
		if (System.getenv("DEBUG") != null) {
			debug = Boolean.parseBoolean(System.getenv("DEBUG"));
		}
		
		if (invertFGBG) {
			foreground = getBackground();
			back = Color.WHITE.getRGB();
			
			ImageOperation.BACKGROUND_COLORint = back;
			ImageOperation.BACKGROUND_COLOR = new Color(back);
		}
		
		if (System.getenv("BACKGROUND") != null) {
			if (System.getenv("BACKGROUND").startsWith("#")) {
				back = ColorUtil.getColorFromHex(System.getenv("BACKGROUND")).getRGB();
			} else {
				back = Integer.parseInt(System.getenv("BACKGROUND"));
			}
			ImageOperation.BACKGROUND_COLORint = back;
			ImageOperation.BACKGROUND_COLOR = new Color(back);
		}
		
		if (System.getenv("FOREGROUND") != null) {
			if (System.getenv("FOREGROUND").startsWith("#")) {
				foreground = ColorUtil.getColorFromHex(System.getenv("FOREGROUND")).getRGB();
			} else {
				foreground = Integer.parseInt(System.getenv("FOREGROUND"));
			}
		}
		
		if (debug) {
			System.out.println("DEBUG: BACKGROUND=" + ColorUtil.getHexFromColor(ImageOperation.BACKGROUND_COLOR) + " / INT=" + ImageOperation.BACKGROUND_COLORint);
			System.out.println("DEBUG: FOREGROUND=" + ColorUtil.getHexFromColor(new Color(foreground)) + " / INT=" + foreground);
			System.out.println("HINT: Use environmen variables FOREGROUND and BACKGROUND using Hex-Color code (e.g. #99AAFF), to override these values.");
			System.out.println("HINT: Color code may also start without '#', then the input is parsed as integer code.");
		}
	}
	
	public static int getBackground() {
		ImageOperation.BACKGROUND_COLORint = -16777216; // RGB background color
		ImageOperation.BACKGROUND_COLOR = new Color(back);
		
		return ImageOperation.BACKGROUND_COLORint;
	}
}
