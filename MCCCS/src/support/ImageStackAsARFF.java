package support;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;

import de.ipk.ag_ba.image.structures.ImageStack;

/**
 * @author klukas
 */
public class ImageStackAsARFF {
	
	private LinkedHashMap<String, ImageArff> fileName2image = new LinkedHashMap<>();
	
	public void show(String title) throws IOException {
		show(title, true);
	}
	
	public void show(String title, boolean doIt) throws IOException {
		if (!doIt)
			return;
		ImageStack is = new ImageStack();
		for (String fn : fileName2image.keySet()) {
			is.addImage(fn, fileName2image.get(fn).getImage());
		}
		is.show(title);
	}
	
	public void addImage(String fileName, ImageArff imageArff) {
		fileName2image.put(fileName, imageArff);
	}
	
	public String[] getLabels() {
		return fileName2image.keySet().toArray(new String[] {});
	}
	
	public int size() {
		return fileName2image.size();
	}
	
	public ImageArff getProcessor(int i) {
		return fileName2image.get(getLabels()[i]);
	}
	
	public Collection<ImageArff> values() {
		return fileName2image.values();
	}
	
	public void deleteSlice(int i) {
		String lbl = getLabels()[i - 1];
		fileName2image.get(lbl).content_tempFile.delete();
		fileName2image.remove(lbl);
	}
	
	public String getImageLabel(int i) {
		return getLabels()[i - 1];
	}
	
}
