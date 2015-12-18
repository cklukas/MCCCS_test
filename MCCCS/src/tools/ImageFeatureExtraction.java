package tools;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.IntStream;

import org.ErrorMsg;

import de.ipk.ag_ba.gui.picture_gui.StreamBackgroundTaskHelper;
import de.ipk.ag_ba.image.operation.ArrayUtil;
import de.ipk.ag_ba.image.operation.FirstOrderTextureFeatures;
import de.ipk.ag_ba.image.operation.GLCMTextureFeatures;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operation.ImageTexture;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.ImageStack;
import de.lmu.ifi.dbs.jfeaturelib.edgeDetector.Kirsch;
import de.lmu.ifi.dbs.jfeaturelib.features.Gabor;
import de.lmu.ifi.dbs.jfeaturelib.features.Haralick;
import ij.ImagePlus;
import ij.plugin.filter.RankFilters;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import workflow.Settings;

public class ImageFeatureExtraction {
	
	public TreeMap<String, Image> processImage(Image img, int masksize, double parm_sigma, FeatureMode mode) {
		TreeMap<String, Image> res = new TreeMap<String, Image>();
		boolean br = true;
		
		ImageStack is = null;
		int idx;
		
		switch (mode) {
			
			case ALL:
			br = false;
			case SHARPEN:
			ImagePlus imgp = img.copy().getAsImagePlus();
			imgp.getProcessor().sharpen();
			res.put(FeatureMode.SHARPEN.name(), new Image(imgp));
			if (br)
				break;
				
			case BLUR:
			img.getAsImagePlus().getProcessor().blurGaussian(parm_sigma);
			res.put(FeatureMode.BLUR.name(), img);
			if (br)
				break;
				
			case MEDIAN:
			RankFilters rf = new RankFilters();
			rf.rank(img.getAsImagePlus().getProcessor().convertToByteProcessor(), masksize, RankFilters.MEDIAN);
			res.put(FeatureMode.MEDIAN.name(), img);
			if (br)
				break;
				
			case HARLICK:
			is = run(FeatureMode.HARLICK, img, masksize);
			idx = 1;
			for (ImageProcessor i : is) {
				// use Median filter to suppress noise (may caused by discontinuities)
				RankFilters rf2 = new RankFilters();
				rf2.rank(i, 2, RankFilters.MEDIAN);
				Image filteredImage = new Image(i).io().getImage();
				res.put(FeatureMode.HARLICK.name() + "_" + is.getImageLabel(idx++), filteredImage);
			}
			if (br)
				break;
				
			case KIRSCH:
			Kirsch kirschdetector = new Kirsch();
			ImageProcessor kimg = img.getAsImagePlus().getProcessor().convertToByteProcessor();
			kirschdetector.run(kimg);
			res.put(FeatureMode.KIRSCH.name(), new Image(kimg));
			if (br)
				break;
				
			case GABOR:
			GaborFilter g = new GaborFilter(img.getAsImagePlus());
			g.run();
			new ImagePlus("gabor", g.getIs()).show();
			is.setStack(g.getIs());
			is.show("gabor");
			idx = 1;
			for (ImageProcessor i : is) {
				Image filteredImage = new Image(i).io().getImage();
				res.put(FeatureMode.GABOR.name() + "_" + is.getImageLabel(idx++), filteredImage);
			}
			// is = run(FeatureMode.GABOR, img, masksize);
			// idx = 1;
			// for (ImageProcessor i : is) {
			// Image filteredImage = new Image(i).io().getImage();
			// res.put(FeatureMode.GABOR.name() + "_" + is.getImageLabel(idx++), filteredImage);
			// }
			break;
			
			default:
			break;
		}
		return res;
	}
	
	/**
	 * Method to apply features using JfeatureLib which have to be calculated for each pixel (Convolutuion).
	 * 
	 * @mode: Haralick, Gabor
	 */
	private ImageStack run(FeatureMode mode, Image img, int masksize) {
		int w = img.getWidth();
		int h = img.getHeight();
		masksize = masksize * 2 + 1;
		int halfmask = masksize / 2;
		int[][] img2d = img.io().getAs2D();
		int[] temp = new int[masksize * masksize];
		final int f_masksize = masksize;
		
		LinkedList<String> names = new LinkedList<>();
		
		if (mode.name() == "HARLICK") {
			names.add("Angular_Second_Moment");
			names.add("Contrast");
			names.add("Correlation");
			names.add("Variance");
			names.add("Inverse_Difference_Moment");
			names.add("Sum_Average");
			names.add("Sum_Variance");
			names.add("Sum_Entropy");
			names.add("Entropy");
			names.add("Difference_Variance");
			names.add("Difference_Entropy");
			names.add("Information_Measures_of_Correlation_1");
			names.add("Information_Measures_of_Correlation_2");
			names.add("Maximum_Correlation");
			names.add("Coefficient");
			
		} else
			if (mode.name() == "GABOR") {
			for (int i = 0; i < 60; i++) {
				names.add(i + "");
			}
			}
			
		HashMap<String, double[][]> results = new HashMap<String, double[][]>();
		
		for (String n : names) {
			results.put(n, new double[w][h]);
		}
		
		new StreamBackgroundTaskHelper<Integer>("Texture analysis for visualization").process(IntStream.range(0, w), (x) ->
		
		{
			for (int y = 0; y < h; y++) {
			
			if (img2d[x][y] == Settings.back)
				continue;
				
			for (int i = 0; i < temp.length; i++)
				temp[i] = Settings.back;
				
			int count = 0;
			for (int xMask = -halfmask; xMask < halfmask; xMask++) {
				for (int yMask = -halfmask; yMask < halfmask; yMask++) {
					if (x + xMask >= 0 && x + xMask < w && y + yMask >= 0 && y + yMask < h) {
						if (img2d[x + xMask][y + yMask] != ImageOperation.BACKGROUND_COLORint)
						temp[count] = img2d[x + xMask][y + yMask] & 0x0000ff;
						else
						temp[count] = img2d[x + xMask][y + yMask];
					}
					count++;
				}
			}
			
			List<double[]> features;
			
			switch (mode) {
				case ALL:
					break;
				case BLUR:
					break;
				case GABOR:
					// initialize the descriptor
					Gabor gabor = new Gabor();
					
					// run the descriptor and extract the features
					gabor.run(new Image(f_masksize, f_masksize, temp).getAsImagePlus().getProcessor());
					
					// obtain the features
					features = gabor.getFeatures();
					
					for (double[] feature : features) {
						for (int idx = 0; idx < feature.length; idx++)
						results.get(names.get(idx))[x][y] = feature[idx];
					}
				case HARLICK:
					// initialize the descriptor
					Haralick hara = new Haralick();
					
					// run the descriptor and extract the features
					hara.run(new Image(f_masksize, f_masksize, temp).getAsImagePlus().getProcessor());
					
					// obtain the features
					features = hara.getFeatures();
					
					for (double[] feature : features) {
						for (int idx = 0; idx < feature.length; idx++)
						results.get(names.get(idx))[x][y] = feature[idx];
					}
				case KIRSCH:
					break;
				case MEDIAN:
					break;
				case SHARPEN:
					break;
				default:
					break;
					
			}
			
			}
		} , (t, e) ->
		
		{
			ErrorMsg.addErrorMessage(new RuntimeException(e));
		});
		
		ImageStack is = new ImageStack();
		
		Iterator<Entry<String, double[][]>> iter = results.entrySet().iterator();
		while (iter.hasNext())
		
		{
			Entry<String, double[][]> pairs = iter.next();
			FloatProcessor p = new FloatProcessor(pairs.getValue().length, pairs.getValue()[0].length, ArrayUtil.get1d(pairs.getValue()));
			is.addImage(pairs.getKey().toString(), new Image(p));
			iter.remove(); // avoids a ConcurrentModificationException
		}
		
		// is.show("HARLICK");
		
		return is;
		
	}
	
	/**
	 * @deprecated use run(...) instead
	 * @param img
	 * @param masksize
	 * @return
	 */
	@Deprecated
	private ImageStack runHarlick(Image img, int masksize) {
		int w = img.getWidth();
		int h = img.getHeight();
		masksize = masksize * 2 + 1;
		int halfmask = masksize / 2;
		int[][] img2d = img.io().getAs2D();
		int[] temp = new int[masksize * masksize];
		final int f_masksize = masksize;
		
		String[] harlickNames = { "Angular_Second_Moment",
			"Contrast",
			"Correlation",
			"Variance",
			"Inverse_Difference_Moment",
			"Sum_Average",
			"Sum_Variance",
			"Sum_Entropy",
			"Entropy",
			"Difference_Variance",
			"Difference_Entropy",
			"Information_Measures_of_Correlation_1",
			"Information_Measures_of_Correlation_2",
			"Maximum_Correlation",
			"Coefficient" };
			
		HashMap<String, double[][]> results = new HashMap<String, double[][]>();
		
		for (String n : harlickNames) {
			results.put(n, new double[w][h]);
		}
		
		new StreamBackgroundTaskHelper<Integer>("Texture analysis for visualization").process(
			IntStream.range(0, w), (x) -> {
				for (int y = 0; y < h; y++) {
					
					if (img2d[x][y] == Settings.back)
						continue;
						
					for (int i = 0; i < temp.length; i++)
						temp[i] = Settings.back;
						
					int count = 0;
					for (int xMask = -halfmask; xMask < halfmask; xMask++) {
						for (int yMask = -halfmask; yMask < halfmask; yMask++) {
						if (x + xMask >= 0 && x + xMask < w && y + yMask >= 0 && y + yMask < h) {
							if (img2d[x + xMask][y + yMask] != ImageOperation.BACKGROUND_COLORint)
								temp[count] = img2d[x + xMask][y + yMask] & 0x0000ff;
							else
								temp[count] = img2d[x + xMask][y + yMask];
						}
						count++;
						}
					}
					// initialize the descriptor
					Haralick descriptor = new Haralick();
					
					// run the descriptor and extract the features
					descriptor.run(new Image(f_masksize, f_masksize, temp).getAsImagePlus().getProcessor());
					
					// obtain the features
					List<double[]> features = descriptor.getFeatures();
					
					for (double[] feature : features) {
						for (int idx = 0; idx < feature.length; idx++)
						results.get(harlickNames[idx])[x][y] = feature[idx];
					}
				}
			} , (t, e) -> {
				ErrorMsg.addErrorMessage(new RuntimeException(e));
			});
			
		ImageStack is = new ImageStack();
		
		Iterator<Entry<String, double[][]>> iter = results.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, double[][]> pairs = iter.next();
			FloatProcessor p = new FloatProcessor(pairs.getValue().length, pairs.getValue()[0].length, ArrayUtil.get1d(pairs.getValue()));
			is.addImage(pairs.getKey().toString(), new Image(p));
			iter.remove(); // avoids a ConcurrentModificationException
		}
		
		// is.show("HARLICK");
		
		return is;
	}
	
	/**
	 * @deprecated use Haralick features instead
	 */
	@Deprecated
	private ImageStack calcTextureForVizualization(ImageOperation img, int masksize) {
		int w = img.getWidth();
		int h = img.getHeight();
		
		// FloatProcessor fp = (FloatProcessor) img.getImageAsImagePlus().getProcessor();
		//
		// System.out.println(fp.getHistogramSize() + " min " + fp.getHistogramMin() + " max " + fp.getHistogramMax());
		// double masksize
		masksize = masksize * 2 + 1;
		int halfmask = masksize / 2;
		int[][] img2d = img.getAs2D();
		int[] temp = new int[masksize * masksize];
		final int f_masksize = masksize;
		
		HashMap<FirstOrderTextureFeatures, double[][]> firstArrays = new HashMap<>();
		
		for (FirstOrderTextureFeatures name : FirstOrderTextureFeatures.values()) {
			firstArrays.put(name, new double[w][h]);
		}
		
		HashMap<GLCMTextureFeatures, double[][]> glcmArrays = new HashMap<>();
		
		for (GLCMTextureFeatures f : GLCMTextureFeatures.values()) {
			glcmArrays.put(f, new double[w][h]);
		}
		
		new StreamBackgroundTaskHelper<Integer>("Texture analysis for visualization").process(
			IntStream.range(0, w), (x) -> {
				for (int y = 0; y < h; y++) {
					
					if (img2d[x][y] == Settings.back)
						continue;
						
					for (int i = 0; i < temp.length; i++)
						temp[i] = Settings.back;
						
					int count = 0;
					for (int xMask = -halfmask; xMask < halfmask; xMask++) {
						for (int yMask = -halfmask; yMask < halfmask; yMask++) {
						if (x + xMask >= 0 && x + xMask < w && y + yMask >= 0 && y + yMask < h) {
							if (img2d[x + xMask][y + yMask] != ImageOperation.BACKGROUND_COLORint)
								temp[count] = img2d[x + xMask][y + yMask] & 0x0000ff;
							else
								temp[count] = img2d[x + xMask][y + yMask];
						}
						count++;
						}
					}
					ImageTexture it = new ImageTexture(temp, f_masksize, f_masksize);
					
					it.calcTextureFeatures();
					
					for (FirstOrderTextureFeatures f : FirstOrderTextureFeatures.values()) {
						double[][] arr = firstArrays.get(f);
						arr[x][y] = it.firstOrderFeatures.get(f);
					}
					
					it.calcGLCMTextureFeatures();
					
					for (GLCMTextureFeatures f : GLCMTextureFeatures.values()) {
						double[][] arr = glcmArrays.get(f);
						arr[x][y] = it.glcmFeatures.get(f);
					}
				}
			} , (t, e) -> {
				ErrorMsg.addErrorMessage(new RuntimeException(e));
			});
			
		ImageStack is = new ImageStack();
		// is.addImage("", img.getImage());
		
		{
			Iterator<Entry<FirstOrderTextureFeatures, double[][]>> iter = firstArrays.entrySet().iterator();
			while (iter.hasNext()) {
			Entry<FirstOrderTextureFeatures, double[][]> pairs = iter.next();
			FloatProcessor p = new FloatProcessor(pairs.getValue().length, pairs.getValue()[0].length, ArrayUtil.get1d(pairs.getValue()));
			is.addImage(pairs.getKey().toString(), new Image(p));
			iter.remove(); // avoids a ConcurrentModificationException
			}
		}
		
		{
			Iterator<Entry<GLCMTextureFeatures, double[][]>> iter = glcmArrays.entrySet().iterator();
			while (iter.hasNext()) {
			Entry<GLCMTextureFeatures, double[][]> pairs = iter.next();
			FloatProcessor p = new FloatProcessor(pairs.getValue().length, pairs.getValue()[0].length, ArrayUtil.get1d(pairs.getValue()));
			is.addImage(pairs.getKey().toString(), new Image(p));
			iter.remove(); // avoids a ConcurrentModificationException
			}
		}
		is.show("debug texture stack");
		return is;
	}
	
	public enum FeatureMode {
		SHARPEN, BLUR, MEDIAN, HARLICK, KIRSCH, GABOR, ALL
	}
}
