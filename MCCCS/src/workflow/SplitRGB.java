package workflow;

import java.io.File;
import java.io.IOException;

import org.graffiti.plugin.io.resources.FileSystemHandler;

import de.ipk.ag_ba.image.operation.channels.Channel;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.ImageType;

/**
 * SplitRGB
 * input: RGB image file
 * output:channel0.png, channel1.png, channel2.png
 * 
 * @author Christian Klukas
 */
public class SplitRGB {
	
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
					i.io().channels().get(Channel.RGB_R).getImage().saveToFile(f.getParent() + File.separator + "channel_rgb_r.tif", ImageType.GRAY32);
					i.io().channels().get(Channel.RGB_G).getImage().saveToFile(f.getParent() + File.separator + "channel_rgb_g.tif", ImageType.GRAY32);
					i.io().channels().get(Channel.RGB_B).getImage().saveToFile(f.getParent() + File.separator + "channel_rgb_b.tif", ImageType.GRAY32);
				}
			}
		}
	}
}
