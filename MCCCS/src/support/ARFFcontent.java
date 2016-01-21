package support;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;

/**
 * @author klukas
 */
public class ARFFcontent {
	
	private String relationName = "";
	private List<String> headerLines = new ArrayList<>();
	private ArrayList<ArrayList<String>> columnData = new ArrayList<>();
	
	public void appendColumnData(File f) throws IOException {
		TextFile tf = new TextFile(f);
		ArrayList<String> dataLines = new ArrayList<>();
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
		columnData.add(dataLines);
	}
	
	public void writeTo(File file, HashSet<Integer> removeColumns) throws IOException {
		TextFile tf = new TextFile();
		
		tf.add("%");
		tf.add("@relation '" + relationName + "'");
		
		{
			int colIdx = 0;
			for (String hl : headerLines) {
				if (hl.startsWith("@attribute")) {
					colIdx++;
					if (!removeColumns.contains(colIdx))
						tf.add(hl);
				} else
					tf.add(hl);
			}
		}
		
		int maxLines = 0;
		for (ArrayList<String> dl : columnData)
			if (dl.size() > maxLines)
				maxLines = dl.size();
		
		tf.add("@data");
		
		for (int line = 0; line < maxLines; line++) {
			StringBuilder sb = new StringBuilder();
			for (ArrayList<String> cd : columnData) {
				if (sb.length() > 0)
					sb.append(",");
				sb.append(cd.get(line % cd.size()));
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
			tf.add(val);
		}
		
		tf.add("%");
		
		tf.write(file);
	}
}
