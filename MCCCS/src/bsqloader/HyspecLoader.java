package bsqloader;

import java.awt.Color;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.ImageStack;

/**
 * @author Jean-Michel Pape
 */
public class HyspecLoader {
	
	private final BTFFileLoader loader;
	
	public HyspecLoader(String path_hdr, String path_bsq) throws Exception {
		loader = new BTFFileLoader(path_hdr, path_bsq);
	}
	
	public float[][][] getCube() {
		float[][][] data = loader.read();
		System.out.println(data.length);
		return data;
	}
	
	public ImageStack getCubeAsImageStack(double overflow) {
		return getCubeAsImageStack(getCube(), null, ImageStackViewMode.UNCHANGED, overflow);
	}
	
	private ImageStack getCubeAsImageStack(float[][][] cubeF, float[][][] cubeB, ImageStackViewMode viewMode, double overflow) {
		if (cubeB != null)
			cubeB = overflowThresholding(cubeB, overflow);
		if (cubeF != null)
			cubeF = overflowThresholding(cubeF, overflow);
		
		boolean mode4;
		switch (viewMode) {
			case BACKGROUND:
				cubeB = normalizeMaxToN(cubeB, 128, 0);
				mode4 = false;
				break;
			case FOREGROUND:
				System.out.println("WARNING: Normalizing Cube!");
				cubeF = normalizeMaxToN(cubeF, 255, 0);
				mode4 = false;
				break;
			case UNCHANGED:
				mode4 = false;
				break;
			default:
				cubeF = normalizeMaxToN(cubeF, 255, 0);
				cubeB = normalizeMaxToN(cubeB, 255, 0);
				mode4 = true;
				break;
		
		}
		
		ImageStack is = new ImageStack();
		
		for (int idxL = 0; idxL < cubeF.length; idxL++) {
			int[][] slice_i = null;
			float[][] slice_f = null;
			if (viewMode == ImageStackViewMode.UNCHANGED)
				slice_f = new float[cubeF[0].length][cubeF[0][0].length];
			else
				if (mode4)
					slice_i = new int[cubeF.length * 2][cubeF.length * 2];
				
				else
					slice_i = new int[cubeF[0].length][cubeF[0][0].length];
			
			for (int idxA = 0; idxA < cubeF[0].length; idxA++) {
				for (int idxB = 0; idxB < cubeF[0][0].length; idxB++) {
					// if (cubeF[idxL][idxA][idxB] > 0 && cubeB[idxL][idxA][idxB] > 0) {
					boolean set = false;
					float n = 0;
					int col;
					switch (viewMode) {
						case UNCHANGED:
							slice_f[idxA][idxB] = cubeF[idxL][idxA][idxB];
							break;
						case FOREGROUND:
							n = cubeF[idxL][idxA][idxB];
							set = true;
							// fall-through
						case BACKGROUND:
							if (!set)
								n = cubeB[idxL][idxA][idxB];
							// if (n > 255)
							// System.out.println("N=" + n);
							int dd = (int) n;
							if (dd <= 0)
								dd = 0;
							else
								if (dd > 255)
									dd = 255;
							col = new Color(dd, dd, dd).getRGB();
							slice_i[idxA][idxB] = col;
							break;
						case DIFFERENCE:
							float nf = cubeF[idxL][idxA][idxB];
							float nb = cubeB[idxL][idxA][idxB];
							slice_i[idxA * 2][idxB * 2] = new Color((int) nf, 0, 0).getRGB();
							slice_i[idxA * 2 + 1][idxB * 2 + 1] = new Color(0, 0, (int) nb).getRGB();
							float diff = nf - nb;
							if (diff < 0) {
								diff = -diff;
								diff = 255 - diff;
								col = new Color(0, (int) diff, (int) diff).getRGB();
								slice_i[idxA * 2 + 1][idxB * 2] = col;
							} else {
								diff = 255 - diff;
								col = new Color((int) diff, (int) diff, 0).getRGB();
								slice_i[idxA * 2 + 1][idxB * 2] = col;
							}
							break;
						default:
							break;
					
					}
					// }
				}
			}
			if (viewMode == ImageStackViewMode.UNCHANGED)
				is.addImage("Lab: " + idxL, new Image(slice_f), null, false);
			else
				is.addImage("Lab: " + idxL, new Image(slice_i));
		}
		return is;
	}
	
	private float[][][] overflowThresholding(float[][][] cube, double overflow) {
		float[][][] res = new float[cube.length][cube[0].length][cube[0][0].length];
		for (int idxL = 0; idxL < cube.length; idxL++) {
			for (int idxA = 0; idxA < cube[0].length; idxA++) {
				for (int idxB = 0; idxB < cube[0][0].length; idxB++) {
					if (cube[idxL][idxA][idxB] >= overflow && overflow > 0)
						res[idxL][idxA][idxB] = 0.0f;
					else
						res[idxL][idxA][idxB] = cube[idxL][idxA][idxB];
				}
			}
		}
		return res;
	}
	
	/**
	 * default:
	 * 
	 * @param N
	 *           - 255
	 * @return M - 0
	 */
	private float[][][] normalizeMaxToN(float[][][] cube, int N, int M) {
		float[][][] res = new float[cube.length][cube[0].length][cube[0][0].length];
		float max = 0.0f, min = Float.MAX_VALUE;
		SummaryStatistics ds = new SummaryStatistics();
		for (int idxL = 0; idxL < cube.length; idxL++) {
			for (int idxA = 0; idxA < cube[0].length; idxA++) {
				for (int idxB = 0; idxB < cube[0][0].length; idxB++) {
					float n = cube[idxL][idxA][idxB];
					ds.addValue(n);
					// n = (float) Math.log(n);
					if (n > max)
						max = n;
					if (n < min)
						min = n;
				}
			}
		}
		
		for (int idxL = 0; idxL < cube.length; idxL++) {
			for (int idxA = 0; idxA < cube[0].length; idxA++) {
				for (int idxB = 0; idxB < cube[0][0].length; idxB++) {
					if (cube[idxL][idxA][idxB] > 0)
						res[idxL][idxA][idxB] = 256f - (M + 1 / cube[idxL][idxA][idxB]) / 2f;
					// res[idxL][idxA][idxB] = 256f * ((256f - (M + 1 / cube[idxL][idxA][idxB]) / 2f) - min) / (max - min);
					else
						if (M > 0)
							res[idxL][idxA][idxB] = M;
						else
							res[idxL][idxA][idxB] = 0;
				}
			}
		}
		return res;
	}
	
	public enum ImageStackViewMode {
		DIFFERENCE, FOREGROUND, BACKGROUND, UNCHANGED,
	}
}
