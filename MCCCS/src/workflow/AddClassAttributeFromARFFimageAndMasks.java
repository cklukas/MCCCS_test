package workflow;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @author klukas
 *         Reads the image ARFF file, and all provided mask ARFF files.
 *         Outputs N rows for each class.
 *         Requires less memory, as all input ARFF files are read row by row, so only
 *         one row of each input ARFF needs to stay in memory. In addition sets of valid row indices for
 *         each mask file need to be constructed, to allow random selection of random samples. From the
 *         mask ARFF files, the correct class of the current image row is detected, and the according class
 *         information is added at the end of each image ARFF line.
 *         The input mask files need to be read two times, the first time the number of
 *         samples in each class are determined, so that in the main run the correct number
 *         of samples may be selected randomly from the class rows.
 */
public class AddClassAttributeFromARFFimageAndMasks {
	public static void main(String[] args) throws InterruptedException, IOException {
		if (args == null || args.length < 1) {
			System.err
					.println("The following parameters are required: [class sample size N] [output arff file] [image arff file] [mask arff file 0] [mask arff file 1] [mask arff file 2] [..] - "
							+ "At least two mask arff files should be provided (though one would be enough, but then all"
							+ "image rows would belong to a single class. Return Code 1");
			System.exit(1);
		} else {
			int sampleSize = Integer.parseInt(args[0]);
			String outputFile = args[1];
			String imageFile = args[2];
			TreeMap<Integer, TreeSet<Integer>> maskIdx2ValidRows = new TreeMap<>();
			for (int maskIndex = 3; maskIndex < args.length; maskIndex++) {
				String currentMaskImage = args[maskIndex];
				File f = new File(currentMaskImage);
				FileReader fileReader = new FileReader(f);
				BufferedReader bufferedReader = new BufferedReader(fileReader);
				String line;
				int row = 0;
				TreeSet<Integer> treeSet = new TreeSet<>();
				while ((line = bufferedReader.readLine()) != null) {
					boolean isCurrentArffFileRowAforegroundMaskPixelRow = line.contains("class1");
					if (isCurrentArffFileRowAforegroundMaskPixelRow)
						treeSet.add(row);
					row++;
				}
				fileReader.close();
				maskIdx2ValidRows.put(maskIndex, treeSet);
			}
		}
	}
}
