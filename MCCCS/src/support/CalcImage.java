package support;

import java.io.File;
import java.io.IOException;

import org.graffiti.plugin.io.resources.FileSystemHandler;

import workflow.Settings;
import de.ipk.ag_ba.image.structures.Image;

/**
 * Create image by calculating the difference, sum, division or multiplication of two images.
 * input: param 1 image A, params 2 imageB, param 3 output file, param 4 one of this: +,-,*,/.
 * output: difference image according to operation in param 4
 * 
 * @author Christian Klukas
 */
public class CalcImage {
	
	private enum Operation {
		plus, minus, times, divide
	};
	
	public static void main(String[] args) throws IOException, Exception {
		{
			new Settings();
		}
		if (args == null || args.length != 4) {
			System.err.println("Params: [image A (float)] [image B (float)] [target file] [-+*/] ! Return Code 1");
			System.exit(1);
		} else {
			File f_a = new File(args[0]);
			File f_b = new File(args[1]);
			File f_out = new File(args[2]);
			String arg3 = args[3];
			Operation op = null;
			if (arg3.equals("-"))
				op = Operation.minus;
			if (arg3.equals("+"))
				op = Operation.plus;
			if (arg3.equals("*"))
				op = Operation.times;
			if (arg3.equals("/"))
				op = Operation.divide;
			if (op == null) {
				System.err.println("Error - Operation  '" + args[3] + "' is unknown! Return Code 3");
				System.exit(3);
			}
			if (f_out.exists()) {
				System.err.println("Error - Output target file  '" + f_out.getName() + "' already exists! Return Code 2");
				System.exit(2);
			} else
				if (!f_a.exists()) {
					System.err.println("File image A '" + f_a.getName() + "' could not be found! Return Code 4");
					System.exit(4);
				} else
					if (!f_b.exists()) {
						System.err.println("File image B '" + f_b.getName() + "' could not be found! Return Code 5");
						System.exit(5);
					} else {
						Image imgA = new Image(FileSystemHandler.getURL(f_a));
						Image imgB = new Image(FileSystemHandler.getURL(f_b));
						float[] imgAf = imgA.getAs1float();
						float[] imgBf = imgB.getAs1float();
						if (imgAf.length != imgBf.length) {
							System.err.println("Error - Int file dimensions do not match! Return Code 3");
							System.exit(3);
						}
						float[] out = new float[imgAf.length];
						switch (op) {
							case divide:
								for (int i = 0; i < imgAf.length; i++)
									out[i] = imgAf[i] / imgBf[i];
								break;
							case minus:
								for (int i = 0; i < imgAf.length; i++)
									out[i] = imgAf[i] - imgBf[i];
								break;
							case plus:
								for (int i = 0; i < imgAf.length; i++)
									out[i] = imgAf[i] + imgBf[i];
								break;
							case times:
								for (int i = 0; i < imgAf.length; i++)
									out[i] = imgAf[i] * imgBf[i];
								break;
						}
						
						new Image(imgA.getWidth(), imgA.getHeight(), out).saveToFile(f_out.getPath());
					}
		}
	}
}
