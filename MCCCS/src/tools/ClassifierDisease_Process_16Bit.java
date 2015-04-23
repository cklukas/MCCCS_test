package tools;

import ij.process.ImageProcessor;

import java.io.File;
import java.io.IOException;

import workflow.Settings;
import de.ipk.ag_ba.image.structures.ImageStack;

public class ClassifierDisease_Process_16Bit extends AbstractClassifier_16Bit {
	
	private final String arffFileName;
	
	public ClassifierDisease_Process_16Bit(String arffFileName) {
		this.arffFileName = arffFileName;
	}
	
	@Override
	public void createSampleData(ImageStack[] isl, File f, int numberofsamples) throws InterruptedException, IOException {
		ImageStack is = isl[0];
		ImageStack masks = isl[1];
		ImageStack gt = isl[2];
		
		boolean showOutput = false;
		boolean removeFromStack = !showOutput;
		
		// apply masks ...
		ImageStack applyedM = applyMasks(is, masks, removeFromStack);
		
		// get fg & bg
		ImageStack[] fgbg = getFGBG2(applyedM);
		
		// apply gt
		ImageStack[] gtApplied = applyGTImages(fgbg, gt, false);
		
		if (showOutput) {
			is.show("input images");
			masks.show("mask images");
			applyedM.show("masked");
			
			for (ImageStack s : fgbg)
				s.show("fgbg");
			
			for (ImageStack s : gtApplied)
				s.show("gtapplied");
		} else {
			while (masks.getStack().getSize() > 0)
				masks.getStack().deleteLastSlice();
			applyedM = null;
			while (gt.getStack().getSize() > 0)
				gt.getStack().deleteLastSlice();
		}
		ARFFProcessor.createTrainingDataSet(gtApplied, 0.0f, f, numberofsamples, arffFileName, true, "label", false); // back => Float.MAX_VALUE
	}
	
	/**
	 * Returns applied disease stacks for all channels: {class1 <- r,g,b,...; class2 <- r,g,b,...; ...}
	 * 
	 * @param fgbg
	 * @param gt
	 * @return
	 */
	private ImageStack[] applyGTImages(ImageStack[] fgbg, ImageStack gt, boolean removeFromStack) {
		ImageStack fg = fgbg[0];
		
		if (Settings.debug_IO) {
			fg.show("fg");
			gt.show("gt");
		}
		
		ImageStack[] stacks = new ImageStack[gt.size()];
		
		for (int i = 0; i < stacks.length; i++) {
			stacks[i] = new ImageStack();
		}
		
		// String[] gtlabels = gt.getLabels();
		String[] fglabels = fg.getLabels();
		int count = fg.size();
		for (int i = 0; i < count; i++) {
			for (int j = 0; j < gt.size(); j++) {
				ImageProcessor d = fg.getProcessor(removeFromStack ? 0 : i).duplicate();
				d.fill(gt.getProcessor(j).convertToByteProcessor());
				stacks[j].addImage(fglabels[i], d);
				// stacks[j].addImage(gtlabels[j] + "_" + fglabels[i], d);
			}
			if (removeFromStack)
				fg.getStack().deleteSlice(1);
		}
		
		// for (ImageStack s : stacks) {
		// s.show("debug gtapply");
		// }
		
		return stacks;
	}
	
	private ImageStack[] getFGBG(ImageStack applyedM) {
		ImageStack fgStack = new ImageStack();
		ImageStack bgStack = new ImageStack();
		
		String[] labels = applyedM.getLabels();
		
		for (int i = 0; i < applyedM.size(); i += 2) {
			ImageProcessor bg = applyedM.getProcessor(i).duplicate();
			bg.fill(applyedM.getProcessor(i + 1).convertToByteProcessor());
			fgStack.addImage(labels[i], bg);
			bgStack.addImage(labels[i + 1], applyedM.getProcessor(i + 1));
		}
		return new ImageStack[] { fgStack, bgStack };
	}
	
	private ImageStack[] getFGBG2(ImageStack applyedM) {
		ImageStack fgStack = new ImageStack();
		ImageStack bgStack = new ImageStack();
		
		String[] labels = applyedM.getLabels();
		
		for (int i = 0; i < applyedM.size(); i += 2) {
			// ImageProcessor bg = applyedM.getProcessor(i).duplicate();
			// bg.fill(applyedM.getProcessor(i + 1).convertToByteProcessor());
			fgStack.addImage(labels[i], applyedM.getProcessor(i));
			bgStack.addImage(labels[i + 1], applyedM.getProcessor(i + 1));
		}
		return new ImageStack[] { fgStack, bgStack };
	}
}
