package tools;

import java.io.File;

import de.ipk.ag_ba.image.structures.ImageStack;

import ij.process.ImageProcessor;

public abstract class AbstractClassifier_16Bit {
	protected final boolean debug = false;
	protected float background_16bit = 0f;
	
	protected int numberOfClasses = 0;
	
	public abstract void createSampleData(ImageStack[] isl, File outputPathForArrfFile, int numberofsamples)
			throws Exception;
			
	protected ImageStack applyMasks(ImageStack imgst, ImageStack maskst, boolean removeFromStack) {
		ImageStack out = new ImageStack();
		/*
		 * int img_count = 1;
		 * for (ImageProcessor img : imgst) {
		 * int gt_count = 1;
		 * for (ImageProcessor m : maskst) {
		 * ImageProcessor applyed = img.duplicate();
		 * applyed.fill(m.convertToByteProcessor());
		 * out.addImage(imgst.getImageLabel(img_count), applyed);
		 * }
		 * img_count++;
		 * }
		 * return out;
		 */
		
		int size = imgst.size();
		for (int img_count = 0; img_count < size; img_count++) {
			ImageProcessor img = imgst.getProcessor(removeFromStack ? 0 : img_count);
			for (ImageProcessor m : maskst) {
			ImageProcessor applyed = img.duplicate();
			applyed.fill(m.convertToByteProcessor());
			out.addImage(imgst.getImageLabel(removeFromStack ? 1 : img_count + 1), applyed);
			}
			if (removeFromStack)
			imgst.getStack().deleteSlice(1);
		}
		return out;
	}
}
