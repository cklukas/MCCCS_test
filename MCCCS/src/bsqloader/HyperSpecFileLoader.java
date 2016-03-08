package bsqloader;

import java.io.File;
import java.io.FilenameFilter;

import bsqloader.HyspecLoader.HyperSpecDataMode;

class HyperSpecFileLoader extends ENVILoader {
	
	private ENVIHeader header;
	private FileReaderUtil fUtil;
	
	/**
	 * Search whole folder for hdr and bsq,bil,btf files.
	 * 
	 * @author Jean-Michel Pape
	 * @param mode 
	 * @param  
	 */
	public HyperSpecFileLoader(String path) throws Exception {
		
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
	
	public HyperSpecFileLoader(String hdr, String data) throws Exception {
		header = ENVIHeader.readHeaderFile(new File(hdr));
		fUtil = FileUtils.getFileReaderUtil(new File(data));
	}
	
	public float[][][] read(HyperSpecDataMode hymode) {
		switch(hymode) {
			case BIL:
				return readBIL(header, fUtil);
			case BSQ:
				return readBSQ(header, fUtil);
			case BIP:
				return readBIP(header, fUtil);
			default:
				return null;
		}

	}

	public HyperSpecDataMode getDataMode() {
		String h = header.getInterleave();
		try {
			for (HyperSpecDataMode mode : HyperSpecDataMode.values()) {
				if(mode.getName().contains(h))
					return mode;
			}
		} catch (Exception e) {
			System.out.println("Data mode not supported.");
		}
		return null;
	}
}