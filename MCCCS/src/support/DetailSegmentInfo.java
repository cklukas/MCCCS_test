package support;

import java.util.ArrayList;

/**
 * @author klukas
 */
public class DetailSegmentInfo {
	
	public static int area = 0;
	public static int clusterCount = 0;
	
	public static double clusterSizeMean = Double.NaN;
	public static double clusterSizeStdDev;
	public static double clusterSizeSkewness;
	public static double clusterSizeKurtosis;
	public static double clusterSizePercentile10;
	public static double clusterSizePercentile90;
	
	public static double clusterXMean;
	public static double clusterXStdDev;
	public static double clusterXSkewness;
	public static double clusterXKurtosis;
	public static double clusterXPercentile10;
	public static double clusterXPercentile90;
	
	public static double clusterYMean;
	public static double clusterYStdDev;
	public static double clusterYSkewness;
	public static double clusterYKurtosis;
	public static double clusterYPercentile10;
	public static double clusterYPercentile90;
	
	public static double clusterDMean;
	public static double clusterDStdDev;
	public static double clusterDSkewness;
	public static double clusterDKurtosis;
	public static double clusterDPercentile10;
	public static double clusterDPercentile90;
	
	public ArrayList<String> getLines(String pre) {
		ArrayList<String> res = new ArrayList<>();
		res.add(pre + "_area\t" + area);
		res.add(pre + "_count\t" + clusterCount);
		
		res.add(pre + "_area\t" + area);
		
		res.add(pre + "_clusterSizeMean\t" + clusterSizeMean);
		res.add(pre + "_clusterSizeStdDev\t" + clusterSizeStdDev);
		res.add(pre + "_clusterSizeSkewness\t" + clusterSizeSkewness);
		res.add(pre + "_clusterSizeKurtosis\t" + clusterSizeKurtosis);
		res.add(pre + "_clusterSizePercentile10\t" + clusterSizePercentile10);
		res.add(pre + "_clusterSizePercentile90\t" + clusterSizePercentile90);
		
		res.add(pre + "_clusterXMean\t" + clusterXMean);
		res.add(pre + "_clusterXStdDev\t" + clusterXStdDev);
		res.add(pre + "_clusterXSkewness\t" + clusterXSkewness);
		res.add(pre + "_clusterXKurtosis\t" + clusterXKurtosis);
		res.add(pre + "_clusterXPercentile10\t" + clusterXPercentile10);
		res.add(pre + "_clusterXPercentile90\t" + clusterXPercentile90);
		
		res.add(pre + "_clusterYMean\t" + clusterYMean);
		res.add(pre + "_clusterYStdDev\t" + clusterYStdDev);
		res.add(pre + "_clusterYSkewness\t" + clusterYSkewness);
		res.add(pre + "_clusterYKurtosis\t" + clusterYKurtosis);
		res.add(pre + "_clusterYPercentile10\t" + clusterYPercentile10);
		res.add(pre + "_clusterYPercentile90\t" + clusterYPercentile90);
		
		res.add(pre + "_clusterDMean\t" + clusterDMean);
		res.add(pre + "_clusterDStdDev\t" + clusterDStdDev);
		res.add(pre + "_clusterDSkewness\t" + clusterDSkewness);
		res.add(pre + "_clusterDKurtosis\t" + clusterDKurtosis);
		res.add(pre + "_clusterDPercentile10\t" + clusterDPercentile10);
		res.add(pre + "_clusterDPercentile90\t" + clusterDPercentile90);
		
		return res;
	}
}
