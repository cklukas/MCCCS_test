package support;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import de.ipk.ag_ba.image.structures.Image;

/**
 * @author klukas
 */
public class ImageArff {
	
	File content_tempFile = null;
	
	int w = -1;
	int h = -1;
	
	public ImageArff(Image image, String datasetName, String intensityName) throws IOException {
		w = image.getWidth();
		h = image.getHeight();
		
		String attributes = "@attribute " + intensityName + "\tNUMERIC\n";
		
		String header = "%\n" + "@relation '" + datasetName + "'\n" + attributes
				+ "@data\n";
		
		float[] intensity = image.getAs1float();
		
		int width = image.getWidth();
		int height = image.getHeight();
		
		String line = "";
		File tf = File.createTempFile("mcccs_", ".arff");
		content_tempFile.deleteOnExit();
		FileWriter fw = new FileWriter(tf, false);
		
		fw.write(header);
		
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				line += intensity[x + y * width];
				
				if (line.length() > 0) {
					// appends the string to the file
					fw.write(line + "\n");
					line = "";
				}
			}
		}
		
		fw.write("%");
		fw.close();
		
		content_tempFile = tf;
	}
	
	public Image getImage() throws IOException {
		FileReader fr = new FileReader(content_tempFile);
		BufferedReader br = new BufferedReader(fr);
		
		float[] px = new float[w * h];
		
		boolean dataAvailable = false;
		String line;
		int idx = 0;
		while ((line = br.readLine()) != null) {
			if (line.startsWith("@data"))
				dataAvailable = true;
			else {
				if (dataAvailable && !line.startsWith("%")) {
					px[idx++] = Float.parseFloat(line);
				}
			}
		}
		
		br.close();
		return new Image(w, h, px);
	}
}
