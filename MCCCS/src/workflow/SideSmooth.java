package workflow;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugin.io.resources.FileSystemHandler;

import workflow.ext.PolynomialFitter;
import workflow.ext.PolynomialFitter.Polynomial;
import de.ipk.ag_ba.gui.picture_gui.BackgroundThreadDispatcher;
import de.ipk.ag_ba.gui.picture_gui.LocalComputeJob;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operation.canvas.ImageCanvas;
import de.ipk.ag_ba.image.structures.Image;

/**
 * Smoothes the left and right-hand side borders of a single object within the image.
 * Uses a polynom to fit a curve and reconstructs the image object with the smoothed-out
 * side borders.
 * 
 * @author Christian Klukas
 */
public class SideSmooth {
	
	public static void main(String[] args) throws IOException, Exception {
		{
			new Settings(true);
		}
		if (args == null || args.length == 0) {
			System.err.println("No filenames provided as parameters! Return Code 1");
			System.exit(1);
		} else {
			for (String a : args) {
				LinkedList<File> fl = new LinkedList<>();
				String path = new File(a).getParent();
				for (File f : new File(path).listFiles((fn) -> {
					if (fn.getName().endsWith("_smooth.png"))
						return false;
					if (fn.getName().endsWith("smooth_all.png"))
						return false;
					if (!fn.getName().endsWith(".png"))
						return false;
					if (fn.getName().endsWith("_cluster.png"))
						return false;
					return fn.getName().startsWith(new File(a).getName());
				})) {
					fl.add(f);
				}
				
				ThreadSafeOptions resAll = new ThreadSafeOptions();
				Image all = null;
				// fl.parallelStream().forEach((f) -> {
				for (File f : fl) {
					if (!f.exists()) {
						System.err.println("File '" + f.getPath() + "' could not be found! Return Code 2");
						System.exit(2);
					} else {
						Image i;
						try {
							i = new Image(FileSystemHandler.getURL(f));
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
						int[][] img = i.getAs2A();
						Rectangle bounds = i.io().getBoundingBox().getBounds();
						PolynomialFitter pf_left = new PolynomialFitter(3);
						PolynomialFitter pf_right = new PolynomialFitter(3);
						for (int y = bounds.y; y < bounds.y + bounds.height; y++) {
							// scanline Left
							scanLeftAndRight(img, bounds, pf_left, pf_right, y);
						}
						Polynomial leftCurve = pf_left.getBestFit();
						Polynomial rightCurve = pf_right.getBestFit();
						ImageOperation resImg = new Image(i.getWidth(), i.getHeight(), new int[i.getWidth() * i.getHeight()]).io();
						resImg = resImg.clearArea(0, 0, i.getWidth(), i.getHeight(), Settings.back);
						ImageCanvas ic = resImg.canvas();
						for (int y = bounds.y; y < bounds.y + bounds.height; y++) {
							int x0 = (int) Math.ceil(leftCurve.getY(y));
							int x1 = (int) rightCurve.getY(y);
							ic = ic.drawLine(x0, y, x1, y, Settings.foreground, 0, 1);
						}
						Image ri = ic.getImage();
						all = i.io().or(ri, Settings.back).getImage();
						// synchronized (resAll) {
						// if (resAll.getParam(0, null) == null)
						// resAll.setParam(0, ri.io());
						// else
						// resAll.setParam(0, ((ImageOperation) resAll.getParam(0, null)).or(ri, 0));
						// }
						ri.saveToFile(f.getParent() + File.separator
								+ f.getName().substring(0, f.getName().lastIndexOf(".")) + "_smooth.png");
					}
				}
				all.saveToFile(a + "smooth_all.png");
				// );
				// synchronized (resAll) {
				// ((ImageOperation) resAll.getParam(0, null)).getImage().saveToFile(a + "smooth_all.png");
				// }
			}
		}
	}
	
	private static void scanLeftAndRight(int[][] img, Rectangle bounds, PolynomialFitter pf_left, PolynomialFitter pf_right, int y) {
		int fromBorder = 20;
		LinkedList<LocalComputeJob> wait = new LinkedList<>();
		try {
			BackgroundThreadDispatcher.addTask(() -> {
				for (int x = bounds.x; x < bounds.x + bounds.width; x++) {
					if (img[x][y] != Settings.back) {
						pf_left.addPoint(y, x);
						if (y < bounds.y + fromBorder) {
							pf_left.addPoint(bounds.y - (y - bounds.y), x);
						}
						if (y > bounds.y + bounds.height - fromBorder) {
							pf_left.addPoint(bounds.y + bounds.height + (bounds.y + bounds.height - y), x);
						}
						break;
					}
				}
			}, "scan left");
			BackgroundThreadDispatcher.addTask(() -> {
				for (int x = bounds.x + bounds.width; x >= bounds.x; x--) {
					if (x < img.length && y < img[0].length)
						if (img[x][y] != Settings.back) {
							pf_right.addPoint(y, x);
							if (y < bounds.y + fromBorder) {
								pf_right.addPoint(bounds.y - (y - bounds.y), x);
							}
							if (y > bounds.y + bounds.height - fromBorder) {
								pf_right.addPoint(bounds.y + bounds.height + (bounds.y + bounds.height - y), x);
							}
							break;
						}
				}
			}, "scan right");
			BackgroundThreadDispatcher.waitFor(wait);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
