package workflow;

import java.io.File;

import org.graffiti.plugin.io.resources.FileSystemHandler;

import de.ipk.ag_ba.image.structures.Image;

/**
 * Creates an circular gradient image (tiff float), the most far point from the
 * center has value 0, the center point has value 1. The dimensions of the
 * target image are determined from the given input (template) image. input:
 * filename output: filename for gradient image (should have tiff extension)
 * 
 * @author klukas
 */
public class CreateCircularGradientImage {

	public static void main(String[] args) throws Exception {
		// {
		// new Settings(true);
		// }
		if (args == null || args.length < 2) {
			System.err.println("Invalid parameters provided! See examples for proper use.");
			System.err.println("Example 1: gradient from center of image, center is calculated from input file: CreateCircularGradientImage inp.png output.png");
			System.err.println("Example 2: gradient from griven center point(120;120), output size 640x480: CreateCircularGradientImage 120 120 640 480 output.png");
			System.err
					.println("Example 3: gradient from center of gravity of input image, considering pixels up to 70% of max distance from center point: CreateCircularGradientImage inp.png output.png auto 0.7");
			System.err
					.println("Example 4: gradient from center of gravity of input image, considering pixels up to 70% of max distance from center point, using inverted values: CreateCircularGradientImage lab_a.png output.png -auto 0.7");
			System.exit(1);
		} else {
			int w, h;
			int cx, cy;
			int offTargetFile = 0;
			double maxDist;
			if (args.length == 5) {
				cx = Integer.parseInt(args[0]);
				cy = Integer.parseInt(args[1]);
				w = Integer.parseInt(args[2]);
				h = Integer.parseInt(args[3]);
				offTargetFile = 2;
				maxDist = (float) Math.sqrt(w / 2 * w / 2 + h / 2 * h / 2);
			} else {
				File ff = new File(args[0]);
				if (!ff.exists()) {
					System.err.println("File input template '" + ff.getName() + "' could not be found! Return Code 2");
					System.exit(2);
				}
				Image img = new Image(FileSystemHandler.getURL(ff));
				w = img.getWidth();
				h = img.getHeight();
				cx = w / 2;
				cy = h / 2;
				maxDist = (float) Math.sqrt(w / 2 * w / 2 + h / 2 * h / 2);
				if (args.length == 4) {
					boolean ok = false;
					if (args[2].equalsIgnoreCase("auto") || args[2].equalsIgnoreCase("-auto")) {
						boolean invert = false;
						if (args[2].equalsIgnoreCase("-auto"))
							invert = true;
						double maxAllowed = Double.parseDouble(args[3]);
						maxDist = Math.sqrt(w / 2 * w / 2 + h / 2 * h / 2) * maxAllowed;

						double sumGray = 0;
						float[][] imgf = get2d(w, h, img.getAs1float());

						if (invert) {
							for (int x = 0; x < w; x++)
								for (int y = 0; y < h; y++)
									imgf[x][y] = -imgf[x][y];
						}

						{
							float min = Float.MAX_VALUE;
							for (int x = 0; x < w; x++)
								for (int y = 0; y < h; y++)
									if (imgf[x][y] < min)
										min = imgf[x][y];
							for (int x = 0; x < w; x++)
								for (int y = 0; y < h; y++)
									imgf[x][y] = imgf[x][y] - min;
						}

						for (int x = 0; x < w; x++) {
							for (int y = 0; y < h; y++) {
								double dist = Math.sqrt((x - cx) * (x - cx) + (y - cy) * (y - cy));
								if (dist < maxDist) {
									float color = imgf[x][y];
									sumGray += color;
								}
							}
						}
						double s0 = 0;
						for (int x = 0; x < w; x++) {
							double columnSum = 0;
							for (int y = 0; y < h; y++) {
								double dist = Math.sqrt((x - cx) * (x - cx) + (y - cy) * (y - cy));
								if (dist < maxDist) {
									float color = imgf[x][y];
									columnSum += color;
								}
							}
							s0 += columnSum;
							if (s0 >= sumGray / 2d) {
								cx = x;
								ok = true;
								break;
							}
						}
						s0 = 0;
						for (int y = 0; y < h; y++) {
							double rowSum = 0;
							for (int x = 0; x < w; x++) {
								double dist = Math.sqrt((x - cx) * (x - cx) + (y - cy) * (y - cy));
								if (dist < maxDist) {
									float color = imgf[x][y];
									rowSum += color;
								}
							}
							s0 += rowSum;
							if (s0 >= sumGray / 2d) {
								cy = y;
								break;
							}
						}
						if (!ok) {
							System.err.println("Return Code 4");
							System.exit(4);
						}
					} else {
						System.err.println("Invalid parameter 3. For auto center-of-gravity calculation use this syntax:");
						System.err.println("CreateCircularGradientImage inp.png output.png auto");
						System.err.println("Return Code 3");
						System.exit(3);
					}
				}
			}
			float[][] gradient = new float[w][h];
			for (int x = 0; x < w; x++)
				for (int y = 0; y < h; y++) {
					gradient[x][y] = 1f - (float) (Math.sqrt((x - cx) * (x - cx) + (y - cy) * (y - cy)) / maxDist);
				}
			new Image(gradient).saveToFile(args[offTargetFile + 1]);
		}
	}

	public static float[][] get2d(int w, int h, float[] as1a) {
		if (w * h != as1a.length)
			throw new IllegalArgumentException("width * height not equal to source length");

		float[][] image = new float[w][h];
		int idx = 0;
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				image[x][y] = as1a[idx++];
			}
		}
		return image;
	}
}
