package workflow;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import org.graffiti.plugin.io.resources.FileSystemHandler;

import PSI_IO.ConvertTools;
import de.ipk.ag_ba.image.structures.Image;
import ij.ImagePlus;
import ij.process.ImageProcessor;

/**
 * Command to convert .dumm (raw image files for PSII measurements) and
 *  .fimg (result image files, including the results for a feature as 
 *  calculated during PSII analysis) to .tif image files.
 *  
 *  input: folder including files for conversion
 *  output: same folder as used for input
 * 
 * @author pape
 *
 */
public class ConvertPSIIToTif {
	
	/**
	 * 
	 * @param args [path to folder]
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		LinkedList<File> fl = new LinkedList<>();
		
		for (int idx = 0; idx < args.length; idx++) {
			String a = args[idx];
			String path = new File(a).getAbsolutePath();
			for (File f : new File(path).listFiles()) {
				if (!f.getName().endsWith(".dumm") && !f.getName().endsWith(".fimg"))
					continue;
				else
					fl.add(f);
			}
		}
		
		for (File inp : fl) {
			ImageProcessor ip = null;
		
			if(inp.getName().endsWith(".dumm"))
				ip = ConvertTools.dummToTif(inp);
		
			if(inp.getName().endsWith(".fimg"))
				ip = ConvertTools.fimgToTif(inp);
		
			if(ip != null) {
				ImagePlus imp = new ImagePlus("MCCCS conversion", ip);
				String outf = inp.getParent() + File.separator + inp.getName().split("\\.")[0] + ".tif";
				new Image(imp).saveToFile(outf);
			}
		}
	}
}
