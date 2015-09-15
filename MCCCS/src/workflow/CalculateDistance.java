package workflow;

import java.io.File;
import java.io.IOException;

import org.graffiti.plugin.io.resources.FileSystemHandler;

import de.ipk.ag_ba.image.operation.DistanceMapFloatMode;
import de.ipk.ag_ba.image.structures.Image;

/**
 * Create difference image, indicating true positive and true negative in white.
 * False positive in blue and false negative in red.
 * input: param 1 ground truth mask, params 2 prediction, param 3 output file name for difference image.
 * output: difference image according to param 3
 * 
 * @author Christian Klukas
 */
public class CalculateDistance {
	
	public static void main(String[] args) throws IOException, Exception {
		{
			new Settings();
		}
		if (args == null || args.length != 3) {
			System.err.println("Modes: 0 - eucl. distance map X, 1 - eucl. distance map Y, 2 - eucl. distance map dist, 3 - euchl. distance map degree*255/360");
			System.err.println("Params: [mode 0/1/2/3] [input mask] [target TIFF file (distance map)] ! Return Code 1");
			System.exit(1);
		} else {
			Integer mode = Integer.parseInt(args[0]);
			File f_input_mask = new File(args[1]);
			File f_out = new File(args[2]);
			if (f_out.exists()) {
				System.err.println("Error - Output target file  '" + f_out.getName() + "' already exists! Return Code 2");
				System.exit(2);
			} else
				if (mode < 0 || mode > 3) {
					System.err
							.println("Modes: 0 - eucl. distance map X, 1 - eucl. distance map Y, 2 - eucl. distance map dist, 3 - euchl. distance map degree*255/360");
					System.err.println("Only modes 0..3 are supported! Return Code 2");
					System.exit(2);
				} else
					if (!f_input_mask.exists()) {
						System.err.println("File input mask '" + f_input_mask.getName() + "' could not be found! Return Code 2");
						System.exit(2);
					} else {
						Image foregroundMask = new Image(FileSystemHandler.getURL(f_input_mask));
						float[][] out = null;
						
						DistanceMapFloatMode dm = DistanceMapFloatMode.fromInt(mode);
						
						out = foregroundMask.io().distanceMapFloat(dm);
						
						new Image(out).saveToFile(f_out.getPath());
					}
		}
	}
}
