package workflow;

import ij.process.ImageProcessor;

import java.io.File;
import java.util.LinkedList;

import org.graffiti.plugin.io.resources.FileSystemHandler;

import de.ipk.ag_ba.image.structures.Image;

/**
 * Rotates an image in left or right direction, given by parameter 1 [l,r].
 * 
 * @author Jean-Michel Pape
 */
public class Rotate {
	
	public static void main(String[] args) throws Exception {
		{
			new Settings(true);
		}
		
		String direction = "";
		
		if (args == null || args.length < 2) {
			System.err.println("No parameter for [rotate-angle] and / or no [filenames] provided as parameters! Return Code 1");
			System.exit(1);
		} else {
			int parmCount = 0;
			for (String a : args) {
				if (parmCount == 0) {
					direction = a;
					parmCount++;
					continue;
				}
				
				LinkedList<File> fl = new LinkedList<>();
				if (a.contains("*")) {
					String path = new File(a).getParent();
					for (File f : new File(path).listFiles((fn) -> {
						return fn.getName().startsWith(new File(a).getName().substring(0, new File(a).getName().indexOf("*")));
					})) {
						fl.add(f);
					}
				} else {
					fl.add(new File(a));
				}
				for (File f : fl) {
					
					Image mask = new Image(FileSystemHandler.getURL(f));
					
					String name2 = f.getName();
					String[] split = name2.split("\\.");
					String name = split[0];
					
					ImageProcessor proc = mask.getAsImagePlus().getProcessor();
					if (direction.equals("l"))
						new Image(proc.rotateLeft()).saveToFile(f.getParent() + File.separator + name + "_rot_" + direction + ".tif");
					else
						if (direction.equals("r"))
							new Image(proc.rotateRight()).saveToFile(f.getParent() + File.separator + name + "_rot_" + direction + ".tif");
						else
							System.err.println("Parameter value for rotation is not supported.");
				}
			}
		}
	}
}
