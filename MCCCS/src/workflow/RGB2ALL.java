package workflow;

import ij.ImagePlus;
import ij.process.FloatProcessor;

import java.io.File;
import java.io.IOException;

import org.graffiti.plugin.io.resources.FileSystemHandler;

import colors.ChannelProcessingExt;
import colors.ColorSpaceExt;
import colors.RgbColorSpaceExt;
import de.ipk.ag_ba.image.structures.Image;

/**
 * Create color channel files in diverse color spaces. input: R/G/B image files
 * output:channel_xyz_x.png, ... (diverse set of color channels)
 * 
 * @author Christian Klukas
 */
public class RGB2ALL {
	
	public static void main(String[] args) throws IOException, Exception {
		{
			new Settings();
		}
		if (args == null || args.length != 4) {
			System.err
					.println("Params: [r] [g] [b] [source bit range 8/16/..] ! Return Code 1");
			System.exit(1);
		} else {
			File f_r = new File(args[0]);
			File f_g = new File(args[1]);
			File f_b = new File(args[2]);
			if (!f_r.exists()) {
				System.err.println("File RGB - R '" + f_r.getName()
						+ "' could not be found! Return Code 2");
				System.exit(2);
			} else
				if (!f_g.exists()) {
					System.err.println("File RGB - G '" + f_g.getName()
							+ "' could not be found! Return Code 2");
					System.exit(2);
				} else
					if (!f_b.exists()) {
						System.err.println("File RGB - B '" + f_b.getName()
								+ "' could not be found! Return Code 2");
						System.exit(2);
					} else {
						// SystemAnalysis.simulateHeadless = false;
						Image r = new Image(FileSystemHandler.getURL(f_r));
						Image g = new Image(FileSystemHandler.getURL(f_g));
						Image b = new Image(FileSystemHandler.getURL(f_b));
						float divistorFor8bitRangeTarget = (float) (Math.pow(2,
								Float.parseFloat(args[3])) / 256);
						float[] r01 = getFloat(r);
						float[] g01 = getFloat(g);
						float[] b01 = getFloat(b);
						for (int idx = 0; idx < r01.length; idx++) {
							r01[idx] = r01[idx] / divistorFor8bitRangeTarget / 255f;
							g01[idx] = g01[idx] / divistorFor8bitRangeTarget / 255f;
							b01[idx] = b01[idx] / divistorFor8bitRangeTarget / 255f;
						}
						
						for (RgbColorSpaceExt rgb : RgbColorSpaceExt.values()) {
							// if (rgb != RgbColorSpaceExt.AdobeRGB_D65)
							// continue;
							for (ColorSpaceExt cs : ColorSpaceExt.values()) {
								// if (cs != ColorSpaceExt.Lab)
								// continue;
								// if (ce != ChannelExt.Lab_L)
								// continue;
								float[] rc01 = new float[r01.length];
								System.arraycopy(r01, 0, rc01, 0, r01.length);
								
								float[] gc01 = new float[g01.length];
								System.arraycopy(g01, 0, gc01, 0, g01.length);
								
								float[] bc01 = new float[b01.length];
								System.arraycopy(b01, 0, bc01, 0, b01.length);
								
								ChannelProcessingExt cpe = new ChannelProcessingExt(r.getWidth(), r.getHeight(), rc01, gc01, bc01);
								int idx = 0;
								for (ImagePlus ip : cpe.getImage(rgb, cs)) {
									new Image(ip).saveToFile(
											f_r.getParent() + File.separator
													+ "channel_" + cs.getChannels()[idx++].getID() + "_" + rgb.getID() + ".tif");
								}
							}
						}
					}
		}
	}
	
	private static float[] getFloat(Image r) {
		if (r.getAsImagePlus().getProcessor() instanceof FloatProcessor)
			return r.getAs1float();
		else {
			int[] inta = r.getAs1Ar();
			float[] res = new float[inta.length];
			for (int i = 0; i < inta.length; i++) {
				res[i] = inta[i];
			}
			return res;
		}
	}
}
