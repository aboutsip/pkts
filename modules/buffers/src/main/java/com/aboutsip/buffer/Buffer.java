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
     * Same as {@link #readUntil(4096, b)}
     * 
     * Read until the specified byte is encountered and return a buffer
     * representing that section of the buffer.
     * 
     * If the byte isn't found, then a {@link ByteNotFoundException} is thrown
     * and the {@link #getReaderIndex()} is left where we bailed out.
     * 
     * Note, the byte we are looking for will have been consumed so whatever
     * that is left in the {@link Buffer} will not contain that byte.
     * 
     * Example: <code>
     *    Buffer buffer = Buffers.wrap("hello world");
     *    Buffer hello = buffer.readUntil((byte)' ');
     *    System.out.println(hello);  // will contain "hello"
     *    System.out.println(buffer); // will contain "world"
     * </code>
     * 
     * As the example above illustrates, we are looking for a space, which is
     * found between "hello" and "world". Since the space will be consumed, the
     * original buffer will now only contain "world" and not " world".
     * 
     * @param b
     *            the byte to look for
     * @return a buffer containing the content from the initial reader index to
     *         the the position where the byte was found (exclusive the byte we
     *         are looking for)
     * 
     * @throws ByteNotFoundException
     *             in case the byte we were looking for is not found.
     */
    Buffer readUntil(byte b) throws IOException, ByteNotFoundException;

    /**
     * Read until any of the specified bytes have been encountered or until we
     * have read a maximum amount of bytes. This one works exactly the same as
     * {@link #readUntil(byte)} except it allows you to look for multiple bytes
     * and to specify for how many bytes we should be looking before we give up.
     * 
     * Example, we want to read until we either find
     * 
     * @param maxBytes
     *            the maximum number of bytes we would like to read before
     *            giving up.
     * @param bytes
     *            the bytes we are looking for (either one of them)
     * @return a buffer containing the content from the initial reader index to
     *         the the position where the byte was found (exclusive the byte we
     *         are looking for)
     * @throws IOException
     * @throws ByteNotFoundException
     *             in case none of the bytes we were looking for are found
     *             within the specified maximum number of bytes.
     * @throws IllegalArgumentException
     *             in no bytes to look for is specified.
     */
    Buffer readUntil(int maxBytes, byte... bytes) throws IOException, ByteNotFoundException, IllegalArgumentException;

    /**
     * Same as {@link #readUntil(int, byte...)} but instead of returning the
     * buffer with everything up until the specified byte it returns the index
     * instead.
     * 
     * NOTE. The index is representing where in the {@link Buffer} you can find
     * the byte and the index is in relation to the entire {@link Buffer} and
     * its capacity so even if you have already read x bytes, it would not
     * change the index of what you search for.
     * 
     * Example:
     * 
     * 
     * @param maxBytes
     *            the maximum number of bytes we would like to read before
     *            giving up.
     * @param bytes
     *            the bytes we are looking for (either one of them)
     * @return the index of the found byte or -1 (negative one) if we couldn't
     *         find it.
     * @throws IOException
     * @throws ByteNotFoundException
     *             will ONLY be thrown if we haven't found the byte within the
     *             maxBytes limit. If the buffer we are searching in is less
     *             than maxBytes and we can't find what we are looking for then
     *             negative one will be returned instead.
     */
    int indexOf(int maxBytes, byte... bytes) throws IOException, ByteNotFoundException, IllegalArgumentException;

    /**
     * 
     * @param b
     * @return
     * @throws IOException
     * @throws ByteNotFoundException
     * @throws IllegalArgumentException
     */
    int indexOf(byte b) throws IOException, ByteNotFoundException, IllegalArgumentException;

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
     * Note, if you slice an empty buffer you will get back another empty
     * buffer. Same goes for when you slice a buffer whose bytes already have
     * been consumed.
     * 
     * @return
     */
    Buffer slice();

    /**
     * The reader index
     * 
     * @return
     */
    int getReaderIndex();

    void setReaderIndex(int index);

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
     * @return the capacity
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
     * Peak a head to see what the next byte is. This method will not change the
     * readerIndex
     * 
     * @return the next byte
     * @throws IndexOutOfBoundsException
     *             in case there is nothing left to read
     */
    byte peekByte() throws IndexOutOfBoundsException, IOException;

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

    short readUnsignedByte() throws IndexOutOfBoundsException, IOException;

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

    /**
     * Check whether to buffers are considered to be equal.
     * 
     * To buffers are equal if the underlying visible area of the byte array are
     * equal.
     * 
     * @param b
     * @return
     */
    @Override
    boolean equals(Object b);

    @Override
    int hashCode();

    @Override
    String toString();
}
