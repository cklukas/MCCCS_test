package tools;

import ij.ImagePlus;
import ij.process.ImageConverter;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.TreeMap;

import org.Colors;
import org.GapList;
import org.StringManipulationTools;
import org.SystemOptions;

import workflow.Settings;
import de.ipk.ag_ba.image.operation.ColorSpaceConverter;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.ImageStack;

/**
 * @author pape, klukas
 */
public class ARFFProcessor {
	
	/**
	 * Creates ARFF file for classifier training.
	 * 
	 * @param inputImages
	 * @param background
	 * @param path
	 * @param samplesize
	 * @param filename
	 * @param id
	 * @param calcAdditionalColorFeatures
	 * @throws IOException
	 */
	public static void createTrainingDataSet(ImageStack[] inputImages, float background, File path, int samplesize, String filename, boolean debug, String id,
			boolean addOtherColorValuesFromRGBbands)
			throws IOException {
		
		background = 0.0f;
		
		int numberOfClasses = inputImages.length;
		LinkedList<float[][][]> cubes = new LinkedList<float[][][]>();
		
		for (int idx = 0; idx < numberOfClasses; idx++) {
			cubes.add(inputImages[idx].getFloatCube());
			// System.out.println(idx);
		}
		// inputImages[0].show("11");
		// inputImages[1].show("2");
		
		int width = cubes.get(0).length;
		int height = cubes.get(0)[0].length;
		int bands = cubes.get(0)[0][0].length;
		
		int numberOfChannels = bands;
		
		// inputImages[0].show("000");
		// inputImages[1].show("1");
		
		// create header
		String attributes = "";
		
		for (int i = 0; i < numberOfChannels; i++) {
			attributes += "@attribute " + inputImages[0].getImageLabel(i + 1) + "\tREAL\n";
		}
		
		if (addOtherColorValuesFromRGBbands) {
			attributes += "@attribute X\tNUMERIC\n";
			attributes += "@attribute Y\tNUMERIC\n";
			attributes += "@attribute Z\tNUMERIC\n";
			attributes += "@attribute L\tNUMERIC\n";
			attributes += "@attribute a\tNUMERIC\n";
			attributes += "@attribute b\tNUMERIC\n";
		}
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("%\n"
				+ "@relation " + id + "\n"
				+ attributes
				+ "@attribute class\t{");
		for (int idx = 0; idx < numberOfClasses; idx++) {
			if (idx < numberOfClasses - 1)
				sb.append("class" + idx + ",");
			else
				sb.append("class" + idx);
		}
		sb.append("}\n"
				+ "@data\n");
		
		String line = "";
		
		LinkedList<GapList<short[]>> sampleLists = new LinkedList<GapList<short[]>>();
		
		for (int n = 0; n < numberOfClasses; n++) {
			GapList<short[]> sampleList = new GapList<short[]>();
			float[][][] cube = cubes.get(n);
			
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					int bc = 0;
					for (int b = 0; b < bands; b++) {
						if (cube[x][y][b] == background)
							bc++;
					}
					if (bc < bands) {
						sampleList.add(new short[] { (short) x, (short) y, (short) n });
						// sampleList.add(new Vector3i(x, y, n));
					}
					line = "";
				}
			}
			line = "";
			sampleLists.add(sampleList);
		}
		
		ImageStack debugSampleImagesStack = new ImageStack(numberOfClasses);
		int count = 0;
		ColorSpaceConverter convert = new ColorSpaceConverter(SystemOptions.getInstance().getStringRadioSelection("IAP", "Color Management//White Point",
				ColorSpaceConverter.getWhitePointList(), ColorSpaceConverter.getDefaultWhitePoint(), true));
		double[] xyz = new double[3];
		double[] lab = new double[3];
		
		TreeMap<Integer, String> posToLine = new TreeMap<Integer, String>();
		
		// choose random samples
		for (int idxl = 0; idxl < sampleLists.size(); idxl++) {
			GapList<short[]> sampleList = sampleLists.get(idxl);
			int[][] debugSampleImage = new int[width][height];
			
			while ((count < samplesize || samplesize < 0) && sampleList.size() > 0) {
				int randidx = (int) (Math.random() * sampleList.size());
				String[] st = getSampleString(sampleList.get(randidx), cubes, bands).split(";");
				String[] colors = st[0].split(",");
				
				String[] coords = st[1].split(",");
				int xCoord = Integer.parseInt(StringManipulationTools.getNumbersFromString(coords[0]));
				int yCoord = Integer.parseInt(StringManipulationTools.getNumbersFromString(coords[1]));
				
				int pos = xCoord + yCoord * width;
				
				if (addOtherColorValuesFromRGBbands) {
					double scale = 1d; // in case of 14-bit => 64d else 1d;
					try {
						double r = Double.parseDouble(colors[0]) / scale;
						double g = Double.parseDouble(colors[1]) / scale;
						double b = Double.parseDouble(colors[2]) / scale;
						convert.RGBtoXYZ(r, g, b, xyz);
						convert.XYZtoLAB(xyz, lab);
						
						line = xyz[0] + "," + xyz[1] + "," + xyz[2] + "," + lab[0] + "," + lab[1] + "," + lab[2] + ",";
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				}
				
				int idx = idxl;
				// if (idx == 1)
				// idx = 6;
				
				// if (idx == 5)
				// idx = 1;
				//
				// if (idx == 4)
				// idx = 1;
				//
				// if (idx == 7)
				// idx = 1;
				//
				// if (idx == 2)
				// idx = 0;
				//
				// if (idx == 3)
				// idx = 0;
				
				posToLine.put(pos, st[0] + "," + line + "class" + idx + "\n");
				
				// mark in debug image
				if (debug) {
					// for (int i = 0; i < 4; i++)
					// for (int j = 0; j < 4; j++)
					debugSampleImage[new Integer(xCoord)][new Integer(yCoord)] = Color.RED.getRGB();
				}
				sampleList.remove(randidx);
				count++;
			}
			if (debug)
				debugSampleImagesStack.addImage("Class " + idxl, new Image(debugSampleImage));
			
			count = 0;
		}
		
		if (debug)
			debugSampleImagesStack.show("debug sample images");
		
		for (String l : posToLine.values())
			sb.append(l);
		
		sb.append("%");
		
		IO_MCCCS.write(path.getPath(), filename, sb.toString(), ".arff");
	}
	
	private static String getSampleString(short[] vec_xyn, LinkedList<float[][][]> cubes, int bands) {
		String line = "";
		float[][][] cube = cubes.get(vec_xyn[2]);
		int x = vec_xyn[0];
		int y = vec_xyn[1];
		
		for (int b = 0; b < bands; b++) {
			if (b == bands - 1)
				line += cube[x][y][b];
			else
				line += cube[x][y][b] + ",";
		}
		
		return line + "; x: " + x + ", y: " + y;
	}
	
	/**
	 * Creates ARFF from given image stack.
	 * 
	 * @param isl
	 * @param path
	 * @param name
	 * @param debug
	 * @throws IOException
	 */
	public void convertImagesToArff(ImageStack isl, String path, String name,
			boolean addOtherColorValuesFromRGBbands, boolean debug) throws IOException {
		convertImagesToArff(isl, path, name, null, addOtherColorValuesFromRGBbands, debug);
	}
	
	/**
	 * Creates ARFF from given image stack, extract only pixels which are labeled as FG in mask image.
	 * 
	 * @param isl
	 * @param path
	 * @param name
	 * @param mask
	 * @param debug
	 * @param addOtherColorValuesFromRGBbands
	 * @throws IOException
	 */
	public void convertImagesToArff(ImageStack isl, String path, String name, Image mask_img,
			boolean addOtherColorValuesFromRGBbands, boolean debug) throws IOException {
		
		boolean checkMask = mask_img == null ? false : true;
		
		int[] mask = null;
		int background = 0;
		if (checkMask) {
			mask = mask_img.getAs1A();
			background = Settings.back;
		}
		
		int numberOfChannels = isl.size();
		
		String attributes = "";
		
		for (int i = 0; i < numberOfChannels; i++) {
			attributes += "@attribute " + isl.getImageLabel(i + 1) + "\tREAL\n";
		}
		
		if (addOtherColorValuesFromRGBbands) {
			attributes += "@attribute X\tREAL\n";
			attributes += "@attribute Y\tREAL\n";
			attributes += "@attribute Z\tREAL\n";
			attributes += "@attribute L\tREAL\n";
			attributes += "@attribute a\tREAL\n";
			attributes += "@attribute b\tREAL\n";
		}
		
		int numberOfDiseaseClasses = Settings.numberOfClasses;
		
		attributes += "@attribute class\t{";
		for (int idx = 0; idx < numberOfDiseaseClasses; idx++) {
			if (idx < numberOfDiseaseClasses - 1)
				attributes += ("class" + idx + ",");
			else
				attributes += ("class" + idx);
		}
		attributes += "}\n";
		
		String header = "%\n"
				+ "@relation '" + name + "'\n"
				+ attributes
				+ "@data\n";
		
		float[][][] cubeSliceXY = getFloatCubeSliceXY(isl, true);
		isl = null;
		
		int width = cubeSliceXY[0].length;
		int height = cubeSliceXY[0][0].length;
		int bands = cubeSliceXY.length;
		
		String line = "";
		
		FileWriter fw = new FileWriter(new File(path + "/" + name + ".arff"), false);
		
		fw.write(header);
		
		ColorSpaceConverter convert = new ColorSpaceConverter(SystemOptions.getInstance().getStringRadioSelection("IAP", "Color Management//White Point",
				ColorSpaceConverter.getWhitePointList(), ColorSpaceConverter.getDefaultWhitePoint(), true));
		double[] xyz = new double[3];
		double[] lab = new double[3];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (checkMask)
					if (mask[x + y * width] == background)
						continue;
				
				for (int b = 0; b < bands; b++)
					line += cubeSliceXY[b][x][y] + ",";
				if (addOtherColorValuesFromRGBbands) {
					double d = 1d; // 64d;
					double r = cubeSliceXY[0][x][y] / d;
					double g = cubeSliceXY[1][x][y] / d;
					double b = cubeSliceXY[2][x][y] / d;
					
					convert.RGBtoXYZ(r, g, b, xyz);
					convert.XYZtoLAB(xyz, lab);
					
					line += xyz[0] + "," + xyz[1] + "," + xyz[2] + "," + lab[0] + "," + lab[1] + "," + lab[2];
				}
				
				if (line.length() > 0) {
					// appends the string to the file
					fw.write(line + "?" + "\n");
					// .add(line + "; x: " + x + ", y: " + y);
					line = "";
				}
			}
		}
		
		fw.write("%");
		fw.close();
	}
	
	/**
	 * @return Slice/X/Y
	 */
	public float[][][] getFloatCubeSliceXY(ImageStack is, boolean removeExtractedSlicesToSaveMemory) {
		int bands = is.getStack().getSize();
		float[][][] cube = new float[bands][][];
		
		for (int b = 0; b < bands; b++) {
			float[][] slice = is.getProcessor(
					removeExtractedSlicesToSaveMemory ? 0 : b).getFloatArray();
			if (removeExtractedSlicesToSaveMemory)
				is.getStack().deleteSlice(1);
			cube[b] = slice;
		}
		
		return cube;
	}
	
	/**
	 * 
	 */
	public void convertArffToImage(String parent, String name, Image mask_img, boolean useArffClassInformation, boolean debug) throws IOException {
		
		ImagePlus ip = mask_img.getAsImagePlus();
		ImageConverter ic = new ImageConverter(ip);
		ic.convertToRGB();
		Image iii = new Image(ip);
		
		int[][] mask = iii.getAs2A();
		
		FileReader fr = new FileReader(parent + "/" + name + ".arff");
		BufferedReader br = new BufferedReader(fr);
		
		// skip header
		boolean headpresent = true;
		while (headpresent) {
			String line = br.readLine();
			// System.out.println(line);
			if (line.contains("@data"))
				headpresent = false;
		}
		
		ArrayList<Color> colorsAL = Colors.get(Settings.numberOfClasses, 1);
		int[] colors = new int[colorsAL.size()];
		
		int idx = 0;
		for (Color c : colorsAL)
			colors[idx++] = c.getRGB();
		
		boolean goToStart = false;
		
		out: for (int x = 0; x < mask_img.getWidth(); x++) {
			for (int y = 0; y < mask_img.getHeight(); y++) {
				if (goToStart) {
					x = 0;
					y = 0;
					goToStart = false;
				}
				if (mask[x][y] != Settings.back || useArffClassInformation) {
					String line = br.readLine();
					// System.out.println(line);
					// end of file ?
					if (line == null)
						break out;
					if (line.contains("%"))
						break out;
					if (line.length() == 0) {
						if (useArffClassInformation) {
							// first ARFF file lines empty -> idicies [0,0]
							goToStart = true;
						}
						continue;
					}
					String[] s = line.split(",");
					if (useArffClassInformation) {
						String cl = s[s.length - 1];
						if ("class0".equals(cl))
							mask[x][y] = Settings.foreground;
						else
							mask[x][y] = Settings.back;
						
					} else {
						// last element defines class [0 ... n]
						if (s.length > 0) {
							int cls = Integer.parseInt(StringManipulationTools.getNumbersFromString(s[s.length - 1]));
							mask[x][y] = colors[cls];
						} else
							mask[x][y] = Settings.back;
					}
				}
			}
		}
		
		br.close();
		
		// new Image(mask).show("test");
		if (useArffClassInformation)
			new Image(mask).saveToFile(parent + "/foreground.png");
		else
			new Image(mask).saveToFile(parent + "/classified.png");
	}
}
