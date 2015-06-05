package workflow;

import java.awt.Color;
import java.io.File;
import java.util.LinkedList;
import java.util.TreeMap;

import org.AttributeHelper;
import org.graffiti.plugin.io.resources.FileSystemHandler;

import de.ipk.ag_ba.gui.picture_gui.BackgroundThreadDispatcher;
import de.ipk.ag_ba.gui.picture_gui.LocalComputeJob;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;

/**
 * Reads and image and quantifies (counts) the foreground pixels, marked with different colors.
 * For each color a the corresponding infection rate is calculated.
 * 
 * @author Christian Klukas
 */
public class Quantify {
	
	public static void main(String[] args) throws Exception {
		{
			new Settings(true);
		}
		if (args == null || args.length == 0) {
			System.err.println("No filenames provided as parameters! Return Code 1");
			System.exit(1);
		} else {
			LinkedList<LocalComputeJob> wait = new LinkedList<>();
			
			for (String a : args) {
				LinkedList<File> fl = new LinkedList<>();
				String path = new File(a).getParent();
				for (File f : new File(path).listFiles((fn) -> {
					if (!fn.getName().endsWith("_cluster.png") && !fn.getName().startsWith("classified_") && !fn.getName().startsWith("cluster"))
						return false;
					return fn.getName().startsWith(new File(a).getName());
				})) {
					fl.add(f);
				}
				fl.forEach((f) -> {
					if (!f.exists()) {
						System.err.println("File '" + a + "' could not be found! Return Code 2");
						System.exit(2);
					} else {
						try {
							wait.add(BackgroundThreadDispatcher.addTask(
									() -> {
										Image i;
										try {
											i = new Image(FileSystemHandler.getURL(f));
										} catch (Exception e) {
											throw new RuntimeException(e);
										}
										int[] img = i.getAs1A();
										int b = Settings.back;
										TreeMap<Integer, Integer> diseaseSymptomId2Area = new TreeMap<>();
										int fgArea = 0;
										for (int p : img) {
											if (p != b) {
												fgArea++;
												if (!diseaseSymptomId2Area.containsKey(p))
													diseaseSymptomId2Area.put(p, 0);
												diseaseSymptomId2Area.put(p, diseaseSymptomId2Area.get(p) + 1);
											}
										}
										TextFile tf = new TextFile();
										tf.add(f.getParent() + File.separator + f.getName() + "\t" + "leafarea_pixel" + "\t" + fgArea);
										for (Integer symp : diseaseSymptomId2Area.keySet()) {
											String colorName = AttributeHelper.getColorName(new Color(symp));
											tf.add(f.getParent() + File.separator + f.getName() + "\t" + "class_area_percent_" + colorName + "\t" + 100d
													* diseaseSymptomId2Area.get(symp)
													/ fgArea);
										}
										try {
											tf.write(f.getParent() + File.separator + f.getName() + "_quantified.csv");
										} catch (Exception e) {
											throw new RuntimeException(e);
										}
									}, "process " + f.getName()));
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					}
				});
			}
			BackgroundThreadDispatcher.waitFor(wait);
		}
	}
}
