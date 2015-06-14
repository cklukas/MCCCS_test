package lsc;

import java.awt.Color;
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
 * Heuristics: Select the most uncompact region. It is split by a straight line
 * so that the division creates two regions, which have together the highest
 * compactness.
 * Important: Each color in the background should be a single connected
 * component, it is assumed that each region is labeled in a different color.
 * 
 * @author Christian Klukas
 */
public class SplitUncompactRegions {
	
	public static void main(String[] args) throws Exception {
		
		boolean debug = true;
		{
			new Settings(false);
		}
		if (args == null || args.length != 3) {
			System.err
					.println("No parameter [8 bit rgb, colored input image] [result image] [target count (may be floating point)] provided as parameters! Return Code 1");
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
							maxComp = c;
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
	
	private static double splitRegionGetCompactnessSum(Integer colorOfRegionWithMinimumCompactness, HashMap<Integer, ArrayList<Vector2i>> color2region,
			HashMap<Integer, ArrayList<Vector2i>> color2regionOutline, Vector2i bestStartP, Vector2i bestEndP, int[][] ia, boolean doIt) {
		// position = sign( (Bx-Ax)*(Y-Ay) - (By-Ay)*(X-Ax) )
		// ==> -1 / 0 on line / +1
		float[] hsv = new float[3];
		Color c = new Color(colorOfRegionWithMinimumCompactness);
		int r = c.getRed();
		int g = c.getGreen();
		int b = c.getBlue();
		Color.RGBtoHSB(r, g, b, hsv);
		Color cA = Color.getHSBColor(hsv[0], hsv[1] * 0.85f, hsv[2]);
		Color cB = Color.getHSBColor(hsv[0], hsv[1], hsv[2] * 0.85f);
		int colA = cA.getRGB();
		int colB = cB.getRGB();
		
		if (color2region.containsKey(colA) || color2region.containsKey(colB)) {
			System.out.println("At least one region split color is already in the image. "
					+ "Another color should be determined, but currently can't be determined (not yet implemented)! " + "Need to abort processing. Error Code 1.");
			System.exit(1);
			
		}
		
		ArrayList<Vector2i> line = drawLine(bestStartP, bestEndP);
		Vector2i A = bestStartP;
		Vector2i B = bestEndP;
		int areaA = 0;
		int areaB = 0;
		for (Vector2i p : color2region.get(colorOfRegionWithMinimumCompactness)) {
			int X = p.x;
			int Y = p.y;
			float pos = Math.signum((B.x - A.x) * (Y - A.y) - (B.y - A.y) * (X - A.x));
			if (pos < 0) {
				if (doIt)
					ia[X][Y] = colA;
				areaA++;
			}
			if (pos > 0) {
				if (doIt)
					ia[X][Y] = colB;
				areaB++;
			}
		}
		int outlineA = 0;
		int outlineB = 0;
		for (Vector2i p : color2regionOutline.get(colorOfRegionWithMinimumCompactness)) {
			int X = p.x;
			int Y = p.y;
			float pos = Math.signum((B.x - A.x) * (Y - A.y) - (B.y - A.y) * (X - A.x));
			if (pos < 0)
				outlineA++;
			if (pos > 0)
				outlineB++;
		}
		outlineA += line.size() - 2;
		outlineB += line.size() - 2;
		areaA = areaA - line.size() + 2;
		areaB = areaB - line.size() + 2;
		
		double compactnessA = areaA <= 0 ? -Double.MAX_VALUE : 4 * Math.PI / (outlineA * outlineA / areaA);
		double compactnessB = areaB <= 0 ? -Double.MAX_VALUE : 4 * Math.PI / (outlineB * outlineB / areaB);
		
		return -line.size();
	}
	
	// position = sign( (Bx-Ax)*(Y-Ay) - (By-Ay)*(X-Ax) )
	// ==> -1 / 0 on line / +1
	public static ArrayList<Vector2i> drawLine(Vector2i a, Vector2i b) {
		int x0 = a.x;
		int y0 = a.y;
		int x1 = b.x;
		int y1 = b.y;
		int dx = Math.abs(x1 - x0), sx = x0 < x1 ? 1 : -1;
		int dy = -Math.abs(y1 - y0), sy = y0 < y1 ? 1 : -1;
		int err = dx + dy, e2; /* error value e_xy */
		ArrayList<Vector2i> line = new ArrayList<>();
		while (true) { /* loop */
			Vector2i point = new Vector2i(x0, y0);
			line.add(point);
			if (x0 == x1 && y0 == y1)
				break;
			e2 = 2 * err;
			if (e2 >= dy) {
				err += dy;
				x0 += sx;
			} /* e_xy+e_x > 0 */
			if (e2 <= dx) {
				err += dx;
				y0 += sy;
			} /* e_xy+e_y < 0 */
		}
		return line;
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
				} else
					if (img[x][y] != color)
						found++;
			}
		}
		return found;
	}
}
