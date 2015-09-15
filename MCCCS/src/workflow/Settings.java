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
		if (invertFGBG) {
			foreground = getBackground();
			back = Color.WHITE.getRGB();
			
			ImageOperation.BACKGROUND_COLORint = back;
			ImageOperation.BACKGROUND_COLOR = new Color(back);
		}
		
		if (System.getenv("BACKGROUND") != null) {
			back = ColorUtil.getColorFromHex(System.getenv("BACKGROUND")).getRGB();
			ImageOperation.BACKGROUND_COLORint = back;
			ImageOperation.BACKGROUND_COLOR = new Color(back);
		}
		
		if (System.getenv("FOREGROUND") != null) {
			foreground = ColorUtil.getColorFromHex(System.getenv("FOREGROUND")).getRGB();
		}
	}
	
	public static int getBackground() {
		ImageOperation.BACKGROUND_COLORint = -16777216; // RGB background color
		ImageOperation.BACKGROUND_COLOR = new Color(back);
		
		return ImageOperation.BACKGROUND_COLORint;
	}
}
