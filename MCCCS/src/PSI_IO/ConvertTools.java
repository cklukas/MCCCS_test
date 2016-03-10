package PSI_IO;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

/**
 * 
 * @author pape
 *
 */
public class ConvertTools {

	/**
	 * 
	 * @param input .fimg file (result file 32-bit real as used by PSII measurements)
	 * @return 
	 * @throws IOException
	 */
	public static ImageProcessor fimgToTif(File input) throws IOException {
		
		boolean use_little_endian = true;
		FileInputStream fis = new FileInputStream(input);
		
		// read header 16 bytes
		byte [] header = new byte[8];
		fis.read(header);

		//  create a byte buffer and wrap the array
		ByteBuffer bb = ByteBuffer.wrap(header);

		//  if the file uses little endian as apposed to network
		//  (big endian, Java's native) format,
		//  then set the byte order of the ByteBuffer
		if(use_little_endian)
		    bb.order(ByteOrder.LITTLE_ENDIAN);
				
		int width = bb.getInt();
		int height = bb.getInt();
		// not in header -> error in format?
		//int bitsPerPixel = bb.getInt();
		//int bytesPerPixel = bb.getInt();
		
		// System.out.println("width: " + width + " height: " + height);

		float[] out = new float[width * height];
		
		// read into data array
		byte[] data = new byte[width * height * 4];
		fis.read(data);
		
		ByteBuffer bbuf = ByteBuffer.wrap(data);
		
		for(int idx = 0; idx < width * height; idx++) {
			// 32-bit real
			byte b4 = bbuf.get();
			byte b3 = bbuf.get();
			byte b2 = bbuf.get();
			byte b1 = bbuf.get();
			int i = 0;
			i |= b1 & 0xFF;
			i <<= 8;
			i |= b2 & 0xFF;
			i <<= 8;
			i |= b3 & 0xFF;
			i <<= 8;
			i |= b4 & 0xFF;
			
			float f = Float.intBitsToFloat(i);
			out[idx] = f;
		}

		fis.close();
		
		return new FloatProcessor(width, height, out);
	}
	
	/**
	 * 
	 * @param input .dumm file (contains raw data 16-bit unsigned as generated by PSII measurements)
	 * @return 
	 * @throws IOException
	 */
	public static ImageProcessor dummToTif(File input) throws IOException {
		
		boolean use_little_endian = true;
		FileInputStream fis = new FileInputStream(input);
		
		// read header 16 bytes
		byte [] header = new byte[16];
		fis.read(header);

		//  create a byte buffer and wrap the array
		ByteBuffer bb = ByteBuffer.wrap(header);

		//  if the file uses little endian as apposed to network
		//  (big endian, Java's native) format,
		//  then set the byte order of the ByteBuffer
		if(use_little_endian)
		    bb.order(ByteOrder.LITTLE_ENDIAN);
				
		int width = bb.getInt();
		int height = bb.getInt();
		int bitsPerPixel = bb.getInt();
		int bytesPerPixel = bb.getInt();
		
		// System.out.println("width: " + width + " height: " + height + " Bits per pixel: " + bitsPerPixel + " Bytes per pixel: " + bytesPerPixel);

		float[] out = new float[width * height];
		
		// read into data array
		byte[] data = new byte[width * height * bytesPerPixel];
		fis.read(data);
		
		ByteBuffer bbuf = ByteBuffer.wrap(data);
		
		for(int idx = 0; idx < width * height; idx++) {
			// unsigned short
			byte b2 = bbuf.get();
			byte b1 = bbuf.get();
			int i = 0;
			i |= b1 & 0xFF;
			i <<= bitsPerPixel;
			i |= b2 & 0xFF;
			
			out[idx] = i;
		}

		fis.close();
		
		return new FloatProcessor(width, height, out);
	}
}
