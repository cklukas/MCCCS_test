package bsqloader;

import java.io.IOException;

public interface FileReaderUtil {
	void close() throws IOException;
	
	long remaining() throws IOException;
	
	long position() throws IOException;
	
	byte readByte() throws IOException;
	
	byte readByte(int position) throws IOException;
	
	short readShort() throws IOException;
	
	short readShort(int position) throws IOException;
	
	int readInt(int position) throws IOException;
	
	int readInt() throws IOException;
	
	float readFloat(int position) throws IOException;
	
	float readFloat() throws IOException;
	
	double readDouble() throws IOException;
	
	double readDouble(int position) throws IOException;
	
	void position(int position) throws IOException;
}
