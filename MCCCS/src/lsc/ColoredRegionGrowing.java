package lsc;

import java.awt.Color;
import java.io.File;

import org.graffiti.plugin.io.resources.FileSystemHandler;

import workflow.Settings;
import de.ipk.ag_ba.image.structures.Image;

/**
 * Nearest-neighbor approach, to find colorized areas near uncolored foreground area.
 * Assign color of nearest colorized pixel to uncolored pixels.
 * Input: colored image, with some uncolored (white) areas, mask image, background pixels (black) are
 * ignored during processing.
 * 
 * @author Christian Klukas
 */
public class ColoredRegionGrowing {
	
	public static void main(String[] args) throws Exception {
		{
			new Settings(false);
		}
		if (args == null || args.length != 2) {
			System.err.println("No parameter [8 bit rgb, colored input image] [result image] provided as parameters! Return Code 1");
			System.exit(1);
		} else {
			Image img = new Image(FileSystemHandler.getURL(new File(args[0])));
			// Image roi = new Image(FileSystemHandler.getURL(new File(args[1])));
			// img = img.io().applyMask(roi).getImage();
			int w = img.getWidth();
			int h = img.getHeight();
			int uncolored = Color.WHITE.getRGB();
			int[][] ia = img.getAs2A();
			int[][] it = img.getAs2A();
			img.io().stat().printColorCodes(true);
			for (int x = 0; x < w; x++) {
				for (int y = 0; y < h; y++) {
					int c = ia[x][y];
					if (c != Settings.back) {
						if (c == uncolored) {
							// find colored neighbor pixel
							double minDistOfColoredPixel = Double.MAX_VALUE;
							int colorOfNearestColoredPixel = uncolored;
							for (int searchRadius = 1; searchRadius < Math.max(w, h); searchRadius++) {
								for (int xsearch = x - searchRadius; xsearch <= x + searchRadius; xsearch++) {
									for (int ysearch = y - searchRadius; ysearch <= y + searchRadius; ysearch++) {
										// if (xsearch > x - searchRadius && xsearch < x + searchRadius
										// && ysearch > y - searchRadius && ysearch < y + searchRadius)
										// continue; // spare inner parts which have already been scanned
										if (xsearch >= 0 && ysearch >= 0 && xsearch < w && ysearch < h) {
											int cs = ia[xsearch][ysearch];
											if (cs != Settings.back && cs != uncolored) {
												double distOfColoredPixel = Math.sqrt((xsearch - x) * (xsearch - x) + (ysearch - y) * (ysearch - y));
												if (distOfColoredPixel < minDistOfColoredPixel) {
													colorOfNearestColoredPixel = cs;
												}
											}
										}
									}
								}
								if (colorOfNearestColoredPixel != uncolored)
									break;
							}
							it[x][y] = colorOfNearestColoredPixel;
						}
					}
				}
			}
			new Image(it).saveToFile(args[1]);
		}
	}
}
