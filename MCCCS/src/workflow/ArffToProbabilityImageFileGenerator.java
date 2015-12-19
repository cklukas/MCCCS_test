package workflow;

import java.io.File;
import java.util.LinkedList;

import org.graffiti.plugin.io.resources.FileSystemHandler;

import de.ipk.ag_ba.image.structures.Image;
import tools.ARFFProcessor;

/**
 * Converts classified ARFF result file (including pixel-probabilities for each class) to an grayscale-image (use of FG mask is possible).
 * 
 * @author Jean-Michel Pape
 */
public class ArffToProbabilityImageFileGenerator {
	
	public static void main(String[] args) throws Exception {
		{
			new Settings(true);
		}
		if (args == null || args.length < 3) {
			System.err
					.println(
					"No parameter for [channel-count (png output), -channel-count (negative for float tiff output)] and / or [percentage of acceptance (0 .. 1)] and / or no [filenames] provided as parameters! Return Code 1");
			System.exit(1);
		} else {
			int parmCount = 0;
			boolean floatOut = false;
			float percentageForAcceptance = 0.0f;
			for (String a : args) {
				if (parmCount == 0) {
					Settings.numberOfClasses = Integer.parseInt(a);
					if (Settings.numberOfClasses < 0) {
						Settings.numberOfClasses = -Settings.numberOfClasses;
						floatOut = true;
					}
					parmCount++;
					continue;
				}
				
				if (parmCount == 1) {
					percentageForAcceptance = Float.parseFloat(a);
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
					
					ARFFProcessor ac = new ARFFProcessor();
					String name2 = f.getName();
					String[] split = name2.split("\\.");
					String name = split[0];
					if (floatOut)
						ac.convertArffToImageMultiLabelFloatImage(f.getParent(), mask.getWidth(), mask.getHeight(), name, false);
					else
						ac.convertArffToImageMultiLabel(f.getParent(), name, mask, false, percentageForAcceptance, false);
				}
			}
		}
	}
}
