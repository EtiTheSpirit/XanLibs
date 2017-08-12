package xandragon.datalib;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.io.DataInputStream;
import java.io.EOFException;

/**
 * A rewritten implementation of a DataInputStream that allows for seeking through what you've already read, and freely skipping through the DataInputStream attached.
 * @author Xan the Dragon
 */
public class SeekInputStream {
	
	/**The current position of the stream*/
	protected int position = 0;
	
	/**The current stream.*/
	protected DataInputStream stream;
	
	/**The current cache of read values*/
	protected ArrayList<Integer> readValues = new ArrayList<Integer>();
	
	/**
	 * Construct a new SeekInputStream from a DataInputStream
	 * @param in A DataInputStream to read from.
	 */
	public SeekInputStream(DataInputStream in) {
		stream = in;
	}
	
	/**
	 * Reads the next value relative to the current set position in the stream.
	 * @returns The value read, or -1 if the end of file is met;
	 */
	public int read() throws IOException {
		if (position == readValues.size()) {
			//If the position is equal to the size of the list, that means we're at the end and need to read a new value.
			int readValue = stream.read();
			if (readValue != -1) {
				//Not EOF
				readValues.add(readValue);
				position++;
			}
			return readValue;
		} else if (position < readValues.size()) {
			//Otherwise we need to read from something we already got.
			int readValue = readValues.get(position).intValue();
			position++;
			return readValue;
		}
		return -1;
	}
	
	/**
	 * Read the next amount of bytes, but only move forward by one byte. Useful for telling what bytes might be ahead without skipping anything.
	 * @param bytes The amount of bytes to read ahead by
	 * @return A list of the read values
	 */
	public int[] readAhead(int bytes) throws IOException, EOFException {
		int[] values = new int[bytes];
		int actuallyMovedBy = 0;
		for (int i = 0; i < bytes; i++) {
			int readValue = read();
			values[i] = readValue;
			if (readValue != -1) {
				actuallyMovedBy++;
			} else {
				throw new EOFException();
			}
		}
		skipBackwards(actuallyMovedBy-1); //Subtract 1 so it still actually goes forward.
		return values;
	}
	
	/**
	 * Read a byte value.
	 * @returns The value read.
	 */
	public byte readByte() throws IOException {
		return (byte) read();
	}
	
	
	/**
	 * Read a short value.
	 * @return The short value read.
	 */
	public short readShort() throws IOException {
		return ByteBuffer.wrap(getBytes(2)).getShort();
	}
	
	/**
	 * Read an integer value.
	 * @return The integer value read.
	 */
	public int readInt() throws IOException {
		return ByteBuffer.wrap(getBytes(4)).getInt();
	}
	
	/**
	 * Read a long value.
	 * @return The long value read.
	 */
	public long readLong() throws IOException {
		return ByteBuffer.wrap(getBytes(8)).getLong();
	}
	
	/**
	 * Read a float value.
	 * @return The float value read.
	 */
	public float readFloat() throws IOException {
		return ByteBuffer.wrap(getBytes(4)).getFloat();
	}
	
	/**
	 * Read a double value.
	 * @return The double value read.
	 */
	public double readDouble() throws IOException {
		return ByteBuffer.wrap(getBytes(4)).getFloat();
	}
	
	/**
	 * Skips ahead by a specified length of bytes.<br>
	 * @param n The amount of bytes to skip by.
	 * @return Returns -1 if seeking failed. Will otherwise return how many bytes were skipped.
	 */
	public long skip(int n) throws IOException {
		for (int i = 0; i < n; i++) {
			int v = read();
			if (v == -1) {
				return -1;
			}
		}
		return n;
	}
	
	/** 
	 * Seeks the stream backwards by forcing it to read cached data that has already been read.
	 * @param amount The amount of bytes to back by.
	 * @return The amount of bytes moved backwards, or -1 if moving by that amount of bytes was not possible (due to going before the start of the file)
	 */
	public int skipBackwards(int amount) {
		if (amount > position) {
			return -1;
		}
		position-= amount; //Just change the position value. See the read function for why.
		return amount;
	}
	
	/**
	 * Seek to a specific point in the stream.<br>
	 * <strong>This will forcefully read from the stream when attempting to seek past a point that has already been read, so beware skipping ahead long distances in large files!</strong><br>
	 * @param index The byte to go to.
	 * @returns -1 if seeking failed. Will otherwise return the index that the stream is currently located at.
	 */
	public int seek(int index) throws IOException {
		if (index > readValues.size()) {
			//Trying to read past what we can.
			position = readValues.size(); //Push the position to the end of what we've read.
			for (int i = position; i < index; i++) {
				int v = read(); //Continue reading until we hit our goal.
				if (v == -1) {
					position = i; //Set the position value to the end of the stream.
					return -1;
				}
			}
		}
		position = index; //This is common between if we read past what we have already read and if we're just reading normally.
		return index;
	}
	
	/**
	 * @return The current location in the stream.
	 */
	public int at() {
		return position;
	}
	
	/**
	 * Rewind the stream back to its start
	 */
	public void rewind() {
		position = 0;
	}
	
	/**
	 * Internal function to read an amount of byte values and return a byte array.
	 * @param amount The amount of bytes to read.
	 * @return The byte array
	 * @throws IOException
	 */
	protected byte[] getBytes(int amount) throws IOException {
		byte[] b = new byte[amount];
		for (int i = 0; i < amount; i++) {
			b[i] = readByte();
		}
		return b;
	}
}
