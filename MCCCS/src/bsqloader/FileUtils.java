package bsqloader;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileUtils {
	
	public static FileReaderUtil getFileReaderUtil(File file) throws IOException {
		
		FileReaderUtil fileReader = null;
		FileInputStream stream = null;
		FileChannel channel = null;
		
		try {
			stream = new FileInputStream(file);
			
			channel = stream.getChannel();
			ByteBuffer fileBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
			fileBuffer.order(LITTLE_ENDIAN);
			fileReader = new MappedFileChannelReaderUtil(stream, channel, fileBuffer);
			
		} catch (FileNotFoundException e) {
			throw e;
		}
		
		return fileReader;
	}
	
}
