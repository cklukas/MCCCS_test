package workflow;

import java.io.File;

import org.graffiti.plugin.io.resources.FileSystemHandler;

import de.ipk.ag_ba.image.structures.Image;

/**
 * Creates an circular gradient image (tiff float), the most far point from the
 * center has value 0, the center point has value 1. The dimensions of the
 * target image are determined from the given input (template) image.
 * input: filename output: filename for gradient image (should have tiff
 * extension)
 * 
 * @author klukas
 */
public class CreateCircularGradientImage {
	
	public static void main(String[] args) throws Exception {
		{
			new Settings(true);
		}
		if (args == null || args.length < 2) {
			System.err
					.println("No parameter for [input file] [output file] is provided as a parameter! Return Code 1");
			System.exit(1);
		} else {
			File ff = new File(args[0]);
			if (!ff.exists()) {
				System.err.println("File input template '" + ff.getName()
						+ "' could not be found! Return Code 2");
				System.exit(2);
			}
			Image img = new Image(FileSystemHandler.getURL(ff));
			int w = img.getWidth();
			int h = img.getHeight();
			float cx = w / 2f;
			float cy = h / 2f;
			float[][] gradient = new float[w][h];
			float maxDist = (float) Math.sqrt(w / 2 * w / 2 + h / 2 * h / 2);
			for (int x = 0; x < w; x++)
				for (int y = 0; y < h; y++) {
					gradient[x][y] = 1f - (float) (Math.sqrt((x - cx)
							* (x - cx) + (y - cy) * (y - cy)) / maxDist);
				}
			new Image(gradient).saveToFile(args[1]);
		}
	}
}
