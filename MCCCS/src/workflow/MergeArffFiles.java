package workflow;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;

import support.ARFFcontent;

/**
 * Merges two or more ARFF files (concatinates the column data)
 * 
 * @author Christian Klukas
 */
public class MergeArffFiles {
	
	public static void main(String[] args) throws Exception {
		if (args == null || args.length < 2) {
			System.err.println("No [targetfile] [filenames] [-ColIndex] for ARFF files to be merged specified, "
					+ "optionally specific columns may be removed from the output (1...x)! Return Code 1");
			System.exit(1);
		} else {
			LinkedList<File> fl = new LinkedList<>();
			HashSet<Integer> removeColumns = new HashSet<>();
			boolean first = true;
			for (String a : args) {
				if (a.startsWith("-")) {
					removeColumns.add(Integer.parseInt(a.substring("-".length())));
				} else {
					if (!first)
						fl.add(new File(a));
					first = false;
				}
			}
			ARFFcontent ac = new ARFFcontent();
			for (File f : fl)
				ac.appendColumnData(f);
			ac.writeTo(new File(args[0]), removeColumns);
		}
	}
}
