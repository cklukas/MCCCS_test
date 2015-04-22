package workflow;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import org.graffiti.plugin.io.resources.FileSystemHandler;

import de.ipk.ag_ba.image.structures.Image;

/**
 * Create difference image, indicating true positive and true negative in white.
 * False positive in blue and false negative in red.
 * input: param 1 ground truth mask, params 2 prediction, param 3 output file name for difference image.
 * output: difference image according to param 3
 * 
 * @author Christian Klukas
 */
public class CreateDiffImage {
	
	public static void main(String[] args) throws IOException, Exception {
		{
			new Settings();
		}
		if (args == null || args.length != 3) {
			System.err.println("Params: [ground truth mask] [prediction mask] [target file] ! Return Code 1");
			System.exit(1);
		} else {
			File f_gt = new File(args[0]);
			File f_pred = new File(args[1]);
			File f_out = new File(args[2]);
			if (f_out.exists()) {
				System.err.println("Error - Output target file  '" + f_out.getName() + "' already exists! Return Code 2");
				System.exit(2);
			} else
				if (!f_gt.exists()) {
					System.err.println("File ground truth mask '" + f_gt.getName() + "' could not be found! Return Code 2");
					System.exit(2);
				} else
					if (!f_pred.exists()) {
						System.err.println("File prediction mask '" + f_pred.getName() + "' could not be found! Return Code 2");
						System.exit(2);
					} else {
						Image gt = new Image(FileSystemHandler.getURL(f_gt));
						Image pr = new Image(FileSystemHandler.getURL(f_pred));
						int[] gta = gt.getAs1A();
						int[] pra = pr.getAs1A();
						if (gta.length != pra.length) {
							System.err.println("Error - Int file dimensions do not match! Return Code 3");
							System.exit(3);
						}
						int[] out = new int[gta.length];
						for (int i = 0; i < gta.length; i++) {
							boolean gt_fore = gta[i] != Settings.back;
							boolean pr_fore = pra[i] != Settings.back;
							if (gt_fore == pr_fore) {
								out[i] = Settings.foreground;
							} else {
								if (pr_fore)
									out[i] = Color.RED.getRGB();
								else
									out[i] = Color.BLUE.getRGB();
							}
						}
						new Image(gt.getWidth(), gt.getHeight(), out).saveToFile(f_out.getPath());
					}
		}
	}
}
