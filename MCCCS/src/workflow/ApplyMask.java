package workflow;

import java.io.File;
import java.util.LinkedList;

import org.graffiti.plugin.io.resources.FileSystemHandler;

import de.ipk.ag_ba.image.structures.Image;

/**
 * Converts classified ARFF result file to an image by using a labeled mask image.
 * input: filename
 * 
 * @author Jean-Michel Pape
 */
public class ApplyMask {
	
	public static void main(String[] args) throws Exception {
		{
			new Settings(true);
		}
		if (args == null || args.length < 2) {
			System.err.println("No parameter for [filenames] provided as parameters! Return Code 1");
			System.exit(1);
		} else {
			int parmcount = 0;
			LinkedList<File> fl = new LinkedList<>();
			LinkedList<File> fl1 = new LinkedList<>();
			for (String a : args) {
				if (parmcount == 0) {
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
				}
				if (parmcount == 1) {
					if (a.contains("*")) {
						String path = new File(a).getParent();
						for (File f : new File(path).listFiles((fn) -> {
							return fn.getName().startsWith(new File(a).getName().substring(0, new File(a).getName().indexOf("*")));
						})) {
							fl1.add(f);
						}
					} else {
						fl1.add(new File(a));
					}
				}
				parmcount++;
			}
			int idx = 0;
			for (File f : fl) {
				File f2 = fl1.get(idx++);
				Image img = new Image(FileSystemHandler.getURL(f));
				Image roi = new Image(FileSystemHandler.getURL(f2));
				
				img = img.io().applyMask(roi).getImage();
				
				String[] fnames = f.getName().split("\\.");
				String[] f2names = f2.getName().split("\\.");
				
				if (fnames[1].compareTo(f2names[1]) == -1)
					System.err.println("Filetypes do not match!");
				
				img.saveToFile(f.getParent() + File.separator + fnames[0] + "_" + f2names[0] + "." + fnames[1]);
			}
		}
	}
}
