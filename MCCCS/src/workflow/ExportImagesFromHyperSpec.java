package workflow;

import java.io.File;
import java.util.LinkedList;

import bsqloader.HyspecLoader;
import bsqloader.HyspecLoader.HyperSpecDataMode;
import de.ipk.ag_ba.image.structures.ImageStack;

public class ExportImagesFromHyperSpec {
	
	/**
	 * Creates separate images from hyper-spectral image data set (BSQ, BIL format).
	 * input: prefix for output filename, overflow threshold or negative value to disable, filenames (input)
	 * 
	 * @author Jean-Michel Pape
	 */
	public static void main(String[] args) throws Exception {
		{
			new Settings(true);
		}
		if (args == null || args.length < 3) {
			System.err
					.println("No [prefix], [overflow threshold, -1 to keep input values, other negative values to divide the output values by the specified number] and [filenames] provided as parameters! Return Code 1");
			System.exit(1);
		} else {
			int idx = 0;
			double overflow = Double.NaN;
			for (String a : args) {
				idx++;
				if (idx == 1)
					continue;
				
				if (idx == 2) {
					overflow = Double.parseDouble(args[1]);
					continue;
				}
				
				LinkedList<File> fl = new LinkedList<>();
				
				fl.add(new File(a));
				
				File data = null;
				File hdr = null;
				HyperSpecDataMode mode = null;
				
				for (File f : fl) {
					for (File ff : f.listFiles()) {
						for (HyperSpecDataMode dataMode : HyperSpecDataMode.values())
							if (ff.getName().endsWith("." + dataMode.getName())) {
								data = ff;
							}
						
						if (ff.getName().endsWith(".hdr"))
							hdr = ff;
					}
				}
				
				if (data != null && hdr != null) {
					HyspecLoader loader = new HyspecLoader(hdr.getPath(), data.getPath());
					ImageStack is = loader.getCubeAsImageStack(overflow);
					is.saveAsSeparateImages(new File(hdr.getParent() + "/"), args[0]);
					is.saveAsLayeredTif(new File(hdr.getPath().split("\\.")[0] + "_stack.tif"));
				}
			}
		}
	}
	
}
