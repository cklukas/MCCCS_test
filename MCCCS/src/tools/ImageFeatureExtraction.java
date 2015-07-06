package tools;

import ij.ImagePlus;
import ij.plugin.filter.RankFilters;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.IntStream;

import org.ErrorMsg;

import workflow.Settings;
import de.ipk.ag_ba.gui.picture_gui.StreamBackgroundTaskHelper;
import de.ipk.ag_ba.image.operation.ArrayUtil;
import de.ipk.ag_ba.image.operation.FirstOrderTextureFeatures;
import de.ipk.ag_ba.image.operation.GLCMTextureFeatures;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operation.ImageTexture;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.ImageStack;
import de.lmu.ifi.dbs.jfeaturelib.features.Haralick;

public class ImageFeatureExtraction {
	
	public TreeMap<String, Image> processImage(Image img, int masksize, double parm_sigma, FeatureMode mode) {
		TreeMap<String, Image> res = new TreeMap<String, Image>();
		boolean br = true;
		
		switch (mode) {
			case ALL:
				br = false;
			case SHARPEN:
				ImagePlus imgp = img.getAsImagePlus();
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
				rf.rank(img.getAsImagePlus().getProcessor(), masksize, RankFilters.MEDIAN);
				res.put(FeatureMode.MEDIAN.name(), img);
				if (br)
					break;
			case TEXTURE:
				ImageStack is = calcTextureForVizualization(img.io(), masksize);
				int idx = 1;
				for (ImageProcessor i : is) {
					// use Median filter to suppress noise (may caused by discontinuities)
					RankFilters rf2 = new RankFilters();
					rf2.rank(i, 2, RankFilters.MEDIAN);
					Image filteredImage = new Image(i).io().getImage();
					res.put(FeatureMode.TEXTURE.name() + "_" + is.getImageLabel(idx++), filteredImage);
				}
				if (br)
					break;
			case HARLICK:
				ImageStack iss = runHarlick(img, masksize);
				int idxx = 1;
				for (ImageProcessor i : iss) {
					// use Median filter to suppress noise (may caused by discontinuities)
					RankFilters rf2 = new RankFilters();
					rf2.rank(i, 2, RankFilters.MEDIAN);
					Image filteredImage = new Image(i).io().getImage();
					res.put(FeatureMode.HARLICK.name() + "_" + iss.getImageLabel(idxx++), filteredImage);
				}
				break;
			default:
				break;
		}
		return res;
	}
	
	private ImageStack runHarlick(Image img, int masksize) {
		int w = img.getWidth();
		int h = img.getHeight();
		masksize = masksize * 2 + 1;
		int halfmask = masksize / 2;
		int[][] img2d = img.io().getAs2D();
		int[] temp = new int[masksize * masksize];
		final int f_masksize = masksize;
		
		String[] harlickNames = { "Angular 2nd moment",
				"Contrast",
				"Correlation",
				"variance",
				"Inverse Difference Moment",
				"Sum Average",
				"Sum Variance",
				"Sum Entropy",
				"Entropy",
				"Difference Variance",
				"Difference Entropy",
				"Information Measures of Correlation",
				"Information Measures of Correlation",
				"Maximum Correlation",
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
						ImageTexture it = new ImageTexture(temp, f_masksize, f_masksize);
						
						// initialize the descriptor
				Haralick descriptor = new Haralick();
				
				// run the descriptor and extract the features
				descriptor.run(new Image(f_masksize, f_masksize, temp).getAsImagePlus().getProcessor());
				
				// obtain the features
				List<double[]> features = descriptor.getFeatures();
				
				// print the features to system out
				
				for (double[] feature : features) {
					for (int idx = 0; idx < feature.length; idx++)
						results.get(harlickNames[idx])[x][y] = feature[idx];
				}
			}
		}, (t, e) -> {
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
		
		is.show("HARLICK");
		
		return is;
	}
	
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
				}, (t, e) -> {
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
		SHARPEN, BLUR, MEDIAN, TEXTURE, HARLICK, ALL
	}
}
