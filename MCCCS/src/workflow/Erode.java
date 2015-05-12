package workflow;

import java.io.File;
import java.util.LinkedList;

import org.graffiti.plugin.io.resources.FileSystemHandler;

import de.ipk.ag_ba.image.structures.Image;

/**
 * This command performs the erode operation on a given input image (black pixels will be regarded as foreground, white as background).
 * 
 * @author Jean-Michel Pape
 */
public class Erode {
	
	public static void main(String[] args) throws Exception {
		{
			new Settings(true);
		}
		if (args == null || args.length < 1) {
			System.err.println("No parameter for [filenames] provided as parameters! Return Code 1");
			System.exit(1);
		} else {
			for (String a : args) {
				LinkedList<File> fl = new LinkedList<>();
				fl.add(new File(a));
				
				for (File f : fl) {
					Image img = null;
					
					img = new Image(FileSystemHandler.getURL(new File(f.getAbsolutePath())));
					
					if (img != null) {
						Image erode = img.io().bm().erode(10).getImage();
						String name = f.getName().split("\\.")[0];
						erode.saveToFile(f.getParent() + File.separator + name + "_erode." + "png");
					} else {
						System.err.println("Input image is null.");
					}
				}
			}
		}
	}
}
