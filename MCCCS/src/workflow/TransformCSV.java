package workflow;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.StringManipulationTools;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;

/**
 * Reads the CSV result file and transforms some rows to columns and
 * renames infection color ids to human-readable traits.
 * Cleans-up some content of the column entries.
 * 
 * @author Christian Klukas
 */
public class TransformCSV {
	
	public static void main(String[] args) throws Exception {
		{
			new Settings();
		}
		if (args == null || args.length == 0) {
			System.err.println("No filenames provided as parameters! Return Code 1");
			System.exit(1);
		} else {
			for (String a : args) {
				File f = new File(a);
				if (!f.exists()) {
					System.err.println("File '" + a + "' could not be found! Return Code 2");
					System.exit(2);
				} else {
					TextFile tf = new TextFile(f);
					TreeSet<String> globalColumnSet = new TreeSet<>();
					LinkedHashMap<String, TreeMap<String, Double>> values = new LinkedHashMap<>();
					for (String l : tf) {
						String[] cols = l.split("\t");
						String file = cols[0];
						String trait = cols[1];
						Double value = Double.parseDouble(cols[2]);
						globalColumnSet.add(trait);
						if (!values.containsKey(file))
							values.put(file, new TreeMap<String, Double>());
						values.get(file).put(trait, value);
					}
					tf.clear();
					
					TextFile transformed = new TextFile();
					transformed.add("File\t" + StringManipulationTools.getStringList(globalColumnSet, "\t"));
					for (String file : values.keySet()) {
						StringBuilder rowContent = new StringBuilder();
						rowContent.append(file);
						TreeMap<String, Double> kv = values.get(file);
						for (String col : globalColumnSet) {
							Double v = kv.get(col);
							if (v == null)
								rowContent.append("\t" + "");
							else
								rowContent.append("\t" + v);
						}
						transformed.add(rowContent.toString());
					}
					transformed.write(f.getPath() + ".transformed");
				}
			}
		}
	}
}
