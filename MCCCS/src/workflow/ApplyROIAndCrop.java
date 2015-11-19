package workflow;

import ij.ImagePlus;
import ij.io.FileSaver;
import ij.process.ImageProcessor;

import java.io.File;
import java.io.IOException;

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
						ImagePlus img_f = new ImagePlus(f_img.getAbsolutePath());
						ImagePlus mask_f = new ImagePlus(f_img.getAbsolutePath());
						
						ImageProcessor applyed = img_f.getProcessor().duplicate();
						applyed.fill(mask_f.getProcessor().convertToByteProcessor());
						System.out.println("ip_before: " + applyed.getWidth() + " " + applyed.getHeight());
						ImageProcessor out = applyed.resize(1000, 1000);
						//applyed.setRoi(0, 0, 1000, 1000);
						applyed.crop();
						System.out.println("ip_after: " + out.getWidth() + " " + out.getHeight());
								
						new FileSaver(new ImagePlus("cropped", out)).saveAsTiff(f_out.getAbsolutePath());
					}
		}
	}
}
