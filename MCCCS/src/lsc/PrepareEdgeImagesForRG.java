package lsc;

import ij.ImagePlus;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import org.graffiti.plugin.io.resources.FileSystemHandler;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.Image;

public class PrepareEdgeImagesForRG {
	
	public static void main(String[] args) throws IOException, Exception {
		if (args == null || args.length < 1) {
			System.err
					.println("No [path] provided as parameters! Return Code 1");
			System.exit(1);
		} else {
			ImageOperation.BACKGROUND_COLORint = Color.BLACK.getRGB();
			ImageOperation.BACKGROUND_COLOR = new Color(
					ImageOperation.BACKGROUND_COLORint);
			for (String a : args) {
				LinkedList<File> fl = new LinkedList<>();
				if (a.contains("*")) {
					String path = new File(a).getParent();
					for (File f : new File(path).listFiles((fn) -> {
						String ss = fn.getName().split("_")[1];
						String sss = new File(a).getName().substring(
								new File(a).getName().indexOf("*") + 1);
						return ss.startsWith(sss);
					})) {
						fl.add(f);
					}
				} else {
					fl.add(new File(a));
				}
				
				Image label = null;
				Image edge = null;
				String path = "";
				
				for (File f : fl) {
					path = f.getAbsolutePath();
					if (f.getName().contains("label"))
						label = new Image(FileSystemHandler.getURL(f));
					else
						edge = new Image(FileSystemHandler.getURL(f));
				}
				
				// label.show("label");
				// edge.show("edge");
				
				ImagePlus ip_label = label.getAsImagePlus();
				ImagePlus ip_edge = edge.getAsImagePlus();
				// ImageConverter ic1 = new ImageConverter(input);
				// ic1.convertToGray8();
				// input.getProcessor().threshold(0);
				// Image saveInp = new Image(input).copy();
				// input.getProcessor().dilate();
				// ImageConverter ic2 = new ImageConverter(borders);
				// ic2.convertToGray8();
				// borders.getProcessor().findEdges();
				// borders.getProcessor().threshold(0);
				// // borders.getProcessor().medianFilter();
				// // borders.show();
				// ImageCalculator icalc = new ImageCalculator();
				// ImagePlus result = icalc.run("or", ip_label, ip_edge);
				Image extracted_edges = new Image(ip_label).copy();
				ImageOperation.BACKGROUND_COLORint = -1;
				Image skel = extracted_edges.io().skeletonize().getImage();
				label.io().stat().printColorCodes(true);
				Image skel_inv = skel.io().invert().getImage().show("skel");
				label.show("label");
				Image app = skel_inv.io().applyMaskInversed_ResizeMaskIfNeeded(label).getImage();
				app.saveToFile(path + "_skel.png");
			}
		}
	}
	
}
