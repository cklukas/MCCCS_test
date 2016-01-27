package workflow;

import java.io.File;
import java.util.LinkedList;
import java.util.TreeMap;

import org.graffiti.plugin.io.resources.FileSystemHandler;

import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.ImageStack;
import ij.ImagePlus;
import tools.ARFFProcessor;
import tools.IO_MCCCS;
import tools.IO_MCCCS.ReadMode;

/**
 * Converts image into an ARFF file.
 * input: class-count, filename
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
			
			boolean NG = true;
			ARFFProcessor ac = new ARFFProcessor();
			
			for (File f : fl) {
				String name;
				// remove file-ending if it is directory
				if (!f.isDirectory()) {
					String name2 = f.getName();
					String[] split = name2.split("\\.");
					name = split[0];
				} else
					name = f.getName();
				String path = f.getPath();
				
				if (NG) {
					String filename = ReadMode.IMAGES.name();
					String extension = ".tif";
					String[] flist = f.list();
					
					// read mask
					File file = new File(f.getParent() + "/mask_0.png");
					Image mask = null;
					if (file.exists())
						mask = new Image(FileSystemHandler.getURL(file));
						
					// search for all files channel*.tif
					TreeMap<String, String> hmap = new TreeMap<String, String>();
					for (String s : flist) {
						if (!s.endsWith(extension))
						continue;
						if (!s.startsWith(filename))
						continue;
						
						hmap.put(s.substring(0, s.lastIndexOf(".")), s);
					}
					
					// read images and create arff file
					for (int idx = 0; idx < hmap.size(); idx++) {
						final int idxxx = idx;
						String idxxxs = idxxx + "";
						try {
						ImagePlus ip = null;
						String fname = hmap.get(filename + idxxxs);
						String pathOrURL = f.getAbsolutePath() + File.separator + fname;
						
						ac.convertImagesToArffNG(ip, path, name + "_" + Settings.numberOfClasses, mask, false);
						} catch (Exception e) {
						throw new RuntimeException(e);
						}
					}
					// TODO Merge ARFFs
				} else {
					
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
						
					ac.convertImagesToArff(isl[0], path, name + "_" + Settings.numberOfClasses, mask, false);
				}
			}
			}
		}
	}
	
}
