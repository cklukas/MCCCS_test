package workflow;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedList;

import org.Vector2i;
import org.graffiti.plugin.io.resources.FileSystemHandler;

import de.ipk.ag_ba.image.operation.canvas.ImageCanvas;
import de.ipk.ag_ba.image.operations.segmentation.ClusterDetection;
import de.ipk.ag_ba.image.operations.segmentation.Segmentation;
import de.ipk.ag_ba.image.structures.Image;
import iap.blocks.image_analysis_tools.leafClustering.CurveAnalysis;
import ij.gui.Roi;

/**
 * Splits leaves within image (objects which all reach a certain top-position and which at the same time
 * reach a certain lower-end position. Objects which don't 'touch' these virtual top and lower borders, which
 * are of less height, are removed. Then each object which full-fills these criteria, a new image with only
 * that specific object is created.
 * Input:
 * image file
 * Output:
 * file_1.png, file_2.png, file_3.png - objects separated, sorted by x-position of the segment centers
 * 
 * @author Jean-Michel Pape, Christian Klukas
 */
public class SplitHistBased {
	
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
					Image img = new Image(FileSystemHandler.getURL(f));
					int[][] img_2d = img.getAs2A();
					
					// accumulate histogram
					double[] hist = new double[img.getWidth()];
					
					for (int x = 0; x < img.getWidth(); x++)
						for (int y = 0; y < img.getHeight(); y++) {
							if (img_2d[x][y] == Settings.back)
							hist[x]++;
						}
						
					// for (double dd : hist)
					// System.out.println(dd);
					
					// max search
					int[] peaks = null;
					peaks = CurveAnalysis.findMaximaIJ(hist, 150, true);
					
					// may not good here
					// peaks = CurveAnalysis.summarizeMaxima(peaks, peaks.length, 20, CurveAnalysis.SummarizeMode.SUM);
					
					// draw separation lines
					ImageCanvas ic = new ImageCanvas(img);
					int off = 5;
					for (int pos : peaks) {
						for (int offi = pos - off; offi < pos + off; offi++)
							ic.drawRectangle(offi, 0, 1, img.getHeight(), Settings.back, 1);
					}
					
					Image separatedimg = ic.getImage();
					
					int[] separated_1d = separatedimg.getAs1A();
					Roi bb = img.io().getBoundingBox();
					
					// check for outlier deletion (height and area)
					int minh = (int) (bb.getBounds().height * 0.85);
					int esize = (int) (minh * 0.02);
					Segmentation ps = new ClusterDetection(separatedimg.io().bm().erode(esize).getImage(), Settings.back);
					ps.detectClusters();
					ps.getClusterImage().show("cluster image");
					LinkedList<Integer> validClusterIDs = new LinkedList<>();
					int[] cAreas = ps.getClusterSize();
					Vector2i[] cd = ps.getClusterDimension();
					
					// convert areas to colection
					int minAreaThreshold = 10000;
					int maxAreaThreshold = 10000000;
					
					// check for height and area
					for (int c = 0; c < cd.length; c++) {
						if (c == 0)
							continue;
							
						if (cd[c].y >= minh && cAreas[c] > minAreaThreshold && cAreas[c] < maxAreaThreshold) {
							validClusterIDs.add(c);
						}
					}
					
					// sort valid clusters by x position
					Vector2i[] centers = ps.getClusterCenterPoints();
					LinkedList<Vector2i> validIDvsCenterXList = new LinkedList<>();
					
					for (int id : validClusterIDs) {
						validIDvsCenterXList.add(new Vector2i(id, centers[id].x));
					}
					
					validIDvsCenterXList.sort(new Comparator<Vector2i>() {
						
						@Override
						public int compare(Vector2i o1, Vector2i o2) {
							if (o1.y < o2.y)
							return -1;
							else
							if (o1.y == o2.y)
								return 0;
							else
								return 1;
						}
					});
					
					int index = 0;
					for (Vector2i ii : validIDvsCenterXList) {
						validClusterIDs.set(index++, ii.x);
					}
					
					int[] ci = ps.getImageClusterIdMask();
					int leaf = 0;
					for (Integer vc : validClusterIDs) {
						leaf++;
						int vci = vc;
						int[] rp = new int[ci.length];
						for (int idx = 0; idx < rp.length; idx++) {
							rp[idx] = ci[idx] == vci ? separated_1d[idx] // Settings.foreground
								: Settings.back;
						}
						new Image(img.getWidth(), img.getHeight(), rp)
							.saveToFile(f.getParent() + File.separator + f.getName().substring(0, f.getName().lastIndexOf(".")) + "_" + leaf + ".png");
					}
				}
			}
		}
	}
}
