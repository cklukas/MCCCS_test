package workflow;

import ij.ImagePlus;
import ij.plugin.RGBStackMerge;
import ij.process.ImageConverter;

import java.io.File;
import java.util.LinkedList;

import org.graffiti.plugin.io.resources.FileSystemHandler;

import de.ipk.ag_ba.image.structures.Image;

/**
 * This command combines three images 'channel_0, channel_1, channel_2' (0 = red, 1 = green, 2 = blue) to an RGB composite image.
 * 
 * @author Jean-Michel Pape
 */
public class MakeRGBComposite {
	
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
					File[] fList = f.listFiles();
					Image r = null, g = null, b = null;
					String extension = "";
					boolean first = true;
					for (File i : fList) {
						
						String n = i.getName().split("\\.")[0];
						String ext = i.getName().split("\\.")[1];
						
						if (n == "" || ext == "")
							continue;
						
						if (n.equals("channel_0")) {
							r = new Image(FileSystemHandler.getURL(new File(f.getPath() + File.separator + "channel_0." + ext)));
							
							if (first) {
								extension = ext;
								first = false;
							} else {
								if (!extension.equals(ext))
									System.err.println("Extension missmatch!");
							}
						}
						
						if (n.equals("channel_1")) {
							g = new Image(FileSystemHandler.getURL(new File(f.getPath() + File.separator + "channel_1." + ext)));
							
							if (first) {
								extension = ext;
								first = false;
							} else {
								if (!extension.equals(ext))
									System.err.println("Extension missmatch!");
							}
						}
						
						if (n.equals("channel_2")) {
							b = new Image(FileSystemHandler.getURL(new File(f.getPath() + File.separator + "channel_2." + ext)));
							
							if (first) {
								extension = ext;
								first = false;
							} else {
								if (!extension.equals(ext))
									System.err.println("Extension missmatch!");
							}
						}
					}
					
					if (r != null && g != null && b != null) {
						Image composite = combine(r, g, b);
						composite.saveToFile(f.getAbsolutePath() + "/composite." + "png");
					} else {
						System.err.println("No RGB images present!");
					}
				}
			}
		}
	}
	
	public static Image combine(Image imgR, Image imgG, Image imgB) {
		ImagePlus[] ipList = new ImagePlus[] { imgR.getAsImagePlus(), imgG.getAsImagePlus(), imgB.getAsImagePlus() };
		ImagePlus comb = RGBStackMerge.mergeChannels(ipList, true);
		ImageConverter ic = new ImageConverter(comb);
		ic.convertToRGB();
		return new Image(comb);
	}
}
