package workflow;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.graffiti.plugin.io.resources.FileSystemHandler;

import de.ipk.ag_ba.image.operation.GrayscaleMode;
import de.ipk.ag_ba.image.structures.Image;

/**
 * ThresholdGTforFGBG
 * input: colored ground truth image
 * output:mask_1.png, mask_2.png (forground and background mask images), and according mask_1.arff, mask_2.arff files!
 * 
 * @author Jean-Michel Pape, Christian Klukas
 */
public class ThresholdGTforFGBG {
	
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
					Image i = new Image(FileSystemHandler.getURL(f));
					int[] m1 = i.io().convertRGB2Grayscale(GrayscaleMode.LIGHTNESS, false).threshold(1, Settings.back, Settings.foreground).getImage()
							.saveToFile(f.getParent() + File.separator + "mask_1.png").getAs1A();
					i.io().convertRGB2Grayscale(GrayscaleMode.LIGHTNESS, false).threshold(1, Settings.foreground, Settings.back).getImage()
							.saveToFile(f.getParent() + File.separator + "mask_2.png");
					
					saveTwoClassArffFile(f.getParent() + File.separator + "mask_1.arff", m1, Settings.foreground, "class0", "class1");
					saveTwoClassArffFile(f.getParent() + File.separator + "mask_2.arff", m1, Settings.foreground, "class1", "class0");
				}
			}
		}
	}
	
	/**
	 * <pre>
	 * '%
	 * '@relation 'plant005_2'
	 * '@attribute class {class0,class1}
	 * '@data
	 * 'class0
	 * '%
	 * </pre>
	 */
	public static void saveTwoClassArffFile(String fileName, int[] m1, int foreground, String className0, String classNameFG) throws IOException {
		BufferedWriter writer = null;
		File f = new File(fileName);
		
		writer = new BufferedWriter(new FileWriter(f));
		writer.write("%\r\n");
		writer.write("@relation '" + new File(fileName).getName() + "'\r\n");
		writer.write("@attribute class {" + className0 + "," + classNameFG + "}\r\n");
		writer.write("@data\r\n");
		for (int p : m1)
			if (p == foreground)
				writer.write(classNameFG + "\r\n");
			else
				writer.write(className0 + "\r\n");
		writer.write("%\r\n");
		writer.close();
	}
}
