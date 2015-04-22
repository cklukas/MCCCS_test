package tools;

import ij.ImagePlus;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import workflow.Settings;
import de.ipk.ag_ba.gui.picture_gui.LocalComputeJob;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.ImageStack;

public class IO_MacroBot {
	
	File pathToTestingData;
	
	/**
	 * Path to folder, should include /input, /masks and /groundtruth.
	 */
	public IO_MacroBot(File f) {
		this.pathToTestingData = f;
	}
	
	public IO_MacroBot(String pathToTestingData) {
		this.pathToTestingData = new File(pathToTestingData);
	}
	
	public ImageStack[] readTrainingData(boolean onlyGT, boolean ignoreGT) throws InterruptedException {
		ImageStack[] isl;
		
		if (!onlyGT) {
			isl = new ImageStack[ignoreGT ? 2 : 3];
			isl[0] = readImages(pathToTestingData, ReadMode.IMAGES, -1, ".tif", false);
			isl[1] = readImages(pathToTestingData, ReadMode.MASKS, 3, ".png", false);
			if (!ignoreGT)
				isl[2] = readImages(pathToTestingData, ReadMode.GROUNDTRUTH, Settings.numberOfClasses, ".png", false);
		} else {
			isl = new ImageStack[1];
			isl[0] = readImages(pathToTestingData, ReadMode.GROUNDTRUTH_USER, Settings.numberOfClasses, ".png", true);
		}
		
		return isl;
	}
	
	/**
	 * Ignores Roi.
	 * 
	 * @param num
	 * @throws InterruptedException
	 */
	private ImageStack readImages(File pathToTestingData, ReadMode r,
			int numberOfFiles, String extension, boolean skipFirst) throws InterruptedException {
		ImageStack is = new ImageStack();
		String filename = r.getMode();
		
		int idx = 0;
		if (skipFirst)
			idx = 1;
		
		String[] fl = pathToTestingData.list();
		TreeMap<String, String> hmap = new TreeMap<String, String>();
		for (String s : fl) {
			if (!s.endsWith(extension))
				continue;
			if (!s.startsWith(filename))
				continue;
			
			// String[] sa = s.split("_");
			// if (sa.length > 2) {
			// hmap.put(sa[0] + "_" + sa[1], s);
			// } else {
			hmap.put(s.substring(0, s.lastIndexOf(".")), s);
			// }
		}
		
		LinkedList<LocalComputeJob> wait = new LinkedList<>();
		LinkedHashMap<Integer, ImagePlus> res = new LinkedHashMap<>();
		ArrayList<String> fnames = new ArrayList<String>(hmap.keySet());
		for (; idx < (numberOfFiles < 0 ? hmap.size() : numberOfFiles); idx++) {
			final int idxxx = idx;
			String idxxxs = idxxx + "";
			// while (idxxxs.length() < 3)
			// idxxxs = "0" + idxxxs;
			try {
				ImagePlus ip = null;
				String pathOrURL = pathToTestingData.getAbsolutePath() + File.separator + hmap.get(numberOfFiles < 0 ? fnames.get(idxxx) : filename + idxxxs);
				// load ROI mask from parent folder
				if (r == ReadMode.MASKS && idxxx == 0) {
					continue;
				}
				if (Settings.print_IO)
					System.out.println(pathOrURL);
				ip = new ImagePlus(pathOrURL);
				
				if (r == ReadMode.MASKS)
					ip = new Image(ip).io().threshold(127, 0, Color.WHITE.getRGB()).getImageAsImagePlus();
				
				synchronized (res) {
					res.put(idxxx, ip);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		for (int idxx : res.keySet()) {
			is.addImage(numberOfFiles < 0 ? fnames.get(idxx) : filename + idxx, res.get(idxx));
		}
		
		return is;
	}
	
	public static Image readImageAbsPath(String inp) throws IOException {
		File fff = new File(inp);
		if (!fff.exists())
			System.err.println("File does not exist: " + fff.getPath());
		Image img = new Image(ImageIO.read(fff));
		return img;
	}
	
	public static synchronized void saveImage(String outputPath, String name, String format, Image img) throws IOException {
		saveImage(outputPath, name, format, img.getAsBufferedImage(false));
	}
	
	public static synchronized void saveImage(String outputPath, String name, String format, ImagePlus img) throws IOException {
		saveImage(outputPath, name, format, img.getBufferedImage());
	}
	
	public static synchronized void saveImage(String outputPath, String name, String format, BufferedImage img) throws IOException {
		File path = new File(outputPath);
		boolean pathOK = true;
		if (!path.exists())
			pathOK = path.mkdirs();
		if (pathOK == false) {
			System.err.println("Path incorrect, no image has been written!");
			return;
		}
		if (!outputPath.endsWith("/"))
			outputPath = outputPath + "/";
		File outputfile = new File(outputPath + name + "." + format);
		ImageIO.write(img, format, outputfile);
	}
	
	public static void write(String pathname, String filename, double[] data) throws IOException {
		String str = Arrays.toString(data);
		write(pathname, filename, str);
	}
	
	public static void write(String pathname, String filename, String data) throws IOException {
		write(pathname, filename, data, ".txt");
	}
	
	public static void write(String pathname, String filename, String data, String format) throws IOException {
		File path = new File(pathname);
		if (!path.exists())
			path.mkdirs();
		
		PrintWriter out = new PrintWriter(new BufferedWriter(
				new FileWriter(pathname + File.separator + filename + format)));
		out.write(data);
		out.close();
	}
	
	private enum ReadMode {
		IMAGES, MASKS, GROUNDTRUTH, GROUNDTRUTH_USER;
		
		String getMode() {
			switch (this) {
				case GROUNDTRUTH_USER:
					return "dt";
				case GROUNDTRUTH:
					return "label_";
				case IMAGES:
					return "channel_";
				case MASKS:
					return "mask_";
				default:
					new Exception("Not supported mode.");
			}
			return null;
		}
	}
	
	public ImageStack[] readTestingData() throws InterruptedException {
		ImageStack[] isl = new ImageStack[1];
		isl[0] = readImages(pathToTestingData, ReadMode.IMAGES, -1, ".tif", false);
		return isl;
	}
}
