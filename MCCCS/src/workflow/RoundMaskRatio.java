package workflow;

import java.io.File;
import java.io.IOException;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.graffiti.plugin.io.resources.FileSystemHandler;

import de.ipk.ag_ba.image.structures.Image;

/**
 * Determine the difference in the brightness of a inner circle to the area of a outer ring around that circle.
 * input: image A, target File, outer circle radius, dark or bright. Function is related to BlSpotMatcher in IAP.
 * output: contrast image
 * 
 * @author Christian Klukas
 */
public class RoundMaskRatio {
	
	public static void main(String[] args) throws IOException, Exception {
		{
			new Settings();
		}
		if (args == null || args.length != 4) {
			System.err.println("Params: [image A (float)] [target file] [circle radius] [TRUE/FALSE (bright spot search/dark spot search)] ! Return Code 1");
			System.exit(1);
		} else {
			File f_a = new File(args[0]);
			File f_out = new File(args[1]);
			double scanRadius = Double.parseDouble(args[2]);
			boolean brightSpots = Boolean.parseBoolean(args[3]);
			if (f_out.exists()) {
				System.err.println("Error - Output target file  '" + f_out.getName() + "' already exists! Return Code 2");
				System.exit(2);
			} else
				if (!f_a.exists()) {
					System.err.println("File image A '" + f_a.getName() + "' could not be found! Return Code 4");
					System.exit(4);
				} else {
					Image imgA = new Image(FileSystemHandler.getURL(f_a));
					float[] imgAf = imgA.getAs1float();
					
					float[] out = new float[imgAf.length];
					
					new Image(imgA.getWidth(), imgA.getHeight(), out).saveToFile(f_out.getPath());
					
					boolean darkSpots = !brightSpots;
					double max = 0;
					
					int sr = (int) scanRadius;
					boolean[][] okMaskO = new boolean[(int) (scanRadius * 2) + 1][(int) (scanRadius * 2) + 1];
					boolean[][] okMaskI = new boolean[(int) (scanRadius * 2) + 1][(int) (scanRadius * 2) + 1];
					for (int offX = -sr; offX <= sr; offX++) {
						for (int offY = -sr; offY <= scanRadius; offY++) {
							// if ((offX + offY) % 10 != 0)
							// continue;
							double dist = Math.sqrt(offX * offX + offY * offY);
							if (dist < scanRadius)
								okMaskO[offX + sr][offY + sr] = true;
							if (dist < scanRadius / 2)
								okMaskI[offX + sr][offY + sr] = true;
							
						}
					}
					// DescriptiveStatistics statI = new DescriptiveStatistics();
					// DescriptiveStatistics statO = new DescriptiveStatistics();
					SummaryStatistics statI = new SummaryStatistics();
					SummaryStatistics statO = new SummaryStatistics();
					
					int w = imgA.getWidth();
					int h = imgA.getHeight();
					int sr2 = sr * 2;
					for (int x = 0; x < w; x++) {
						for (int y = 0; y < h; y++) {
							statI.clear();
							statO.clear();
							for (int offX = 0; offX <= sr2; offX++) {
								for (int offY = 0; offY <= sr2; offY++) {
									if (okMaskO[offX][offY]) {
										int xi = x + offX - sr;
										int yi = y + offY - sr;
										if (xi >= 0 && yi >= 0 && xi < w && yi < h) {
											float v = imgAf[xi + yi * w];
											if (okMaskI[offX][offY]) {
												statI.addValue(v);
											} else {
												statO.addValue(v);
											}
											if (v > max)
												max = v;
										}
									}
								}
							}
							double averageInner = statI.getMean();// statI.getPercentile(darkSpots ? 50 : 50);
							double averageOuter = statO.getMean();// statO.getPercentile(darkSpots ? 50 : 50);
							
							if (darkSpots) {
								out[x + y * w] = (float) (Math.log(averageOuter) - Math.log(averageInner));
							} else {
								out[x + y * w] = (float) (Math.log(averageInner) - Math.log(averageOuter));
							}
						}
					}
					new Image(imgA.getWidth(), imgA.getHeight(), out).saveToFile(f_out.getPath());
				}
		}
	}
}
