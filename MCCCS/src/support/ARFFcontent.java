package support;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Stream;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;

/**
 * @author klukas
 */
public class ARFFcontent {
	
	private String relationName = "";
	private List<String> headerLines = new ArrayList<>();
	private ArrayList<File> columnData = new ArrayList<File>();
	private ArrayList<Integer> columnDataLineCount = new ArrayList<Integer>();
	
	public void appendColumnData(File f) throws IOException {
		TextFile tf = new TextFile(f);
		TextFile dataLines = new TextFile();
		boolean header = true;
		for (String l : tf) {
			if (header) {
				if (!l.startsWith("%"))
					if (l.contains("@relation")) {
						String rn = l.substring(l.indexOf("@relation") + "@relation".length());
						rn = rn.trim();
						if (rn.startsWith("'"))
							rn = rn.substring(1, rn.length() - 2);
						if (relationName.length() > 0)
							relationName += "_";
						relationName += rn;
					} else
						if (!l.contains("@data"))
							headerLines.add(l);
						else
							header = false;
			} else {
				if (!l.startsWith("%"))
					dataLines.add(l);
			}
		}
		File tempF = File.createTempFile("iccs_", ".txt");
		tempF.deleteOnExit();
		dataLines.write(tempF);
		columnData.add(tempF);
		columnDataLineCount.add(dataLines.size());
	}
	
	public void writeTo(File file, HashSet<Integer> removeColumns) throws IOException {
		FileWriter tf = new FileWriter(file);
		
		tf.write("%" + System.lineSeparator());
		tf.write("@relation '" + relationName + "'" + System.lineSeparator());
		
		{
			int colIdx = 0;
			for (String hl : headerLines) {
				if (hl.startsWith("@attribute")) {
					colIdx++;
					if (!removeColumns.contains(colIdx))
						tf.write(hl + System.lineSeparator());
				} else
					tf.write(hl + System.lineSeparator());
			}
		}
		
		int maxLines = 0;
		for (Integer lineCount : columnDataLineCount)
			if (lineCount > maxLines)
				maxLines = lineCount;
		
		tf.write("@data" + System.lineSeparator());
		
		LinkedHashMap<File, Stream<String>> file2stream = new LinkedHashMap<>();
		LinkedHashMap<File, Iterator<String>> file2input = new LinkedHashMap<>();
		
		for (int line = 0; line < maxLines; line++) {
			StringBuilder sb = new StringBuilder();
			
			for (File f : file2input.keySet()) {
				if (file2input.get(f) != null && !file2input.get(f).hasNext()) {
					file2stream.get(f).close();
					file2stream.remove(f);
					file2input.remove(f);
				}
				if (file2input.get(f) == null) {
					file2stream.put(f, Files.lines(f.toPath()));
					file2input.put(f, file2stream.get(f).iterator());
				}
				Iterator<String> s = file2input.get(f);
				if (s == null || !s.hasNext()) {
					s = Files.lines(f.toPath()).iterator();
				}
				if (sb.length() > 0)
					sb.append(",");
				sb.append(s.next());
			}
			
			String val = sb.toString();
			if (removeColumns.size() > 0) {
				String[] valArr = val.split(",");
				sb = new StringBuilder();
				for (int colIdx = 1; colIdx <= valArr.length; colIdx++) {
					if (!removeColumns.contains(colIdx)) {
						if (sb.length() > 0)
							sb.append(",");
						sb.append(valArr[colIdx - 1]);
					}
				}
				val = sb.toString();
			}
			tf.write(val + System.lineSeparator());
		}
		
		for (File f : file2stream.keySet())
			file2stream.get(f).close();
		
		tf.write("%" + System.lineSeparator());
		
		tf.close();
	}
}
