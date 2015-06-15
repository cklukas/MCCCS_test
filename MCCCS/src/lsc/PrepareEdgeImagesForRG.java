package lsc;

import ij.ImagePlus;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import org.graffiti.plugin.io.resources.FileSystemHandler;

import workflow.Settings;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.Image;

/**
 * @author pape, klukas
 */
public class PrepareEdgeImagesForRG {
	
	public static void main(String[] args) throws IOException, Exception {
		if (args == null || args.length != 3) {
			System.err
					.println("No [label file] [edge file] [result file] provided as parameters! Return Code 1");
			System.exit(1);
		} else {
			ImageOperation.BACKGROUND_COLORint = Color.BLACK.getRGB();
			ImageOperation.BACKGROUND_COLOR = new Color(
					ImageOperation.BACKGROUND_COLORint);
			
			Image label = new Image(FileSystemHandler.getURL(new File(args[0])));
			Image edge = new Image(FileSystemHandler.getURL(new File(args[1])));
			
			// label.show("label");
			// edge.show("edge");
			
			ImagePlus ip_label = label.getAsImagePlus();
			ImagePlus ip_edge = edge.getAsImagePlus();
			
			Image extracted_edges = new Image(ip_edge).copy();
			ImageOperation.BACKGROUND_COLORint = -1;
			Image skel = extracted_edges.io().skeletonize().getImage();
			label = label.io().bm().invert().getImage();
			label.io().stat().printColorCodes(true);
			Image skel_inv = skel.io().invert().getImage().show("skel", false);
			Image app = skel_inv.io().applyMaskInversed_ResizeMaskIfNeeded(label, Settings.foreground).invert().getImage();
			app.saveToFile(args[2]);
		}
	}
}
