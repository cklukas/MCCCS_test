package lsc;

import ij.ImagePlus;
import ij.plugin.ImageCalculator;
import ij.process.ImageConverter;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import org.graffiti.plugin.io.resources.FileSystemHandler;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.Image;

/**
 * * input e.g: path/*label.png Calculates borders for leaf overlaps for all given
 * input labels, steps are: 1. convert to gray-scale 2. input -> threshold +
 * dilate (edges are found outside, mask will be increased of 1 pixel, dilate
 * add one pixel outside) 2. borders -> find edges 3. image calculator: OR
 * output: id_label_leaf_overlap.[png, tif ...]
 * 
 * @author Jean-Michel Pape
 */
public class DetectLeafBordersFromGT {
	
	public static void main(String[] args) throws IOException, Exception {
		if (args == null || args.length < 1) {
			System.err
					.println("No [filenames] provided as parameters! Return Code 1");
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
				
				for (File f : fl) {
					ImagePlus input = new Image(FileSystemHandler.getURL(f))
							.getAsImagePlus();
					ImagePlus borders = input.duplicate();
					ImageConverter ic1 = new ImageConverter(input);
					ic1.convertToGray8();
					input.getProcessor().threshold(0);
					Image saveInp = new Image(input).copy();
					input.getProcessor().dilate();
					ImageConverter ic2 = new ImageConverter(borders);
					ic2.convertToGray8();
					borders.getProcessor().findEdges();
					borders.getProcessor().threshold(0);
					// borders.show();
					ImageCalculator icalc = new ImageCalculator();
					icalc.run("and", input, borders);
					// input.show();
					
					// CK: does not work if path contains a "." in the name...
					// new Image(input).saveToFile(f.getPath().split("\\.")[0] +
					// "_leaf_overlap.png");
					// CK: split after last "." instead
					String name = f.getPath().substring(0,
							f.getPath().lastIndexOf("."));
					// border image class 1
					Image borderimg = new Image(input).io().invertIgnoresBackground().getImage();
					borderimg.saveToFile(name + "_leaf_overlap.png");
					// non-borders class 2
					saveInp = saveInp.io().invertIgnoresBackground().getImage();
					ImageCalculator icalc2 = new ImageCalculator();
					icalc2.run("xor", saveInp.getAsImagePlus(), borderimg.getAsImagePlus());
					saveInp.getAsImagePlus().getProcessor().dilate();
					saveInp = saveInp.io().invertIgnoresBackground().getImage();
					saveInp.io().invertIgnoresBackground().getImage().saveToFile(name + "_non_leaf_overlap.png");
					// System.out.println("save: " + f.getName());
				}
			}
		}
	}
}
