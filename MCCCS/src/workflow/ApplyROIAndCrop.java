package workflow;

import java.io.File;
import java.io.IOException;

import ij.ImagePlus;
import ij.io.FileSaver;
import ij.process.FloatProcessor;

/**
 * Apply a mask to given an image and return a cropped version with specified name (target file).
 * 
 * @author Jean-Michel Pape
 */
public class ApplyROIAndCrop {
	public static void main(String[] args) throws IOException, Exception {
		{
			new Settings();
		}
		if (args == null || args.length != 3) {
			System.err.println("Params: [image] [mask] [target file] ! Return Code 1");
			System.exit(1);
		} else {
			File f_img = new File(args[0]);
			File f_mask = new File(args[1]);
			File f_out = new File(args[2]);
			if (f_out.exists()) {
			System.err.println("Error - Output target file  '" + f_out.getName() + "' already exists! Return Code 2");
			System.exit(2);
			} else
			if (!f_img.exists()) {
				System.err.println("File ground truth mask '" + f_img.getName() + "' could not be found! Return Code 2");
				System.exit(2);
			} else
				if (!f_mask.exists()) {
					System.err.println("File prediction mask '" + f_mask.getName() + "' could not be found! Return Code 2");
					System.exit(2);
				} else {
					ImagePlus img_ip = new ImagePlus(f_img.getAbsolutePath());
					ImagePlus mask_ip = new ImagePlus(f_mask.getAbsolutePath());
					
					float[][] img_f = img_ip.getProcessor().getFloatArray();
					float[][] mask_f = mask_ip.getProcessor().getFloatArray();
					
					// get dimensions
					int min_x = Integer.MAX_VALUE;
					int max_x = Integer.MIN_VALUE;
					int min_y = Integer.MAX_VALUE;
					int max_y = Integer.MIN_VALUE;
					
					for (int x = 0; x < img_ip.getWidth(); x++) {
						for (int y = 0; y < img_ip.getHeight(); y++) {
						if (mask_f[x][y] == Settings.back_16) {
							img_f[x][y] = Settings.back_16;
						} else {
							if (x > max_x)
								max_x = x;
							if (x < min_x)
								min_x = x;
							if (y > max_y)
								max_y = y;
							if (y < min_y)
								min_y = y;
						}
						}
					}
					
					// crop image
					float[][] out_f = new float[max_x - min_x][max_y - min_y];
					
					for (int x = min_x; x < max_x; x++) {
						for (int y = min_y; y < max_y; y++) {
						out_f[x - min_x][y - min_y] = img_f[x][y];
						}
					}
					
					FloatProcessor out_proc = new FloatProcessor(out_f);
					ImagePlus out = new ImagePlus("crop", out_proc);
					// out.setRoi
					// ByteProcessor proc = new ByteProcessor(w, h, pix, null);
					
					new FileSaver(out).saveAsTiff(f_out.getAbsolutePath());
				}
		}
	}
}
