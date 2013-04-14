/**
 * 
 */
package com.aboutsip.buffer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;


/**
 * @author jonas@jonasborjesson.com
 */
public abstract class AbstractBuffer implements Buffer {

    /**
     * From where we will continue reading
     */
    protected int readerIndex;

    /**
     * This is where we will write the next byte.
     */
    protected int writerIndex;

    /**
     * The position of the reader index that has been marked. I.e., this is the
     * position we will move the reader index back to if someone is asking us to
     * {@link #resetReaderIndex()}
     */
    protected int markedReaderIndex;

    /**
     * We will pretend that any bytes below this boundary doesn't exist.
     */
    protected int lowerBoundary;

    /**
     * Any bytes above this boundary is not accessible to us
     */
    protected int upperBoundary;

    // protected AbstractBuffer(final int readerIndex, final int lowerBoundary,
    // final int upperBoundary,
    // final byte[] buffer) {
    protected AbstractBuffer(final int readerIndex, final int lowerBoundary, final int upperBoundary,
            final int writerIndex) {
        assert lowerBoundary <= upperBoundary;
        this.readerIndex = readerIndex;
        this.markedReaderIndex = readerIndex;
        this.lowerBoundary = lowerBoundary;
        this.upperBoundary = upperBoundary;
        this.writerIndex = writerIndex;
    }

    @Override
    public abstract Buffer clone();

    /**
     * {@inheritDoc}
     */
    @Override
    public int getReaderIndex() {
        return this.readerIndex;
    }

    @Override
    public int getWriterIndex() {
        return this.writerIndex;
    }

    @Override
    public void setReaderIndex(final int index) {
        this.readerIndex = index;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int capacity() {
        return this.upperBoundary - this.lowerBoundary;
    }

    @Override
    public int getWritableBytes() {
        return this.upperBoundary - this.writerIndex;
    }

    @Override
    public boolean hasWritableBytes() {
        return getWritableBytes() > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void markReaderIndex() {
        this.markedReaderIndex = this.readerIndex;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer slice(final int stop) {
        return this.slice(getReaderIndex(), stop);
    }

    @Override
    public Buffer slice() {
        return this.slice(getReaderIndex(), getWriterIndex() - this.lowerBoundary);
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public int getReadableBytes() {
        return this.writerIndex - this.readerIndex - this.lowerBoundary;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resetReaderIndex() {
        this.readerIndex = this.markedReaderIndex;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer readUntil(final byte b) throws IOException, ByteNotFoundException {
        return this.readUntil(4096, b);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer readUntil(final int maxBytes, final byte... bytes) throws IOException, ByteNotFoundException,
    IllegalArgumentException {
        final int index = indexOf(maxBytes, bytes);
        if (index == -1) {
            throw new ByteNotFoundException(bytes);
        }

        final int size = index - getReaderIndex();
        Buffer result = null;
        if (size == 0) {
            result = Buffers.EMPTY_BUFFER;
        } else {
            result = readBytes(size);
        }
        readByte(); // consume the one at the index as well
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int indexOf(final byte b) throws IOException, ByteNotFoundException, IllegalArgumentException {
        return this.indexOf(4096, b);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int indexOf(final int maxBytes, final byte... bytes) throws IOException, ByteNotFoundException,
    IllegalArgumentException {
        if (bytes.length == 0) {
            throw new IllegalArgumentException("No bytes specified. Not sure what you want me to look for");
        }

        final int start = getReaderIndex();
        int index = -1;

        while (hasReadableBytes() && getReaderIndex() - start < maxBytes && index == -1) {
            if (isByteInArray(readByte(), bytes)) {
                index = this.readerIndex - 1;
            }
        }

        this.readerIndex = start;

        if (getReaderIndex() - start >= maxBytes) {
            throw new ByteNotFoundException(maxBytes, bytes);
        }

        return index;
    }

    private boolean isByteInArray(final byte b, final byte[] bytes) {
        for (final byte x : bytes) {
            if (x == b) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer readLine() throws IOException {
        final byte LF = '\n';
        final byte CR = '\r';

        final int start = this.readerIndex;
        boolean foundCR = false;
        while (hasReadableBytes()) {
            final byte b = readByte();
            switch (b) {
            case LF:
                return slice(start, this.readerIndex - (foundCR ? 2 : 1));
            case CR:
                foundCR = true;
                break;
            default:
                if (foundCR) {
                    --this.readerIndex;
                    return slice(start, this.lowerBoundary + this.readerIndex - 1);
                }
            }
        }

        // i guess there were nothing for us to read
        if (start >= this.readerIndex) {
            return null;
        }

        return slice(start, this.readerIndex);
    }

    /**
     * Convenience method for checking if we have enough readable bytes
     * 
     * @param length the length the user wishes to read
     * @throws IndexOutOfBoundsException in case we don't have the bytes
     *             available
     */
    protected void checkReadableBytes(final int length) throws IndexOutOfBoundsException {
        if (!checkReadableBytesSafe(length)) {
            throw new IndexOutOfBoundsException("Not enough readable bytes");
        }
    }

    /**
     * Convenience method for checking if we have enough readable bytes
     * 
     * @param length the length the user wishes to read
     * @return true if we have enough bytes available for read
     */
    protected boolean checkReadableBytesSafe(final int length) {
        return getReadableBytes() >= length;
    }

    /**
     * Convenience method for checking if we can read at the index
     * 
     * @param index
     * @throws IndexOutOfBoundsException
     */
    protected void checkIndex(final int index) throws IndexOutOfBoundsException {
        if (index >= this.lowerBoundary + capacity()) {
            //if (index >= this.lowerBoundary + this.writerIndex) {
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * Check whether we have enough space for writing the desired length.
     * 
     * @param length
     * @return
     */
    protected boolean checkWritableBytesSafe(final int length) {
        return getWritableBytes() >= length;
    }

    /**
     * Convenience method for checking whether we can write at the specified
     * index.
     * 
     * @param index
     * @throws IndexOutOfBoundsException
     */
    protected void checkWriterIndex(final int index) throws IndexOutOfBoundsException {
        if (index < this.writerIndex || index >= this.upperBoundary) {
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final short readUnsignedByte() throws IndexOutOfBoundsException, IOException {
        return (short) (readByte() & 0xFF);
    }

    /**
     * The underlying subclass should override this if it has write support.
     * {@inheritDoc}
     */
    @Override
    public boolean hasWriteSupport() {
        return false;
    }

    @Override
    public void write(final byte b) throws IndexOutOfBoundsException {
        throw new WriteNotSupportedException("This is an empty buffer. Cant write to it");
    }

    @Override
    public void write(final String s) throws IndexOutOfBoundsException, WriteNotSupportedException,
    UnsupportedEncodingException {
        throw new WriteNotSupportedException("This is an empty buffer. Cant write to it");
    }

    @Override
    public void write(final String s, final String charset) throws IndexOutOfBoundsException,
    WriteNotSupportedException, UnsupportedEncodingException {
        throw new WriteNotSupportedException("This is an empty buffer. Cant write to it");
    }

    @Override
    public abstract boolean equals(Object other);

    @Override
    public abstract int hashCode();


}
