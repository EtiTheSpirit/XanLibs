package xandragon.datalib;

import java.io.IOException;
import java.util.ArrayList;
import java.io.DataInputStream;

/**
 * A rewritten implementation of a DataInputStream that allows for seeking through what you've already read.<br>
 * <br>
 * This was written for personal usage in instances when I needed to rewind, especially in an instance where I'm skipping a set of specific characters.<br>
 * In order to check if the character is one of those specific ones, I need to call read() which causes it to move ahead. In the instance that it's NOT<br>
 * one of those specific characters, this can cause issues because I've just cut off the first bit of what I may need to read.<br>
 * <br>
 * Unfortunately, this only does its magic with the read(). You can wrap your own version of things like readFloat() and such with a {@link java.nio.ByteBuffer}
 * 
 * @author Xan the Dragon
 */
public class SeekInputStream extends DataInputStream {
	
	/**The current position of the stream*/
	protected int position = 0;
	
	/**The current cache of read values*/
	protected ArrayList<Integer> readValues = new ArrayList<Integer>();
	
	/**
	 * Construct a new SeekInputStream
	 * @param in A DataInputStream to read from.
	 */
	public SeekInputStream(DataInputStream in) {
		super(in);
	}
	
	/**
	 * Reads the next value relative to the current set position in the stream.
	 * @returns The value read, or -1 if the end of file is met;
	 */
	@Override
	public int read() throws IOException {
		if (position == readValues.size()) {
			//If the position is equal to the size of the list, that means we're at the end and need to read a new value.
			int readValue = super.read();
			if (readValue != -1) {
				//Not EOF
				readValues.add(readValue);
				position++;
			}
			return readValue;
		} else {
			//Otherwise we need to read from something we already got.
			int readValue = readValues.get(position).intValue();
			position++;
			return readValue;
		}
	}
	
	/**
	 * Skips ahead by a specified length of bytes.<br>
	 * <strong>Use this instead of skipBytes, as skipBytes will not register the amount of bytes that have been skipped and will break the reader.</strong>
	 * @param n The amount of bytes to skip by.
	 * @return Returns -1 if seeking failed. Will otherwise return how many bytes were skipped.
	 */
	@Override
	public long skip(long n) throws IOException {
		//Here, I can't use the normal skip method. I will actually have to seek.
		int returnVal = seek((int) (position + n));
		return returnVal == -1 ? returnVal : n;
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
	 * <strong>This will forcefully read from the stream when attempting to seek past a point that has already been read.</strong>
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
}
