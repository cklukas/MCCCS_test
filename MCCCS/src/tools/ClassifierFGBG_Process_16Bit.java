package tools;

import java.io.File;
import java.io.IOException;

import workflow.Settings;
import de.ipk.ag_ba.image.structures.ImageStack;

/**
 * @author pape, klukas
 */
public class ClassifierFGBG_Process_16Bit extends AbstractClassifier_16Bit {
	
	private final String arffFileName;
	
	public ClassifierFGBG_Process_16Bit(String arffFileName) {
		this.arffFileName = arffFileName;
	}
	
	@Override
	public void createSampleData(ImageStack[] isl, File f, int numberofsamples) throws InterruptedException, IOException {
		ImageStack is = isl[0];
		ImageStack masks = isl[1];
		
		// apply masks ...
		ImageStack applyedM = applyMasks(is, masks, false);
		
		// get fg & bg
		ImageStack[] fgbg = getFGBG(applyedM);
		
		if (false) {
			is.show("input images");
			masks.show("mask images");
			applyedM.show("masked");
		}
		// ARFFProcessor.createTrainingDataSet(gtApplied, 0.0f, f, numberofsamples, arffFileName, false, "label", false); // back => Float.MAX_VALUE
		ARFFProcessor.createTrainingDataSet(fgbg, Settings.back, f, numberofsamples, arffFileName, false, "fgbg"); // Float.MAX_VALUE
	}
	
	private ImageStack[] getFGBG(ImageStack applyedM) {
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
