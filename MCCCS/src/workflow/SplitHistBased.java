package workflow;

import iap.blocks.image_analysis_tools.leafClustering.CurveAnalysis;
import ij.gui.Roi;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import org.Vector2i;
import org.graffiti.plugin.io.resources.FileSystemHandler;

import de.ipk.ag_ba.image.operation.canvas.ImageCanvas;
import de.ipk.ag_ba.image.operations.segmentation.ClusterDetection;
import de.ipk.ag_ba.image.operations.segmentation.Segmentation;
import de.ipk.ag_ba.image.structures.Image;

/**
 * Splits leaves within image (objects which all reach a certain top-position and which at the same time
 * reach a certain lower-end position. Objects which don't 'touch' these virtual top and lower borders, which
 * are of less height, are removed. Then each object which full-fills these criteria, a new image with only
 * that specific object is created.
 * Input:
 * image file
 * Output:
 * file_1.png, file_2.png, file_3.png - objects separated
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
					Image i = new Image(FileSystemHandler.getURL(f));
					int[][] ipix = i.getAs2A();
					
					// akkumulate histogram
					double[] hist = new double[i.getWidth()];
					
					for (int x = 0; x < i.getWidth(); x++)
						for (int y = 0; y < i.getHeight(); y++) {
							if (ipix[x][y] == Settings.back)
								hist[x]++;
						}
					
					// for (double dd : hist)
					// System.out.println(dd);
					
					// max search
					int[] peaks = null;
					peaks = CurveAnalysis.findMaximaIJ(hist, 200, true);
					// peaks = CurveAnalysis.summarizeMaxima(peaks, peaks.length, 20, CurveAnalysis.SummarizeMode.SUM);
					
					// draw separation lines
					ImageCanvas ic = new ImageCanvas(i);
					int off = 5;
					for (int pos : peaks) {
						for (int offi = pos - off; offi < pos + off; offi++)
							ic.drawRectangle(offi, 0, 1, i.getHeight(), Settings.back, 1);
					}
					
					Image separatedimg = ic.getImage();
					
					int[] ipixx = separatedimg.getAs1A();
					Roi bb = i.io().getBoundingBox();
					int minh = (int) (bb.getBounds().height * 0.85);
					int esize = (int) (minh * 0.02);
					Segmentation ps = new ClusterDetection(separatedimg.io().bm().erode(esize).getImage(), Settings.back);
					ps.detectClusters();
					ps.getClusterImage().show("cluster image");
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
									ipixx[idx] // Settings.foreground
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
