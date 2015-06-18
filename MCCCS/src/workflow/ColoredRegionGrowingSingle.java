package workflow;

import java.awt.Color;
import java.io.File;

import org.graffiti.graph.Node;
import org.graffiti.plugin.io.resources.FileSystemHandler;

import tools.Image2Graph;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NodeHelper;

/**
 * Nearest-neighbor approach, to find colorized areas near uncolored foreground
 * area. Assign color of nearest colorized pixel to uncolored pixels. Input:
 * colored image, with some uncolored (white) areas, mask image, background
 * pixels (black) are ignored during processing.
 * 
 * @author Christian Klukas
 */
public class ColoredRegionGrowingSingle {
	
	public static void main(String[] args) throws Exception {
		{
			new Settings(false);
		}
		if (args == null || args.length < 2) {
			System.err.println("No parameter [8 bit rgb, colored input image] [result image] provided as parameters! Return Code 1");
			System.exit(1);
		} else {
			Image img = new Image(FileSystemHandler.getURL(new File(args[0])));
			int w = img.getWidth();
			int h = img.getHeight();
			int uncolored = Color.WHITE.getRGB();
			int[][] ia = img.getAs2A();
			int[][] it = img.getAs2A();
			img.io().stat().printColorCodes(true);
			
			Image i2 = new Image(ia);
			
			Image2Graph i2g = new Image2Graph(i2, Settings.back);
			Node[][] nodeMap = i2g.getNodeMap();
			
			for (int x = 0; x < w; x++) {
				for (int y = 0; y < h; y++) {
					Node n = nodeMap[x][y];
					if (n != null) {
						int c = new NodeHelper(n).getFillColor().getRGB();
						if (c == uncolored) {
							int col = i2g.getColorOfNearestColoredNode(n, uncolored);
							it[x][y] = col;
						}
					}
				}
			}
			
			i2 = new Image(it);
			
			i2.saveToFile(args[1]);
		}
	}
}