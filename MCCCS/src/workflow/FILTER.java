package workflow;

import java.io.File;
import java.io.IOException;
import java.util.TreeMap;

import org.StringManipulationTools;
import org.graffiti.plugin.io.resources.FileSystemHandler;

import tools.ImageFeatureExtraction;
import de.ipk.ag_ba.image.structures.Image;

/**
 * Based on an input image an image operation is performed and a target result file is saved..
 * 
 * @author Christian Klukas
 */
public class FILTER {
	
	public static void main(String[] args) throws IOException, Exception {
		{
			new Settings(true);
		}
		if (args == null || args.length != 5) {
			if (args != null) {
				for (int pi = 0; pi < args.length; pi++)
					System.out.println("param " + pi + ": '" + args[pi] + "'");
			}
			System.err
					.println("First parameter 'input image', second parameter 'output image', third parameter 'int parameter for opertion', fourth 'double param', fifth parameter 'operation mode'! Return Code 1");
			System.out.println("Operation modes: " + StringManipulationTools.getStringList(ImageFeatureExtraction.FeatureMode.values(), ", "));
			System.exit(1);
		} else {
			File f = new File(args[0]);
			if (!f.exists()) {
				System.err.println("File '" + f.getPath() + "' could not be found! Return Code 2");
				System.exit(2);
			} else {
				Image i;
				i = new Image(FileSystemHandler.getURL(f));
				TreeMap<String, Image> res = new ImageFeatureExtraction().processImage(i
						, Integer.parseInt(args[2]),
						Double.parseDouble(args[3]),
						ImageFeatureExtraction.FeatureMode.valueOf(args[4]));
				for (String sop : res.keySet())
					res.get(sop).saveToFile(args[1].substring(0, args[1].lastIndexOf(".")) + "_" + sop + args[1].substring(args[1].lastIndexOf(".")));
			}
		}
	}
}
