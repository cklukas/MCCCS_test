package workflow;

import java.io.File;
import java.io.IOException;

import org.graffiti.plugin.io.resources.FileSystemHandler;

import de.ipk.ag_ba.image.operation.GrayscaleMode;
import de.ipk.ag_ba.image.structures.Image;

/**
 * SplitRGB
 * input: RGB image file
 * output:channel0.png, channel1.png, channel2.png
 * 
 * @author Jean-Michel Pape, Christian Klukas
 */
public class ThresholdGTforFGBG {
	
	public static void main(String[] args) throws IOException, Exception {
		{
			new Settings();
		}
		if (args == null || args.length == 0) {
			System.err.println("No filenames provided as parameters! Return Code 1");
			System.exit(1);
		} else {
			for (String a : args) {
				File f = new File(a);
				if (!f.exists()) {
					System.err.println("File '" + a + "' could not be found! Return Code 2");
					System.exit(2);
				} else {
					Image i = new Image(FileSystemHandler.getURL(f));
					i.io().convertRGB2Grayscale(GrayscaleMode.LIGHTNESS, false).threshold(1, Settings.back, Settings.foreground).getImage()
							.saveToFile(f.getParent() + File.separator + "mask_1.png");
					i.io().convertRGB2Grayscale(GrayscaleMode.LIGHTNESS, false).threshold(1, Settings.foreground, Settings.back).getImage()
							.saveToFile(f.getParent() + File.separator + "mask_2.png");
				}
			}
		}
	}
}
