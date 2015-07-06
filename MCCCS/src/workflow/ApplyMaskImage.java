package workflow;

import java.io.File;
import java.io.IOException;

import org.graffiti.plugin.io.resources.FileSystemHandler;

import de.ipk.ag_ba.image.structures.Image;

/**
 * @author Jean-Michel Pape
 */
public class ApplyMaskImage {
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
						Image img = new Image(FileSystemHandler.getURL(f_img));
						Image mask = new Image(FileSystemHandler.getURL(f_mask));
						int[] img_1A = img.getAs1A();
						int[] mask_1A = mask.getAs1A();
						if (img_1A.length != mask_1A.length) {
							System.err.println("Error - Int file dimensions do not match! Return Code 3");
							System.exit(3);
						}
						
						int[] out = img_1A;
						for (int i = 0; i < img_1A.length; i++) {
							if (mask_1A[i] == Settings.foreground)
								out[i] = Settings.foreground;
						}
						new Image(img.getWidth(), img.getHeight(), out).saveToFile(f_out.getPath());
					}
		}
	}
}
