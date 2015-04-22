package workflow;

import ij.io.Opener;
import ij.process.ImageProcessor;

import java.io.File;
import java.io.IOException;

import loci.formats.ChannelSeparator;
import loci.plugins.util.ImageProcessorReader;
import loci.plugins.util.LociPrefs;

import org.StringManipulationTools;

import de.ipk.ag_ba.image.structures.Image;

/**
 * Splits input Tiff-stack into separate images (e.g. useful for hyper-spectral datasets).
 * 
 * @author Jean-Michel Pape, Christian Klukas
 */
public class SplitTiffStackToImages {
	
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
					ImageProcessorReader r = new ImageProcessorReader(
							new ChannelSeparator(LociPrefs.makeImageReader()));
					
					r.setId(f.getPath());
					
					Opener oo = new Opener();
					oo.setSilentMode(true);
					
					if (r == null || r.getImageCount() == 0) {
						System.out.println("ERROR while reading Tiff-stack!");
					}
					
					int count = r.getImageCount();
					
					for (int idx = 0; idx < count; idx++) {
						ImageProcessor ip = r.openProcessors(idx)[0];
						new Image(ip).saveToFile(f.getParent() + File.separator + "channel_" + StringManipulationTools.formatNumberAddZeroInFront(idx + 1, 3)
								+ ".tif");
					}
					
					r.close();
				}
			}
		}
	}
}
