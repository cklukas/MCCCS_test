package workflow;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import tools.IO_MCCCS;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.ImageStack;

/**
 * Generates "healthy-image" (AND + INV). Also generates a powerset of the input combinations of images.
 * All these images are then saved.
 * 
 * @author Jean-Michel Pape, Christian Klukas
 */
public class PowerSetGenerator {
	
	public static void main(String[] args) throws IOException, InterruptedException {
		{
			new Settings(true);
		}
		if (args == null || args.length < 2) {
			System.err.println("No [class-count] and / or no [filenames] provided as parameters! Return Code 1");
			System.exit(1);
		} else {
			Image roi = null;
			boolean first = true;
			for (String a : args) {
				if (first) {
					first = false;
					Settings.numberOfClasses = Integer.parseInt(a);
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
				// do sth
				for (File f : fl) {
					// Read data for Training
					IO_MCCCS io = new IO_MCCCS(f);
					ImageStack[] isl = io.readTrainingData(true, false);
					
					// load region of interest
					if (roi == null)
						roi = IO_MCCCS.readImageAbsPath(f.getAbsolutePath() + File.separator + "mask_1.png");
					
					int count = 0;
					// // save dt images as labels
					for (Image ii : isl[0].getImages())
						ii.saveToFile(f.getAbsolutePath() + File.separator + "label_" + count++ + ".png");
					
					Image img = process(isl[0]);
					img = img.io().applyMask(roi).getImage();
					img.saveToFile(f.getAbsolutePath() + File.separator + "label_" + count++ + ".png");
					
					isl[0].initLabels();
					isl[0] = isl[0].getBinaryPowerSet(1);
					
					// roi.show("roi");
					for (Image iii : isl[0].getImages()) {
						String name = iii.getFileName();
						iii = iii.io().applyMask(roi).getImage();
						iii.saveToFile(f.getAbsolutePath() + File.separator + "label_" + count++ + ".png");
					}
					
					if (Settings.debug_IO) {
						for (ImageStack st : isl)
							st.show("debug_IO");
					}
					
				}
			}
		}
	}
	
	protected static Image process(ImageStack st) {
		Image img = null;
		for (int i = 0; i < st.size(); i++) {
			if (i == 0)
				img = st.getImage(i);
			else {
				img = img.io().or(st.getImage(i)).getImage();
			}
		}
		return img.io().bm().invert().getImage();
	}
}
