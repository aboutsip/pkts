package com.aboutsip.buffer;

import java.io.IOException;
import java.io.InputStream;

/**
 * Yet another buffer class!
 * 
 * Kind of the same as the awesome classes in netty.io but currently the buffer
 * classes in netty are a little too tied into the rest of the netty stack.
 * Therefore, copy-pasting the necessary pieces needed for this project. Also,
 * since this entire project (i.e. yajpcap) is only for one particular use case,
 * we don't need all the flexibility of netty's buffers, even though somewhat
 * silly to re-invent the wheel.
 * 
 * @author jonas@jonasborjesson.com
 */
public interface Buffer extends Cloneable {

    /**
     * Read the requested number of bytes and increase the readerIndex with the
     * corresponding number of bytes. The new buffer and this buffer both share
     * the same backing array so changing either one of them will affect the
     * other.
     * 
     * @param length
     * @return
     * @throws IndexOutOfBoundsException
     * @throws IOException
     */
    Buffer readBytes(int length) throws IndexOutOfBoundsException, IOException;

    /**
     * Reads a line, i.e., it reads until we hit a line feed ('\n') or a
     * carriage return ('\r'), or a carriage return followed immediately by a
     * line feed.
     * 
     * @return a buffer containing the line but without the line terminating
     *         characters
     */
    Buffer readLine() throws IOException;

    /**
     * Returns the number of available bytes for reading without blocking. If
     * this returns less than what you want, there may still be more bytes
     * available depending on the underlying implementation. E.g., a Buffer
     * backed by an {@link InputStream} may be able to read more off the stream,
     * however, it may not be able to do so without blocking.
     * 
     * @return
     */
    int readableBytes();

    /**
     * Checks whether this buffer has any bytes available for reading without
     * blocking.
     * 
     * This is the same as <code>{@link #readableBytes} > 0</code>
     * 
     * @return
     */
    boolean hasReadableBytes();

    /**
     * Check whether this buffer is empty or not. This is the same as
     * <code>!{@link #hasReadableBytes()}</code>
     * 
     * @return
     */
    boolean isEmpty();

    /**
     * Get the backing array.
     * 
     * @return
     */
    byte[] getArray();

    /**
     * Get a slice of the buffer starting at <code>start</code> (inclusive)
     * ending at <code>stop</code> (exclusive). Hence, the new capacity of the
     * buffer is <code>stop - start</code>
     * 
     * @return
     */
    Buffer slice(int start, int stop);

    /**
     * Same as {@link #slice(Buffer.getReaderIndex(), int)}
     * 
     * @param stop
     * @return
     */
    Buffer slice(int stop);

    /**
     * Slice off the rest of the buffer. Same as {@link
     * #slice(Buffer.getReaderIndex(), buffer.getCapacity())}
     * 
     * @return
     */
    Buffer slice();

    /**
     * The the reader index
     * 
     * @return
     */
    int getReaderIndex();

    /**
     * Mark the current position of the reader index.
     * 
     * @see #reset()
     */
    void markReaderIndex();

    /**
     * Reset the reader index to the marked position or to the beginning of the
     * buffer if mark hasn't explicitly been called.
     */
    void resetReaderIndex();

    /**
     * The capacity of this buffer. The capacity is not affected by where the
     * reader index is etc
     * 
     * @return the capcity
     */
    int capacity();

    /**
     * Get the byte at the index.
     * 
     * Note, depending on the underlying implementing buffer, this method may
     * block and try and read the missing bytes off a stream. E.g., the
     * {@link InputStreamBuffer} gets its bytes off of a {@link InputStream} so
     * let's say you have read 10 bytes off of the stream already but ask to
     * access the byte at index 20, we will try and ready an additional 10 bytes
     * from the InputStream so we can return the byte at index 20.
     * 
     * @param index
     * @return the byte at the specified index
     * @throws IndexOutOfBoundsException in case the index is greater than the
     *             capacity of this buffer
     */
    byte getByte(int index) throws IndexOutOfBoundsException, IOException;

    /**
     * Read the next byte, which will also increase the readerIndex by one.
     * 
     * @return the next byte
     * @throws IndexOutOfBoundsException in case there is nothing left to read
     */
    byte readByte() throws IndexOutOfBoundsException, IOException;

    /**
     * Read an unsigned int and will increase the reader index of this buffer by
     * 4
     * 
     * @return a long representing the unsigned int
     * @throws IndexOutOfBoundsException in case there is not 4 bytes left to
     *             read
     */
    long readUnsignedInt() throws IndexOutOfBoundsException;

    /**
     * Read an int and will increase the reader index of this buffer by 4
     * 
     * @return the int value
     * @throws IndexOutOfBoundsException in case there is not 4 bytes left to
     *             read
     */
    int readInt() throws IndexOutOfBoundsException;

    /**
     * Get a 32-bit integer at the specified absolute index. This method will
     * not modify the readerIndex of this buffer.
     * 
     * @param index
     * @return
     * @throws IndexOutOfBoundsException in case there is not 4 bytes left to
     *             read
     */
    int getInt(int index) throws IndexOutOfBoundsException;

    short getShort(int index) throws IndexOutOfBoundsException;

    int readUnsignedShort() throws IndexOutOfBoundsException;


    int getUnsignedShort(int index) throws IndexOutOfBoundsException;

    short readShort() throws IndexOutOfBoundsException;

    short readUnsignedByte() throws IndexOutOfBoundsException;

    short getUnsignedByte(int index) throws IndexOutOfBoundsException;

    /**
     * Dump the content of this buffer as a hex dump ala Wireshark. Mainly for
     * debugging purposes
     * 
     * @return
     */
    String dumpAsHex();

    /**
     * Performs a deep clone of this object. I.e., the array that is backed by
     * this buffer will be copied and a new buffer will be returned. Hence, any
     * changes to the backing of this buffer will not affect the cloned buffer.
     * 
     * @return
     */
    Buffer clone();

    /**
     * Set the byte at given index to a new value
     * 
     * @param index the index
     * @param value the value
     * @throws IndexOutOfBoundsException
     */
    void setByte(int index, byte value) throws IndexOutOfBoundsException;

    @Override
    String toString();

}
