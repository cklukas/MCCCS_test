package workflow;

import iap.blocks.image_analysis_tools.methods.RegionLabeling;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.TreeMap;

import org.AttributeHelper;
import org.StringManipulationTools;
import org.Vector2i;
import org.graffiti.plugin.io.resources.FileSystemHandler;

import de.ipk.ag_ba.image.operation.PositionAndColor;
import de.ipk.ag_ba.image.operation.canvas.ImageCanvas;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;

/**
 * Compared to the simple Quantify command (works for one object in an image), it is possible to analyze and distinguish several separated objects in an image.
 * The results are saved into a .csv file and a debug image.
 * 
 * @param output
 *           mode (0 = percentage, 1 = absolute values)
 * @param image
 *           file
 * @return csv file
 * @return debug image
 * @author Jean-Michel Pape, Christian Klukas
 */
public class Quantify_Enhanced {
	
	public static void main(String[] args) throws Exception {
		{
			new Settings(true);
		}
		if (args == null || args.length < 2) {
			System.err.println("No output mode [0 - percentage, 1 - absolute pixel numbers]  and/or filename provided as parameters! Return Code 1");
			System.exit(1);
		} else {
			int outMode = Integer.parseInt(args[0]);
			
			File f = new File(args[1]);
			
			if (f.exists()) {
				Image image = new Image(FileSystemHandler.getURL(f));
				
				RegionLabeling rl = new RegionLabeling(image, false, Color.WHITE.getRGB(), 0);
				rl.detectClusters();
				
				Vector2i[] c_center = rl.getClusterCenterPoints();
				Vector2i[] c_dims = rl.getClusterDimension();
				int[] c_sizes = rl.getClusterSize();
				long fgArea = rl.getForegroundPixelCountl();
				
				// evaluate colors
				LinkedList<ArrayList<PositionAndColor>> rl_list = rl.getRegionList();
				LinkedList<TreeMap<Integer, Integer>> quant = new LinkedList<>();
				int back[] = { Settings.back, Color.WHITE.getRGB() };
				// System.out.println(c_sizes.length);
				for (ArrayList<PositionAndColor> cluster : rl_list) {
					TreeMap<Integer, Integer> diseaseSymptomId2Area = new TreeMap<>();
					
					for (PositionAndColor p : cluster) {
						if (p.intensityInt != back[0] && p.intensityInt != back[1]) {
							if (!diseaseSymptomId2Area.containsKey(p.intensityInt))
								diseaseSymptomId2Area.put(p.intensityInt, 0);
							diseaseSymptomId2Area.put(p.intensityInt, diseaseSymptomId2Area.get(p.intensityInt) + 1);
						}
					}
					quant.add(diseaseSymptomId2Area);
				}
				
				// put into one structure for sorting
				LinkedList<ClusterFeatures> c_features = new LinkedList<>();
				int max_size = 0;
				for (int idx = 0; idx < c_center.length; idx++) {
					c_features.add(new ClusterFeatures(c_sizes[idx], c_dims[idx], c_center[idx], quant.get(idx)));
					if (c_sizes[idx] > max_size)
						max_size = c_sizes[idx];
				}
				
				// remove background cluster
				for (ClusterFeatures cf : c_features) {
					if (cf.size == max_size)
						c_features.remove(cf);
					break;
				}
				
				// sort by column (left_top = first)
				c_features.sort(new Comparator<ClusterFeatures>() {
					
					@Override
					public int compare(ClusterFeatures o1, ClusterFeatures o2) {
						// 0 -> same, -1 -> smaller ...
						if (o1.x < o2.x)
							return -1;
						else
							if (o1.x > o2.x)
								return 1;
							else
								if (o1.x == o2.x) {
									if (o1.y < o2.y)
										return -1;
									else
										if (o1.y > o2.y)
											return 1;
										else
											return 0;
								}
						return 0;
					}
				});
				
				// mark result image (for debug purposes)
				TextFile tf = exportResults(outMode, f, image, fgArea, c_features);
				
				try {
					tf.write(f.getParent() + File.separator + f.getName() + "_quantified.csv");
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			} else {
				System.out.println("Cant read classification result image for " + f.getParent() + "!");
			}
		}
	}
	
	private static TextFile exportResults(int outMode, File f, Image image, long fgArea,
			LinkedList<ClusterFeatures> c_features) {
		ImageCanvas canvas = new ImageCanvas(image);
		int idx = 0;
		int w = (int) (0.0125 * image.getWidth());
		
		canvas.text(100, 100, "Foreground-area: " + fgArea, Color.BLACK);
		
		for (ClusterFeatures c : c_features) {
			int off = -60;
			int wh = w / 2;
			int hh = (int) (w * 2.5);
			canvas.fillRect(c.x - wh, c.y - wh + off, hh, 20, Color.lightGray.getRGB(), 0.25);
			canvas.text(c.x - wh, c.y - wh + off + 20, "Index: " + idx++, Color.RED);
			off += 20;
			canvas.fillRect(c.x - wh, c.y - wh + off, hh, 20, Color.lightGray.getRGB(), 0.25);
			canvas.text(c.x - wh, c.y - wh + off + 20, "Center X: " + c.x, Color.RED);
			off += 20;
			canvas.fillRect(c.x - wh, c.y - wh + off, hh, 20, Color.lightGray.getRGB(), 0.25);
			canvas.text(c.x - wh, c.y - wh + off + 20, "Center Y: " + c.y, Color.RED);
			for (Integer cc : c.quant.keySet()) {
				off += 20;
				canvas.fillRect(c.x - wh, c.y - wh + off, hh, 20, Color.lightGray.getRGB(), 0.25);
				canvas.text(c.x - wh, c.y - wh + off + 20, AttributeHelper.getColorName(new Color(cc)) + ": " + c.quant.get(cc), Color.RED);
			}
			// check ratio
			double ratio = (double) c.dim.x / (double) c.dim.y;
			off += 20;
			if (ratio < 1.0) {
				canvas.fillRect(c.x - wh, c.y - wh + off, hh, 20, Color.RED.getRGB(), 0.35);
				canvas.text(c.x - wh, c.y - wh + off + 20, "wh-ratio: " + StringManipulationTools.formatNumber(ratio, "0.000"), Color.WHITE);
			} else {
				canvas.fillRect(c.x - wh, c.y - wh + off, hh, 20, Color.lightGray.getRGB(), 0.25);
				canvas.text(c.x - wh, c.y - wh + off + 20, "wh-ratio: " + StringManipulationTools.formatNumber(ratio, "0.000"), Color.RED);
			}
		}
		// save debug image
		image.saveToFile(f.getParent() + File.separator + "quantify_debug_" + f.getName());
		
		// write to text file
		TextFile tf = new TextFile();
		if (outMode == 0) {
			int id = 0;
			for (ClusterFeatures c : c_features) {
				long overallCluPixel = c.size;
				String fileName = f.getParent() + File.separator + f.getName() + "\t" + id + "\t";
				tf.add(fileName + "foreground_area_pixel" + "\t" + overallCluPixel);
				for (Integer cc : c.quant.keySet()) {
					String colorName = AttributeHelper.getColorName(new Color(cc));
					
					if (overallCluPixel != 0) {
						// BigDecimal val = new BigDecimal(100).multiply(new BigDecimal(c.quant.get(cc))).divide(new BigDecimal(fgArea), RoundingMode.HALF_UP);
						double val = 100d * c.quant.get(cc) / overallCluPixel;
						tf.add(fileName + "class_area_percent_" + colorName + "\t" + val);
					}
				}
				// check ratio
				double ratio = (double) c.dim.x / (double) c.dim.y;
				tf.add(fileName + "cluster_ratio" + "\t" + ratio);
				id++;
			}
		} else {
			int id = 0;
			for (ClusterFeatures c : c_features) {
				long overallCluPixel = c.size;
				String fileName = f.getParent() + File.separator + f.getName() + "\t" + id + "\t";
				tf.add(fileName + "foreground_area_pixel" + "\t" + overallCluPixel);
				for (Integer cc : c.quant.keySet()) {
					String colorName = AttributeHelper.getColorName(new Color(cc));
					tf.add(fileName + "class_area_percent_" + colorName + "\t" + c.quant.get(cc));
				}
				id++;
			}
		}
		return tf;
	}
}

class ClusterFeatures {
	int size;
	int min_WH;
	Vector2i dim;
	int x;
	int y;
	TreeMap<Integer, Integer> quant;
	
	public ClusterFeatures(int size, Vector2i c_dims, Vector2i center, TreeMap<Integer, Integer> quant) {
		this.size = size;
		this.dim = c_dims;
		this.x = center.x;
		this.y = center.y;
		this.quant = quant;
	}
}
