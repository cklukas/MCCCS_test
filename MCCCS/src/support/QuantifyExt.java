package support;

import java.awt.Color;
import java.io.File;
import java.util.LinkedList;
import java.util.TreeMap;

import org.AttributeHelper;
import org.Vector2i;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.graffiti.plugin.io.resources.FileSystemHandler;

import workflow.Settings;
import de.ipk.ag_ba.gui.picture_gui.BackgroundThreadDispatcher;
import de.ipk.ag_ba.gui.picture_gui.LocalComputeJob;
import de.ipk.ag_ba.image.operations.segmentation.ClusterDetection;
import de.ipk.ag_ba.image.operations.segmentation.Segmentation;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;

/**
 * Reads and image and quantifies (counts) the foreground pixels, marked with different colors.
 * For each color a the corresponding infection rate is calculated.
 * Extended version of Quantify, in addition the number of segments using a particular color, the
 * average size and other statistics is calculated for the colored regions.
 * 
 * @author Christian Klukas
 */
public class QuantifyExt {
	
	public static void main(String[] args) throws Exception {
		{
			new Settings(false);
		}
		if (args == null || args.length == 0) {
			System.err.println("No filenames provided as parameters! Return Code 1");
			System.exit(1);
		} else {
			LinkedList<LocalComputeJob> wait = new LinkedList<>();
			
			for (String a : args) {
				LinkedList<File> fl = new LinkedList<>();
				String path = new File(a).getParent();
				for (File f : new File(path).listFiles((fn) -> {
					if (!fn.getName().endsWith("_cluster.png") && !fn.getName().startsWith("classified_") && !fn.getName().startsWith("cluster"))
						return false;
					return fn.getName().startsWith(new File(a).getName());
				})) {
					fl.add(f);
				}
				fl.forEach((f) -> {
					if (!f.exists()) {
						System.err.println("File '" + a + "' could not be found! Return Code 2");
						System.exit(2);
					} else {
						try {
							wait.add(BackgroundThreadDispatcher.addTask(
									() -> {
										Image i;
										try {
											i = new Image(FileSystemHandler.getURL(f));
										} catch (Exception e) {
											throw new RuntimeException(e);
										}
										int[] img = i.getAs1A();
										TreeMap<Integer, DetailSegmentInfo> diseaseSymptomId2Area = new TreeMap<>();
										int fgArea = 0;
										for (int p : img) {
											if (p != Settings.back) {
												fgArea++;
												if (!diseaseSymptomId2Area.containsKey(p))
													diseaseSymptomId2Area.put(p, new DetailSegmentInfo());
												diseaseSymptomId2Area.get(p).area++;
											}
										}
										int w = i.getWidth();
										int h = i.getHeight();
										for (int color : i.io().stat().getColors()) {
											Segmentation ps = new ClusterDetection(i.io().binary(color, Settings.back, Settings.foreground).getImage(), Settings.back);
											ps.detectClusters();
											diseaseSymptomId2Area.get(color).clusterCount = ps.getClusterCount();
											{
												DescriptiveStatistics statSize = new DescriptiveStatistics();
												for (int sizeC : ps.getClusterSize()) {
													statSize.addValue(sizeC);
												}
												diseaseSymptomId2Area.get(color).clusterSizeMean = statSize.getMean();
												diseaseSymptomId2Area.get(color).clusterSizeStdDev = statSize.getStandardDeviation();
												diseaseSymptomId2Area.get(color).clusterSizeSkewness = statSize.getSkewness();
												diseaseSymptomId2Area.get(color).clusterSizeKurtosis = statSize.getKurtosis();
												diseaseSymptomId2Area.get(color).clusterSizePercentile10 = statSize.getPercentile(10);
												diseaseSymptomId2Area.get(color).clusterSizePercentile90 = statSize.getPercentile(90);
											}
											{
												DescriptiveStatistics statX = new DescriptiveStatistics();
												DescriptiveStatistics statY = new DescriptiveStatistics();
												DescriptiveStatistics statD = new DescriptiveStatistics();
												for (Vector2i pos : ps.getClusterCenterPoints()) {
													statX.addValue(pos.x);
													statY.addValue(pos.y);
													statD.addValue(Math.sqrt(Math.pow(pos.x - w / 2, 2) + Math.pow(pos.y - h / 2, 2)));
												}
												
												diseaseSymptomId2Area.get(color).clusterXMean = statX.getMean();
												diseaseSymptomId2Area.get(color).clusterXStdDev = statX.getStandardDeviation();
												diseaseSymptomId2Area.get(color).clusterXSkewness = statX.getSkewness();
												diseaseSymptomId2Area.get(color).clusterXKurtosis = statX.getKurtosis();
												diseaseSymptomId2Area.get(color).clusterXPercentile10 = statX.getPercentile(10);
												diseaseSymptomId2Area.get(color).clusterXPercentile90 = statX.getPercentile(90);
												
												diseaseSymptomId2Area.get(color).clusterYMean = statY.getMean();
												diseaseSymptomId2Area.get(color).clusterYStdDev = statY.getStandardDeviation();
												diseaseSymptomId2Area.get(color).clusterYSkewness = statY.getSkewness();
												diseaseSymptomId2Area.get(color).clusterYKurtosis = statY.getKurtosis();
												diseaseSymptomId2Area.get(color).clusterYPercentile10 = statY.getPercentile(10);
												diseaseSymptomId2Area.get(color).clusterYPercentile90 = statY.getPercentile(90);
												
												diseaseSymptomId2Area.get(color).clusterDMean = statD.getMean();
												diseaseSymptomId2Area.get(color).clusterDStdDev = statD.getStandardDeviation();
												diseaseSymptomId2Area.get(color).clusterDSkewness = statD.getSkewness();
												diseaseSymptomId2Area.get(color).clusterDKurtosis = statD.getKurtosis();
												diseaseSymptomId2Area.get(color).clusterDPercentile10 = statD.getPercentile(10);
												diseaseSymptomId2Area.get(color).clusterDPercentile90 = statD.getPercentile(90);
												
											}
										}
										
										TextFile tf = new TextFile();
										tf.add(f.getParent() + File.separator + f.getName() + "\t" + "foreground_area_pixel" + "\t" + fgArea);
										String fn = f.getParent() + File.separator + f.getName();
										for (Integer symp : diseaseSymptomId2Area.keySet()) {
											String colorName = AttributeHelper.getColorName(new Color(symp));
											for (String line : diseaseSymptomId2Area.get(symp).getLines(fn + "\t" + "class_" + colorName))
												tf.add(line);
										}
										try {
											tf.write(f.getParent() + File.separator + f.getName() + "_quantified.csv");
										} catch (Exception e) {
											throw new RuntimeException(e);
										}
									}, "process " + f.getName()));
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					}
				});
			}
			BackgroundThreadDispatcher.waitFor(wait);
		}
	}
}
