package io.pkts.buffer;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * @author jonas@jonasborjesson.com
 */
public class BoundedInputStreamBuffer extends BaseBuffer {

    private static final String CANNOT_WRITE_TO_AN_INPUT_STREAM_BUFFER = "Cannot write to an InputStreamBuffer";
    private static final String NOT_IMPLEMENTED_JUST_YET = "Not implemented just yet";

    private final InputStream is;

    /**
     * The default capacity for each individual byte array, default to tcpdumps default snaplength.
     */
    public static final int DEFAULT_CAPACITY = 262144;

    /**
     * From where we will continue reading
     */
    private long readerIndex;

    /**
     * This is where we will write the next byte.
     */
    private long writerIndex;

    final byte[] buffer;

    final int localCapacity;

    public BoundedInputStreamBuffer(final InputStream is) {
        this(DEFAULT_CAPACITY, is);
    }

    /**
     *
     * @param bufferCapacity To be sure that the PCAP framer works, this needs to be same or larger than SNAPLENGTH used to get PCAP.
     * Default SNAPLENGTH of tcpdump is 262144. If using '-s NNN' parameter of tcpdump, you should also be able
     * to reduce this to a lower value for less memory usage.

     * @param is
     */
    public BoundedInputStreamBuffer(final int bufferCapacity, final InputStream is) {
        assert is != null;
        this.is = is;
        this.localCapacity = bufferCapacity;
        buffer = new byte[bufferCapacity];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer slice(final int start, final int stop) {
        throw new RuntimeException(NOT_IMPLEMENTED_JUST_YET);

//        final long length = stop - start;
//        if (length > localCapacity) {
//            throw new IllegalArgumentException("Slice is too big: " + length + ", must be less or equals to " + localCapacity);
//        }
//        checkIndex(this.lowerBoundary + start);
//        checkIndex(this.lowerBoundary + stop - 1);
//
//        final int startPos = (int) (start % this.localCapacity);
//        final int stopPos = (int) (stop % this.localCapacity);
//
//        if (startPos <= stopPos) {
//            // All contained linearly in current buffer. REUSE buffer as-is.
//            return new ByteBuffer(0, startPos, stopPos, this.buffer);
//        } else {
//            // Data is 'wrapped around' the buffer end, need to create a new buffer that ByteBuffer understands
//            final byte[] resultBuffer = new byte[(int)length];
//            final int firstCopyLength = localCapacity - startPos;
//            System.arraycopy(this.buffer, startPos, resultBuffer, 0, firstCopyLength);
//            System.arraycopy(this.buffer, 0, resultBuffer, firstCopyLength, stopPos );
//            return new ByteBuffer(resultBuffer);
//        }
    }

    private static int assertSafeInt(final long value) {
        if (value > Integer.MAX_VALUE || value < Integer.MIN_VALUE) {
            throw new IllegalStateException("pkts only supports this operation with files/streams less than 2gb. Value=" + value);
        }

        return (int) value;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IOException
     */
    @Override
    public byte readByte() throws IndexOutOfBoundsException, IOException {
        final int read = internalReadBytes(1);
        if (read == -1) {
            // not sure this is really the right thing to do
            throw new IndexOutOfBoundsException();
        }
        return getByte(this.readerIndex++);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte peekByte() throws IndexOutOfBoundsException, IOException {
        final int read = internalReadBytes(1);
        if (read == -1) {
            // not sure this is really the right thing to do
            throw new IndexOutOfBoundsException();
        }
        return getByte(this.readerIndex);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IOException
     */
    @Override
    public Buffer readBytes(final int length) throws IndexOutOfBoundsException, IOException {
        if (!checkReadableBytesSafe(length)) {
            final int availableBytes = getReadableBytes();
            final int read = internalReadBytes(length);
            if (read == -1) {
                // end-of-file
                return null;
            } else if (read + availableBytes < length) {
                // do something else?
                throw new IndexOutOfBoundsException("Not enough bytes left in the stream. Wanted " + length
                        + " but only read " + read);
            }
        }

        // perhaps we should create a composite buffer instead of this
        // copying???
        int index = 0;
        final byte[] buf = new byte[length];
        while (index < length) {
            final int spaceLeft = getAvailableLocalReadingSpace();
            final int readAtMost = Math.min(length - index, spaceLeft);
            final int localIndex = getLocalReaderIndex();

            System.arraycopy(this.buffer, localIndex, buf, index, readAtMost);
            this.readerIndex += readAtMost;
            index += readAtMost;
        }
        return Buffers.wrap(buf);

    }

    /**
     * Ensure that <code>length</code> more bytes are available in the internal
     * buffer. Read more bytes from the underlying stream if needed. This
     * method is blocking in case we don't have enough bytes to read.
     *
     * @param length the amount of bytes we wishes to read
     * @return the actual number of bytes read.
     * @throws IOException
     */
    private int internalReadBytes(final int length) throws IOException {

        if (length > localCapacity) {
            throw new IllegalArgumentException("Length is larger than buffer. Request=" + length + ", capacity=" + localCapacity);
        }

        // check if we already have enough bytes available for reading
        // and if so, just return the length the user is asking for
        if (checkReadableBytesSafe(length)) {
            return length;
        }

        return readFromStream(length - getReadableBytes());

    }

    /**
     * Since the writer index (upper boundary) is where we are to write for the
     * entire buffer we need to translate this into the local index into the
     * array we currently are working with.
     *
     * @return
     */
    private int getLocalWriterIndex() {
        return (int) (this.writerIndex % this.localCapacity);
    }

    /**
     * Translates the global reader index into the local index within a row
     *
     * @return
     */
    private int getLocalReaderIndex() {
        return (int) (this.readerIndex % this.localCapacity);
    }

    /**
     * Since the underlying storage for this buffer is essentially a 2-D byte
     * array we sometimes need to find out how much capacity is left in a
     * particular row.
     *
     * @return
     */
    private int getAvailableLocalWritingSpace() {
        return this.localCapacity - getLocalWriterIndex();
    }

    /**
     * Find out how many bytes are left to read in the current row
     *
     * @return
     */
    private int getAvailableLocalReadingSpace() {
        return this.localCapacity - getLocalReaderIndex();
    }

    /**
     * Method for reading bytes off the stream and store it in the local
     * "storage"
     *
     * @param length
     *            the length we wish to read
     * @return the actual amount of bytes we read
     * @throws IOException
     *             in case anything goes wrong while reading
     */
    private int readFromStream(final long length) throws IOException {
        if (getReadableBytes() + length > localCapacity ) {
            throw new IllegalArgumentException("Trying to read too far ahead, will cause wrap-around issues: " + length);
        }
        int total = 0;
        int actual = 0;
        while (total < length && actual != -1) {

            final int localIndex = getLocalWriterIndex();
            final int spaceLeft = getAvailableLocalWritingSpace();
            final int readAtMost = (int)Math.min(length - total, spaceLeft);

            actual = this.is.read(this.buffer, localIndex, readAtMost);

            if (actual > 0) {
                this.writerIndex += actual;
                total += actual;
            }
        }
        return total;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getReadableBytes() {
        return assertSafeInt(this.writerIndex - this.readerIndex);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IOException
     * @throws IndexOutOfBoundsException
     */
    @Override
    public boolean hasReadableBytes() {
        if (!checkReadableBytesSafe(1)) {
            try {
                // if we don't have any bytes available for reading
                // then try and read a bunch at the same time. However,
                // we are satisfied if we can only read one byte
                return internalReadBytes(100) >= 1;
            } catch (final IOException e) {
                return false;
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        throw new RuntimeException(NOT_IMPLEMENTED_JUST_YET);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getArray() {
        throw new RuntimeException(NOT_IMPLEMENTED_JUST_YET);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte getByte(final int index) throws IndexOutOfBoundsException, IOException {
        return getByte((long) index);
    }
    public byte getByte(final long index) throws IndexOutOfBoundsException, IOException {
        checkIndex(index);
        return this.buffer[(int)(index % localCapacity)];
    }
    /**
     * Convenience method for checking if we can get the byte at the specified
     * index. If we can't, then we will try and read the missing bytes off of
     * the underlying {@link InputStream}. If that fails, e.g. we don't ready
     * enough bytes off of the stream, then we will eventually throw an
     * {@link IndexOutOfBoundsException}
     *
     * @param index
     *            the actual index to check. I.e., this is the actual index in
     *            our byte array, irrespective of what the lowerBoundary is set
     *            to.
     * @throws IndexOutOfBoundsException
     * @throws IOException
     */
    private void checkIndex(final long index) throws IndexOutOfBoundsException {
        final long missingBytes = index + 1 - this.writerIndex;
        if (missingBytes <= 0) {
            // we got all the bytes needed
            return;
        }

        try {
            final int read = readFromStream(missingBytes);
            if (read == -1 || read < missingBytes) {
                throw new IndexOutOfBoundsException();
            }
        } catch (final IOException e) {
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long readUnsignedInt() throws IndexOutOfBoundsException {
        throw new RuntimeException(NOT_IMPLEMENTED_JUST_YET);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int readInt() throws IndexOutOfBoundsException {
        throw new RuntimeException(NOT_IMPLEMENTED_JUST_YET);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getInt(final int index) throws IndexOutOfBoundsException {
        throw new RuntimeException(NOT_IMPLEMENTED_JUST_YET);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public short getShort(final int index) throws IndexOutOfBoundsException {
        throw new RuntimeException(NOT_IMPLEMENTED_JUST_YET);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int readUnsignedShort() throws IndexOutOfBoundsException {
        throw new RuntimeException(NOT_IMPLEMENTED_JUST_YET);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getUnsignedShort(final int index) throws IndexOutOfBoundsException {
        throw new RuntimeException(NOT_IMPLEMENTED_JUST_YET);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public short readShort() throws IndexOutOfBoundsException {
        throw new RuntimeException(NOT_IMPLEMENTED_JUST_YET);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getUnsignedInt(final int index) throws IndexOutOfBoundsException {
        throw new RuntimeException(NOT_IMPLEMENTED_JUST_YET);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public short getUnsignedByte(final int index) throws IndexOutOfBoundsException {
        throw new RuntimeException(NOT_IMPLEMENTED_JUST_YET);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String dumpAsHex() {
        throw new RuntimeException(NOT_IMPLEMENTED_JUST_YET);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setByte(final int index, final byte value) throws IndexOutOfBoundsException {
        throw new RuntimeException(NOT_IMPLEMENTED_JUST_YET);
    }

    @Override
    public void setUnsignedByte(final int index, final short value) throws IndexOutOfBoundsException {
        throw new RuntimeException(NOT_IMPLEMENTED_JUST_YET);
    }

    @Override
    public void setUnsignedShort(final int index, final int value) throws IndexOutOfBoundsException {
        throw new RuntimeException(NOT_IMPLEMENTED_JUST_YET);
    }

    @Override
    public Buffer clone() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        if (other == null) {
            return false;
        }

        if (this == other) {
            return true;
        }

        try {
            final Buffer b = (Buffer) other;

            // should we care about how far we may have read into
            // the two buffers? For now we will...
            // Also, we may want to implement our own array compare
            // since now the two arrays will be copied, which is kind
            // of stupid but for now that is ok. Will worry about potential
            // bottlenecks later. Issue has been added to the tracker to keep
            // track of this...
            return Arrays.equals(getArray(), b.getArray());
        } catch (final ClassCastException e) {
            return false;
        }
    }

    @Override
    public boolean equalsIgnoreCase(final Object other) {
        throw new RuntimeException("Sorry, InputStreamBuffer.equalsIgnoreCase isn't implemented yet");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        // TODO: actually go over the array instead of copying like this
        return Arrays.hashCode(getArray());
    }

    @Override
    public String toString() {
        // perhaps not the most efficient way? but it works
        // so for now we'll leave it as this until proven
        // slow
        final Buffer b = this.slice();
        return b.toString();
    }

    @Override
    public int getWritableBytes() {
        return 0;
    }

    @Override
    public boolean hasWritableBytes() {
        return false;
    }

    @Override
    public void write(final byte[] bytes) throws IndexOutOfBoundsException, WriteNotSupportedException {
        throw new WriteNotSupportedException("Cannot write to an InputStreamBuffer");
    }

    @Override
    public void getBytes(final Buffer dst) {
        throw new RuntimeException(NOT_IMPLEMENTED_JUST_YET);
    }

    @Override
    public void getBytes(final byte[] dst) throws IndexOutOfBoundsException {
        throw new RuntimeException(NOT_IMPLEMENTED_JUST_YET);
    }

    @Override
    public void getBytes(final int index, final Buffer dst) throws IndexOutOfBoundsException {
        throw new RuntimeException(NOT_IMPLEMENTED_JUST_YET);
    }

    @Override
    public void setInt(final int index, final int value) throws IndexOutOfBoundsException {
        throw new RuntimeException(NOT_IMPLEMENTED_JUST_YET);
    }

    @Override
    public void write(final int value) throws IndexOutOfBoundsException, WriteNotSupportedException {
        throw new WriteNotSupportedException(CANNOT_WRITE_TO_AN_INPUT_STREAM_BUFFER);
    }

    @Override
    public void write(final long value) throws IndexOutOfBoundsException, WriteNotSupportedException {
        throw new WriteNotSupportedException(CANNOT_WRITE_TO_AN_INPUT_STREAM_BUFFER);
    }

    @Override
    public void writeAsString(final int value) throws IndexOutOfBoundsException, WriteNotSupportedException {
        throw new WriteNotSupportedException(CANNOT_WRITE_TO_AN_INPUT_STREAM_BUFFER);
    }

    @Override
    public void writeAsString(final long value) throws IndexOutOfBoundsException, WriteNotSupportedException {
        throw new WriteNotSupportedException(CANNOT_WRITE_TO_AN_INPUT_STREAM_BUFFER);
    }

    @Override
    public void setUnsignedInt(final int index, final long value) throws IndexOutOfBoundsException {
        throw new WriteNotSupportedException(NOT_IMPLEMENTED_JUST_YET);
    }

    @Override
    public void write(final byte b) throws IndexOutOfBoundsException {
        throw new WriteNotSupportedException(NOT_IMPLEMENTED_JUST_YET);
    }

    @Override
    public void write(final String s) throws IndexOutOfBoundsException, WriteNotSupportedException,
        UnsupportedEncodingException {
        throw new WriteNotSupportedException(NOT_IMPLEMENTED_JUST_YET);
    }

    @Override
    public void write(final String s, final String charset) throws IndexOutOfBoundsException,
        WriteNotSupportedException, UnsupportedEncodingException {
        throw new WriteNotSupportedException(NOT_IMPLEMENTED_JUST_YET);
    }

    @Override
    public boolean hasWriteSupport() {
        return false;
    }

    @Override
    public void setWriterIndex(final int index) {
        throw new RuntimeException(NOT_IMPLEMENTED_JUST_YET);
    }

    @Override
    public void setReaderIndex(final int index) {
        throw new RuntimeException(NOT_IMPLEMENTED_JUST_YET);
    }

    @Override
    public int getReaderIndex() {
        throw new RuntimeException(NOT_IMPLEMENTED_JUST_YET);
    }

    @Override
    public int getWriterIndex() {
        throw new RuntimeException(NOT_IMPLEMENTED_JUST_YET);
    }

    @Override
    public final short readUnsignedByte() throws IndexOutOfBoundsException, IOException {
        return (short) (readByte() & 0xFF);
    }

    @Override
    public int capacity() {
        throw new RuntimeException(NOT_IMPLEMENTED_JUST_YET);
    }

    @Override
    public void resetReaderIndex() {
        throw new RuntimeException(NOT_IMPLEMENTED_JUST_YET);
    }

    @Override
    public void markReaderIndex() {
        throw new RuntimeException(NOT_IMPLEMENTED_JUST_YET);
    }

    @Override
    public int getLowerBoundary() {
        throw new RuntimeException(NOT_IMPLEMENTED_JUST_YET);
    }

    @Override
    public int getUpperBoundary() {
        throw new RuntimeException(NOT_IMPLEMENTED_JUST_YET);
    }

    @Override
    public Buffer slice(final int stop) {
        throw new RuntimeException(NOT_IMPLEMENTED_JUST_YET);
    }

    @Override
    public Buffer slice() {
        throw new RuntimeException(NOT_IMPLEMENTED_JUST_YET);
    }

}
