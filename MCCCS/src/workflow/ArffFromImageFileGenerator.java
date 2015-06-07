package workflow;

import java.io.File;
import java.util.LinkedList;

import org.graffiti.plugin.io.resources.FileSystemHandler;

import tools.ARFFProcessor;
import tools.IO_MCCCS;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.ImageStack;

/**
 * Converts image into an ARFF file.
 * input: channel-count, class-count, filename
 * 
 * @author Jean-Michel Pape
 */
public class ArffFromImageFileGenerator {
	
	public static void main(String[] args) throws Exception {
		{
			new Settings(true);
		}
		if (args == null || args.length < 1) { // [channel-count],
			System.err.println("No parameter for [class-count] and / or no [filenames] provided as parameters! Return Code 1");
			System.exit(1);
		} else {
			int parmIndex = 0;
			for (String a : args) {
				// if (parmCount == 0) {
				// // Settings.numberOfChannels = Integer.parseInt(a);
				// parmCount++;
				// continue;
				// }
				
				if (parmIndex == 0) {
					Settings.numberOfClasses = Math.abs(Integer.parseInt(a));
					parmIndex++;
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
					IO_MCCCS io = new IO_MCCCS(f);
					ImageStack[] isl = io.readTestingData();
					
					if (Settings.debug_IO) {
						for (ImageStack st : isl)
							st.show("debug_IO");
					}
					
					File file = new File(f.getParent() + "/mask_0.png");
					Image mask = null;
					if (file.exists())
						mask = new Image(FileSystemHandler.getURL(file));
					
					ARFFProcessor ac = new ARFFProcessor();
					
					String name;
					// remove file-ending if it is directory
					if (!f.isDirectory()) {
						String name2 = f.getName();
						String[] split = name2.split("\\.");
						name = split[0];
					} else
						name = f.getName();
					
					String path = f.getPath();
					ac.convertImagesToArff(isl[0], path, name + "_" + Settings.numberOfClasses, mask, false, false);
				}
			}
		}
	}
	
}
