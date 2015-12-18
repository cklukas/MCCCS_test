package tools;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.ContrastEnhancer;
import ij.plugin.ZProjector;
import ij.plugin.filter.Convolver;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

/**
 * This script calculates a set of Gabor filters over the selected image.
 * Parameters: sigma, gamma, psi, Fx, nAngles
 **/
public class GaborFilter {
	
	// Sigma defining the size of the Gaussian envelope
	double sigma = 8.0;
	// Aspect ratio of the Gaussian curves
	double gamma = 0.25;
	// Phase
	double psi = Math.PI / 4.0 * 0;
	// Frequency of the sinusoidal component
	double Fx = 3.0;
	
	// Number of diferent orientation angles to use
	int nAngles = 5;
	
	// Apply aspect ratio to the Gaussian curves
	double sigma_x = sigma;
	double sigma_y = sigma / gamma;
	
	int width;
	int height;
	ImagePlus originalImage;
	private ImagePlus result;
	private final boolean debug = false;
	private ImageStack is;
	
	public GaborFilter(ImagePlus imgplus) {
		// copy original image and transform it to 32 bit
		originalImage = imgplus;
		originalImage = new ImagePlus(originalImage.getTitle(), originalImage.getProcessor().convertToFloat());
		this.width = originalImage.getWidth();
		this.height = originalImage.getHeight();
	}
	
	public ImagePlus getResult() {
		return result;
	}
	
	public ImageStack getIs() {
		return is;
	}
	
	public void run() {
		// Decide size of the filters based on the sigma
		int largerSigma = (sigma_x > sigma_y) ? (int) sigma_x : (int) sigma_y;
		if (largerSigma < 1)
			largerSigma = 1;
			
		ImageProcessor ip = originalImage.getProcessor().duplicate();
		
		double sigma_x2 = sigma_x * sigma_x;
		double sigma_y2 = sigma_y * sigma_y;
		
		// Create set of filters
		
		int filterSizeX = 19; // 6 * largerSigma + 1;
		int filterSizeY = 19; // 6 * largerSigma + 1;
		
		int middleX = Math.round(filterSizeX / 2);
		int middleY = Math.round(filterSizeY / 2);
		
		ImageStack is = new ImageStack(width, height);
		ImageStack kernels = new ImageStack(filterSizeX, filterSizeY);
		
		double rotationAngle = Math.PI / nAngles;
		double theta;
		double c;
		// Rotate kernel from 0 to 180 degrees
		for (int i = 0; i < nAngles; i++) {
			theta = rotationAngle * i;
			FloatProcessor filter = new FloatProcessor(filterSizeX, filterSizeY);
			for (int x = -middleX; x <= middleX; x++) {
			for (int y = -middleY; y <= middleY; y++) {
				double xPrime = x * Math.cos(theta) + y * Math.sin(theta);
				double yPrime = y * Math.cos(theta) - x * Math.sin(theta);
				
				double a = 1.0 / (2.0 * Math.PI * sigma_x * sigma_y) *
						Math.exp(-0.5 * (xPrime * xPrime / sigma_x2 + yPrime * yPrime / sigma_y2));
				c = Math.cos(2.0 * Math.PI * (Fx * xPrime) / filterSizeX + psi);
				
				filter.setf(x + middleX, y + middleY, (float) (a * c));
			}
			}
			kernels.addSlice("kernel angle = " + theta, filter);
		}
		
		// Show kernels
		ImagePlus ip_kernels = new ImagePlus("kernels", kernels);
		// ip_kernels.show();
		
		// Apply kernels
		for (int i = 0; i < nAngles; i++) {
			theta = rotationAngle * i;
			Convolver conv = new Convolver();
			
			float[] kernel = (float[]) kernels.getProcessor(i + 1).getPixels();
			ip = originalImage.getProcessor().duplicate();
			conv.convolveFloat(ip, kernel, filterSizeX, filterSizeY);
			
			is.addSlice("gabor angle = " + i, ip);
		}
		
		// Normalize filtered stack
		ContrastEnhancer ce = new ContrastEnhancer();
		for (int i = 1; i <= is.getSize(); i++) {
			ce.stretchHistogram(is.getProcessor(i), 0.4);
		}
		
		ImagePlus projectStack = new ImagePlus("filtered stack", is);
		IJ.run(projectStack, "Enhance Contrast", "saturated=0.4 normalize normalize_all");
		
		ImageStack resultStack = new ImageStack(width, height);
		
		ZProjector zp = new ZProjector(projectStack);
		zp.setStopSlice(is.getSize());
		for (int i = 0; i <= 5; i++) {
			zp.setMethod(i);
			zp.doProjection();
			resultStack.addSlice("Gabor_" + i
				+ "_" + sigma + "_" + gamma + "_" + (int) (psi / (Math.PI / 4)) + "_" + Fx,
				zp.getProjection().getChannelProcessor());
		}
		
		// Display filtered images
		// (new ImagePlus("gabor, sigma=" + sigma + " gamma=" + gamma + " psi=" + psi, is)).show();
		
		ImagePlus result = new ImagePlus("Gabor stack projections", resultStack);
		IJ.run(result, "Enhance Contrast", "saturated=0.4 normalize normalize_all");
		
		this.result = result;
		this.is = is;
		
		if (debug)
			result.show();
	}
}