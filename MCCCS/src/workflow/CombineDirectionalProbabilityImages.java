package workflow;

import java.io.File;
import java.io.IOException;

import org.Vector2d;
import org.graffiti.plugin.io.resources.FileSystemHandler;

import de.ipk.ag_ba.image.structures.Image;

/**
 * @author Christian Klukas
 */
public class CombineDirectionalProbabilityImages {
	public static void main(String[] args) throws IOException, Exception {
		{
			new Settings();
		}
		if (args == null || args.length != 3) {
			System.err.println("Params: [tiff grayscale image1] [tiff grayscale image2] [target file] [mode 0/1] ! Return Code 1");
			System.err.println("Modes: 0 - img1=X, img2=Y; 1 - img1=DIST, img2=DEGREE*255/360");
			System.exit(1);
		} else {
			int mode = Integer.parseInt(args[3]);
			if (mode < 0 || mode > 1) {
				System.err.println("Error - Mode is invalid! Return Code 4");
				System.err.println("Modes: 0 - img1=X, img2=Y; 1 - img1=DIST, img2=DEGREE*255/360");
				System.exit(4);
			}
			File f_img = new File(args[0]);
			File f_mask = new File(args[1]);
			File f_out = new File(args[2]);
			if (f_out.exists()) {
				System.err.println("Error - Output target file  '" + f_out.getName() + "' already exists! Return Code 2");
				System.exit(2);
			} else
				if (!f_img.exists()) {
					System.err.println("File 1 '" + f_img.getName() + "' could not be found! Return Code 2");
					System.exit(2);
				} else
					if (!f_mask.exists()) {
						System.err.println("File 2 '" + f_mask.getName() + "' could not be found! Return Code 2");
						System.exit(2);
					} else {
						Image img1 = new Image(FileSystemHandler.getURL(f_img));
						Image img2 = new Image(FileSystemHandler.getURL(f_mask));
						float[] img_1f = img1.getAs1float();
						float[] img_2f = img2.getAs1float();
						if (img_1f.length != img_2f.length) {
							System.err.println("Error - File dimensions of images 1 and 2 do not match! Return Code 3");
							System.exit(3);
						}
						
						int w = img1.getWidth();
						int h = img1.getHeight();
						float[] out = new float[img_1f.length];
						for (int x = 0; x < w; x++)
							for (int y = 0; y < h; y++)
								out[x + y * w] = 0f;
						Vector2d vec = new Vector2d();
						for (int i = 0; i < img_1f.length; i++) {
							int x = i % w;
							int y = i / w;
							if (mode == 0) {
								float xt = x + img_1f[i];
								float yt = y + img_2f[i];
								if (xt >= 0 && xt < w && yt >= 0 && yt < h)
									out[i]++;
							} else {
								float dist = x + img_1f[i];
								float degree_scaled = y + img_2f[i]; // DEGREE*255/360
								float degree = degree_scaled * 360f / 255f;
								vec.x = dist;
								vec.y = 0;
								vec.rotateDirect(degree / 360 * 2 * Math.PI);
								float xt = (float) (vec.x + x);
								float yt = (float) (vec.y + y);
								if (xt >= 0 && xt < w && yt >= 0 && yt < h)
									out[i]++;
							}
						}
						new Image(img1.getWidth(), img1.getHeight(), out).saveToFile(f_out.getPath());
					}
		}
	}
}
