package lsc;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.Vector2i;
import org.graffiti.plugin.io.resources.FileSystemHandler;

import workflow.Settings;
import de.ipk.ag_ba.image.structures.Image;

/**
 * Split colored regions. The split is continued interatively, until the target
 * count of regions is created. The color of the split regions is determined
 * from the source region color, region one gets 75% of saturation, the other
 * gets 75% of brightness.
 * 
 * Heuristics: Select the most uncompact region. It is split by a straight line
 * so that the division creates two regions, which have together the highest
 * compactness.
 * 
 * Important: Each color in the background should be a single connected
 * component, it is assumed that each region is labeled in a different color.
 * 
 * @author Christian Klukas
 */
public class SplitUncompactRegions {

	public static void main(String[] args) throws Exception {

		boolean alternative_strategy = false;

		boolean debug = false;
		{
			new Settings(false);
		}
		if (args == null || args.length != 3) {
			System.err.println("No parameter [8 bit rgb, colored input image] [result image] [target count (may be floating point)] provided as parameters! Return Code 1");
			System.exit(1);
		} else {
			Image img = new Image(FileSystemHandler.getURL(new File(args[0])));
			double targetCount = Double.parseDouble(args[2]);
			int w = img.getWidth();
			int h = img.getHeight();
			int[][] ia = img.getAs2A();

			// enumerate region pixels
			HashMap<Integer, ArrayList<Vector2i>> color2region = new HashMap<Integer, ArrayList<Vector2i>>();
			findRegions(w, h, ia, color2region);
			int nnn = 0;
			while (color2region.size() < Math.floor(targetCount)) {
				if (debug)
					System.out.println("ROUND " + (++nnn));
				if (debug)
					img.io().stat().printColorCodes(true);
				// detect region borders (8-neighbourhood)
				HashMap<Integer, ArrayList<Vector2i>> color2regionOutline = new HashMap<Integer, ArrayList<Vector2i>>();
				for (int color : color2region.keySet()) {
					ArrayList<Vector2i> region = color2region.get(color);
					for (Vector2i pix : region) {
						if (neighbourCountDifferentToColor(color, pix, ia, w, h) > 0) {
							if (!color2regionOutline.containsKey(color))
								color2regionOutline.put(color, new ArrayList<>());
							color2regionOutline.get(color).add(pix);
						}
					}
				}

				// calculate compactness of regions
				HashMap<Integer, Double> color2compactness = new HashMap<Integer, Double>();
				for (int color : color2region.keySet()) {
					ArrayList<Vector2i> region = color2region.get(color);
					ArrayList<Vector2i> regionOutline = color2regionOutline.get(color);

					int borderPixels = regionOutline.size();
					int filledArea = region.size();

					double c = 4 * Math.PI / (borderPixels * borderPixels / filledArea);
					color2compactness.put(color, c);
				}

				// select split regions, greedy, select the region which is most
				// uncompact.
				double minCompactness = Double.MAX_VALUE;
				Integer colorOfRegionWithMinimumCompactness = null;
				for (int color : color2compactness.keySet()) {
					if (color2regionOutline.get(color).size() < 12)
						continue; // such small region should not be split
					double compactness = color2compactness.get(color);
					if (compactness < minCompactness) {
						minCompactness = compactness;
						colorOfRegionWithMinimumCompactness = color;
					}
				}

				// split along a straight line, so that the two regions
				// compactness is maximized
				if (colorOfRegionWithMinimumCompactness == null) {
					System.out.println("INFO: No region to split available. Can't reach target region count.");
					System.exit(0);
				}
				ArrayList<Vector2i> outline = color2regionOutline.get(colorOfRegionWithMinimumCompactness);
				Vector2i bestStartP = null;
				Vector2i bestEndP = null;
				double maxComp = -Double.MAX_VALUE;
				for (Vector2i startP : outline) {
					for (Vector2i endP : outline) {
						if (startP.distance(endP) < 3)
							continue;
						double c = splitRegionGetCompactnessSum(colorOfRegionWithMinimumCompactness, color2region, color2regionOutline, startP, endP, ia, false);
						if (c > maxComp) {
							bestStartP = startP;
							bestEndP = endP;
						}
					}
				}

				if (bestStartP == null) {
					System.out.println("Found no split line for region with lowest compactness. Can't continue processing.");
					System.exit(0);
				}

				splitRegionGetCompactnessSum(colorOfRegionWithMinimumCompactness, color2region, color2regionOutline, bestStartP, bestEndP, ia, true);

				img = new Image(ia);
				findRegions(w, h, ia, color2region);
			}

			new Image(ia).saveToFile(args[1]);
		}
	}

	private static void splitRegionGetCompactnessSum(Integer colorOfRegionWithMinimumCompactness, HashMap<Integer, ArrayList<Vector2i>> color2region,
			HashMap<Integer, ArrayList<Vector2i>> color2regionOutline, Vector2i bestStartP, Vector2i bestEndP, int[][] ia, boolean b) {
		// position = sign( (Bx-Ax)*(Y-Ay) - (By-Ay)*(X-Ax) )
		// ==> -1 / 0 on line / +1
	}

	private static void findRegions(int w, int h, int[][] ia, HashMap<Integer, ArrayList<Vector2i>> color2region) {
		color2region.clear();
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				int c = ia[x][y];
				if (c != Settings.back) {
					if (!color2region.containsKey(c))
						color2region.put(c, new ArrayList<>());
					color2region.get(c).add(new Vector2i(x, y));
				}
			}
		}
	}

	private static int neighbourCountDifferentToColor(int color, Vector2i pix, int[][] img, int w, int h) {
		int found = 0;
		for (int x = pix.x - 1; x <= pix.x + 1; x++) {
			for (int y = pix.y - 1; y <= pix.y + 1; y++) {
				if (x < 0 || y < 0 || x >= w || y >= h) {
					found++; // treat image border as border of region
				} else if (img[x][y] != color)
					found++;
			}
		}
		return found;
	}
}
