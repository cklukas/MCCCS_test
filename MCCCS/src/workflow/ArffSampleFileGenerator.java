package workflow;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import tools.ClassifierDisease_Process_16Bit;
import tools.ClassifierFGBG_Process_16Bit;
import tools.IO_MacroBot;
import de.ipk.ag_ba.gui.picture_gui.BackgroundThreadDispatcher;
import de.ipk.ag_ba.gui.picture_gui.LocalComputeJob;
import de.ipk.ag_ba.image.structures.ImageStack;

/**
 * Sample Extraction from input images, generates .arff file for classifier training.
 * input: channel-count, class-count (negative in case of foreground/background segmentation), sample-size, filenames
 * 
 * @author Jean-Michel Pape, Christian Klukas
 */
public class ArffSampleFileGenerator {
	
	public static void main(String[] args) throws InterruptedException, IOException {
		{
			new Settings();
		}
		if (args == null || args.length < 4) {
			System.err
					.println("No parameter for [channel-count], [[-]class-count, negative for FGBG separation], [sample-size] and / or no [filenames] provided as parameters! Return Code 1");
			System.exit(1);
		} else {
			int parmCount = 0;
			boolean isFGBG = true;
			for (String a : args) {
				if (parmCount == 0) {
					Settings.numberOfChannels = Integer.parseInt(a);
					parmCount++;
					continue;
				}
				
				if (parmCount == 1) {
					Settings.numberOfClasses = Math.abs(Integer.parseInt(a));
					if (Integer.parseInt(a) < 0)
						isFGBG = true;
					else
						isFGBG = false;
					
					parmCount++;
					continue;
				}
				
				if (parmCount == 2) {
					Settings.sampleSize = Integer.parseInt(a);
					parmCount++;
					continue;
				}
				
				final boolean f_isFGBG = isFGBG;
				LinkedList<File> fl = new LinkedList<>();
				if (a.contains("*")) {
					String path = new File(a).getParent();
					for (File f : new File(path).listFiles((fn) -> {
						return fn.getName().startsWith(new File(a).getName().substring(0, new File(a).getName().indexOf("*")));
					})) {
						fl.add(f);
					}
				} else {
					fl.add(new File(a));
				}
				LinkedList<LocalComputeJob> wait = new LinkedList<>();
				LinkedList<LocalComputeJob> wait_inner = new LinkedList<>();
				for (File f : fl) {
					wait.add(BackgroundThreadDispatcher.addTask(() -> {
						// Read data for Training
							try {
								IO_MacroBot io = new IO_MacroBot(f);
								
								ImageStack[] isl = io.readTrainingData(false, f_isFGBG);
								
								if (Settings.debug_IO) {
									for (ImageStack st : isl)
										st.show("debug_IO");
								}
								
								// --------- Part1 Sample Extraction ---------
								if (f_isFGBG) {
									// segmentation fgbg
									wait_inner.add(BackgroundThreadDispatcher.addTask(() -> {
										ClassifierFGBG_Process_16Bit fgbgClassifier = new ClassifierFGBG_Process_16Bit("fgbgTraining");
										try {
											fgbgClassifier.createSampleData(isl, f, Settings.sampleSize);
										} catch (Exception e) {
											e.printStackTrace();
											throw new RuntimeException(e);
										}
									}, "process fgbg"));
								} else {
									// disease classification
									wait_inner.add(BackgroundThreadDispatcher.addTask(() -> {
										ClassifierDisease_Process_16Bit diseaseClassifier = new ClassifierDisease_Process_16Bit("labelTraining");
										try {
											diseaseClassifier.createSampleData(isl, f, Settings.sampleSize);
										} catch (Exception e) {
											e.printStackTrace();
											throw new RuntimeException(e);
										}
									}, "process label"));
								}
							} catch (Exception e) {
								throw new RuntimeException(e);
							}
						}, "process " + a));
				}
				
				BackgroundThreadDispatcher.waitFor(wait);
				BackgroundThreadDispatcher.waitFor(wait_inner);
			}
		}
	}
}
