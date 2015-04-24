package workflow;

import java.io.File;
import java.util.LinkedList;

import org.graffiti.plugin.io.resources.FileSystemHandler;

import tools.ARFFProcessor;
import de.ipk.ag_ba.image.structures.Image;

/**
 * Converts classified ARFF result file to an image by using a labeled mask image.
 * input: filename
 * 
 * @author Jean-Michel Pape
 */
public class ApplyClass0ToImage {
	
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
					
					ARFFProcessor ac = new ARFFProcessor();
					String name2 = f.getName();
					String[] split = name2.split("\\.");
					String name = split[0];
					ac.convertArffToImage(f.getParent(), name, mask, true, false, false);
				}
			}
		}
	}
}
