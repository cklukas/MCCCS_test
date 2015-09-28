package lsc;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;

import org.StringManipulationTools;
import org.graffiti.plugin.io.resources.FileSystemHandler;

import de.ipk.ag_ba.image.operations.segmentation.ClusterDetection;
import de.ipk.ag_ba.image.structures.Image;

/**
 * @author pape, klukas
 */
public class LabelRegions {
	
	public static void main(String[] args) throws IOException, Exception {
		if (args == null || args.length != 1) {
			System.err
					.println("No [path] and [backgroundcolor] provided as parameters! Return Code 1"); // label file] [edge file] [result file
			System.exit(1);
		} else {
			String[] files = new File(args[0]).list(new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".png");
				}
			});
			
			Arrays.sort(files);
			
			for (int i = 0; i < files.length; i++) {
				File labelFile = new File(new File(args[0]).getAbsolutePath() + File.separator + files[i]);
				Image img = new Image(FileSystemHandler.getURL(labelFile)).show("inp", false);// new File(args[1])));
				
				// alternative
				// RegionLabeling rl = new RegionLabeling(img, false, -16777216, 2);
				// rl.detectClusters();
				// rl.getClusterImage().show("rl");
				
				ClusterDetection cd = new ClusterDetection(img, -16777216);
				cd.detectClusters();
				cd.getClusterImage().saveToFile(labelFile.getParent() + File.separator
						+ StringManipulationTools.removeFileExtension(labelFile.getName())
						+ "_labeled.png");// args[2]);
			}
		}
	}
}
