package bsqloader;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class MappedFileChannelReaderUtil implements FileReaderUtil {
	
	ByteBuffer byteBuffer = null;
	ByteBuffer shortBuffer = null;
	
	ByteBuffer intBuffer = null;
	
	ByteBuffer floatBuffer = null;
	
	ByteBuffer doubleBuffer = null;
	ByteBuffer fileBuffer = null;
	FileInputStream stream;
	FileChannel channel;
	
	public MappedFileChannelReaderUtil(FileInputStream stream, FileChannel channel, ByteBuffer fileBuffer) {
		this.stream = stream;
		this.channel = channel;
		byteBuffer = ByteBuffer.allocate(1);
		byteBuffer.order(LITTLE_ENDIAN);
		shortBuffer = ByteBuffer.allocate(2);
		shortBuffer.order(LITTLE_ENDIAN);
		intBuffer = ByteBuffer.allocate(4);
		intBuffer.order(LITTLE_ENDIAN);
		floatBuffer = ByteBuffer.allocate(4);
		floatBuffer.order(LITTLE_ENDIAN);
		doubleBuffer = ByteBuffer.allocate(8);
		doubleBuffer.order(LITTLE_ENDIAN);
		this.fileBuffer = fileBuffer;
	}
	
	@Override
	public byte readByte() throws IOException {
		return fileBuffer.get();
	}
	
	@Override
	public byte readByte(int position) throws IOException {
		fileBuffer.position(position);
		return readByte();
	}
	
	@Override
	public double readDouble() throws IOException {
		return fileBuffer.getDouble();
	}
	
	@Override
	public double readDouble(int position) throws IOException {
		fileBuffer.position(position);
		return readDouble();
	}
	
	@Override
	public float readFloat(int position) throws IOException {
		fileBuffer.position(position);
		return readFloat();
	}
	
	@Override
	public float readFloat() throws IOException {
		return fileBuffer.getFloat();
	}
	
	@Override
	public int readInt(int position) throws IOException {
		fileBuffer.position(position);
		return readInt();
	}
	
	@Override
	public int readInt() throws IOException {
		return fileBuffer.getInt();
	}
	
	@Override
	public short readShort() throws IOException {
		return fileBuffer.getShort();
	}
	
	@Override
	public short readShort(int position) throws IOException {
		fileBuffer.position(position);
		return readShort();
	}
	
	@Override
	public void position(int position) throws IOException {
		fileBuffer.position(position);
		
	}
	
	@Override
	public void close() throws IOException {
		channel.close();
		stream.close();
	}
	
	@Override
	public long position() throws IOException {
		
		return fileBuffer.position();
	}
	
	@Override
	public long remaining() throws IOException {
		return fileBuffer.remaining();
	}
	
}