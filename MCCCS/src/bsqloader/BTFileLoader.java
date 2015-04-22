package bsqloader;

import java.io.File;
import java.io.FilenameFilter;

class BTFFileLoader extends ENVILoader {
	
	private ENVIHeader header;
	private FileReaderUtil fUtil;
	
	/**
	 * Search whole folder for hdr and bsq files.
	 * 
	 * @author Jean-Michel Pape
	 */
	public BTFFileLoader(String path) throws Exception {
		
		File file = new File(path);
		
		File[] list_h = file.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".hdr");
			}
		});
		
		File[] list_d = file.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".bsq");
			}
		});
		
		if (list_h.length != list_d.length)
			System.out.println("Warning, number of hdr and bsq files differ.");
		
		for (File f1 : list_h)
			for (File f2 : list_d) {
				System.out.println(f1.getName());
				System.out.println(f2.getName());
				if (f1.getName().equals(f2.getName())) {
					header = ENVIHeader.readHeaderFile(f1);
					fUtil = FileUtils.getFileReaderUtil(f2);
					
					f1 = null;
					f2 = null;
				}
			}
	}
	
	public BTFFileLoader(String hdr, String bsq) throws Exception {
		header = ENVIHeader.readHeaderFile(new File(hdr));
		fUtil = FileUtils.getFileReaderUtil(new File(bsq));
	}
	
	public float[][][] read() {
		return readBSQ(header, fUtil);
	}
}