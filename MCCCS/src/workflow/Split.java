package workflow;

import ij.gui.Roi;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import org.Vector2i;
import org.graffiti.plugin.io.resources.FileSystemHandler;

import de.ipk.ag_ba.image.operations.segmentation.ClusterDetection;
import de.ipk.ag_ba.image.operations.segmentation.Segmentation;
import de.ipk.ag_ba.image.structures.Image;

/**
 * Splits leaves within image (objects which all reach a certain top-position and which at the same time
 * reach a certain lower-end position. Objects which don't 'touch' these virtual top and lower borders, which
 * are of less height, are removed. Then each object which fullfills these criteria, a new image with only
 * that specific object is created.
 * Input:
 * image file
 * Output:
 * file_1.png, file_2.png, file_3.png - objects separated
 * 
 * @author Christian Klukas
 */
public class Split {
	
	public static void main(String[] args) throws IOException, Exception {
		{
			new Settings(true);
		}
		if (args == null || args.length == 0) {
			System.err.println("No filenames provided as parameters! Return Code 1");
			System.exit(1);
		} else {
			for (String a : args) {
				File f = new File(a);
				if (!f.exists()) {
					System.err.println("File '" + a + "' could not be found! Return Code 2");
					System.exit(2);
				} else {
					Image i = new Image(FileSystemHandler.getURL(f));
					int[] ipix = i.getAs1A();
					Roi bb = i.io().getBoundingBox();
					int minh = bb.getBounds().height;
					Segmentation ps = new ClusterDetection(i, Settings.back);
					ps.detectClusters();
					// ps.getClusterImage().show("cluster image");
					LinkedList<Integer> validClusterIDs = new LinkedList<>();
					Vector2i[] cd = ps.getClusterDimension();
					for (int c = 0; c < cd.length; c++) {
						if (c == 0)
							continue;
						if (cd[c].y >= minh) {
							validClusterIDs.add(c);
						}
					}
					int[] ci = ps.getImageClusterIdMask();
					int leaf = 0;
					for (Integer vc : validClusterIDs) {
						leaf++;
						int vci = vc;
						int[] rp = new int[ci.length];
						for (int idx = 0; idx < rp.length; idx++) {
							rp[idx] = ci[idx] == vci ?
									ipix[idx] // Settings.foreground
									: Settings.back;
						}
						new Image(i.getWidth(), i.getHeight(), rp)
								.saveToFile(f.getParent() + File.separator + f.getName().substring(0, f.getName().lastIndexOf(".")) + "_" + leaf + ".png");
					}
				}
			}
		}
	}
}
