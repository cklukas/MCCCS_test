package lsc;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.help.UnsupportedOperationException;

import org.Vector2i;
import org.color.ColorUtil;
import org.graffiti.plugin.io.resources.FileSystemHandler;

import workflow.Settings;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.TableData;

/**
 * Merge colored regions, which touch each other. Do that until provided target
 * count of regions is reached. If a floating point number is provided, no merge
 * is performed, if a color count just below or just above the number is
 * detected. E.g. target count 4.2 => no merge of regions in case of 4 or 5
 * observed colors. (also no split for 0..3, at the moment, as splitting is not
 * implemented, yet (?)) Heuristics: merge regions, which improve the
 * compactness of the two merged leaves most, or at least makes the smallest
 * impact on their compactness. Important: Each color in the background should
 * be a single connected component, it is assumed that each region is labeled in
 * a different color.
 * 
 * @author Christian Klukas
 */
public class MergeTouchingRegions {

	public static void main(String[] args) throws Exception {

		int alternative_strategy_0to4 = 4;

		boolean debug = false;
		{
			new Settings(false);
		}
		if (args == null || args.length != 4) {
			System.err
					.println("No parameter [8 bit rgb, colored input image] [result image] [target count (may be floating point), or csv file with column a: image file name, column b: target count] [strategy 0..3] provided as parameters! Return Code 1");
			System.exit(1);
		} else {
			alternative_strategy_0to4 = Integer.parseInt(args[3]);

			File inpf = new File(args[0]);
			Image img = new Image(FileSystemHandler.getURL(inpf));
			double targetCount = Double.NaN;
			File f = new File(args[2]);
			if (!f.exists()) {
				targetCount = Double.parseDouble(args[2]);
			} else {
				TableData td = TableData.getTableData(f);
				for (int row = 0; row < td.getMaximumRow(); row++) {
					String fileName = (String) td.getCellData(0, row, null);
					if (fileName != null && new File(fileName).getName().equalsIgnoreCase(inpf.getName())) {
						targetCount = Double.parseDouble(td.getCellDataDate(1, row, null));
					}
				}
			}
			if (Double.isNaN(targetCount)) {
				System.out.println("Error: could not find file '" + inpf.getName()
						+ "' in column 1 of the table data. That would be used for looking-up the target segment count in column 2 of the corresponding row in input file '"
						+ args[2] + "'. Return 1");
				System.exit(1);
			}
			int w = img.getWidth();
			int h = img.getHeight();
			int[][] ia = img.getAs2A();

			// enumerate region pixels
			HashMap<Integer, ArrayList<Vector2i>> color2region = new HashMap<Integer, ArrayList<Vector2i>>();
			findRegions(w, h, ia, color2region);
			int nnn = 0;
			while (color2region.size() > Math.ceil(targetCount)) {
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

					double c = (borderPixels * borderPixels / filledArea); // 4
					// *
					// Math.PI
					// /
					color2compactness.put(color, c);
				}

				// find touching regions
				ArrayList<Vector2i> touchingColorPairs = new ArrayList<Vector2i>();
				for (int color1 : color2region.keySet()) {
					ArrayList<Vector2i> borderRegion1 = color2regionOutline.get(color1);
					for (int color2 : color2region.keySet()) {
						if (color1 == color2)
							continue;
						if (touching(borderRegion1, color2, ia, w, h)) {
							touchingColorPairs.add(new Vector2i(color1, color2));
						}
					}
				}

				// select merge regions, greedy, select the pair which improves
				// it
				// compactness by merging
				double bestImprovement = -Double.MAX_VALUE;
				Vector2i bestPair = null;
				for (Vector2i touchingPair : touchingColorPairs) {
					int color1 = touchingPair.x;
					int color2 = touchingPair.y;

					double c1 = color2compactness.get(color1);
					double c2 = color2compactness.get(color2);

					int borderPixels = color2regionOutline.get(color1).size() + color2regionOutline.get(color2).size() - 2
							* countTouchingPixels(color2regionOutline.get(color1), color2, ia, w, h);
					int filledArea = color2region.get(color1).size() + color2region.get(color2).size();

					double mergedCompactness = (borderPixels * borderPixels / filledArea); // 4
					// *
					// Math.PI
					// /
					// ...

					double improvement;
					switch (alternative_strategy_0to4) {
					case 0:
						improvement = mergedCompactness;
						break;
					case 1:
						improvement = mergedCompactness - c1 - c2;
						break;
					case 2:
						improvement = -c1 - c2;
						break;
					case 3:
						improvement = Math.max(c1, c2) - Math.min(c1, c2);
						break;
					case 4:
						int a1 = color2region.get(color1).size();
						int a2 = color2region.get(color2).size();
						improvement = Math.max(a1, a2) - Math.min(a1, a2);
					default:
						throw new UnsupportedOperationException("Not supported strategy");
					}

					if (debug)
						System.out.println("MERGING " + color1 + " (c=" + c1 + ") with " + color2 + " (c=" + c2 + ") creates a form with compactness of " + mergedCompactness);
					if (improvement > bestImprovement) {
						bestImprovement = improvement;
						bestPair = touchingPair;
					}
				}

				// recolor region 1 and region 2 of merge regions
				if (bestPair == null) {
					System.out.println("Found no touching regions. Can't continue processing.");
					System.exit(0);
				}

				int colorM1 = bestPair.x;
				int colorM2 = bestPair.y;
				int mergedRegionColor = ColorUtil.getAvgColor(colorM1, colorM2);
				if (color2region.containsValue(mergedRegionColor)) {
					System.out.println("Merged region color is already in the image. "
							+ "Another color should be determined, but currently can't be determined (not yet implemented)! " + "Need to abort processing. Error Code 1.");
					System.exit(1);
				}
				for (int x = 0; x < w; x++) {
					for (int y = 0; y < h; y++) {
						int c = ia[x][y];
						if (c == colorM1 || c == colorM2)
							ia[x][y] = mergedRegionColor;
					}
				}
				img = new Image(ia);
				findRegions(w, h, ia, color2region);
			}

			new Image(ia).saveToFile(args[1]);
		}
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

	private static int countTouchingPixels(ArrayList<Vector2i> borderRegion1, int color2, int[][] img, int w, int h) {
		int found = 0;
		for (Vector2i pix : borderRegion1) {
			if (neighbourCountEqualToColor(color2, pix, img, w, h) > 0) {
				found++;
			}
		}
		return found;
	}

	/**
	 * Scan pixels of outline of region 1 and look for colors of region 2.
	 */
	private static boolean touching(ArrayList<Vector2i> borderRegion1, int color2, int[][] img, int w, int h) {
		for (Vector2i pix : borderRegion1) {
			if (neighbourCountEqualToColor(color2, pix, img, w, h) > 0) {
				return true;
			}
		}
		return false;
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

	private static int neighbourCountEqualToColor(int color, Vector2i pix, int[][] img, int w, int h) {
		int found = 0;
		for (int x = pix.x - 1; x <= pix.x + 1; x++) {
			for (int y = pix.y - 1; y <= pix.y + 1; y++) {
				if (img[x][y] == color)
					found++;
			}
		}
		return found;
	}
}
