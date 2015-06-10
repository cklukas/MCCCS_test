package lsc;

import ij.ImagePlus;
import ij.process.ImageConverter;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import org.graffiti.plugin.io.resources.FileSystemHandler;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.Image;

/**
 * Creates foreground (and background => optionally) masks (BW images) for MCCCS training.
 * 
 * @author Jean-Michel Pape
 */
public class GetForgroundMaskFromGT {
	
	public static void main(String[] args) throws IOException, Exception {
		if (args == null || args.length < 1) {
			System.err
					.println("No [filenames] provided as parameters! Return Code 1");
			System.exit(1);
		} else {
			ImageOperation.BACKGROUND_COLORint = Color.BLACK.getRGB();
			ImageOperation.BACKGROUND_COLOR = new Color(
					ImageOperation.BACKGROUND_COLORint);
			for (String a : args) {
				LinkedList<File> fl = new LinkedList<>();
				if (a.contains("*")) {
					String path = new File(a).getParent();
					for (File f : new File(path).listFiles((fn) -> {
						String ss = fn.getName().split("_")[1];
						String sss = new File(a).getName().substring(
								new File(a).getName().indexOf("*") + 1);
						return ss.startsWith(sss);
					})) {
						fl.add(f);
					}
				} else {
					fl.add(new File(a));
				}
				
				for (File f : fl) {
					ImagePlus input = new Image(FileSystemHandler.getURL(f))
							.getAsImagePlus();
					ImageConverter ic1 = new ImageConverter(input);
					ic1.convertToGray8();
					input.getProcessor().threshold(0);
					String name = f.getPath().substring(0,
							f.getPath().lastIndexOf("."));
					// new Image(input).show("foreground");
					Image i = new Image(input);
					// i.saveToFile(name + "_background.png");
					i.io().invertIgnoresBackground().getImage().saveToFile(name + "_foreground.png");
					// System.out.println("save: " + f.getName());
				}
			}
		}
	}
}
