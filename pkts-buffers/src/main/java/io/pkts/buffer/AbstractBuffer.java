/**
 * 
 */
package io.pkts.buffer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * @author jonas@jonasborjesson.com
 */
public abstract class AbstractBuffer extends BaseBuffer {

    private static final String FOR_INPUT_STRING = "For input string: \"";
    private static final String EMPTY_BUFFER_CANT_WRITE = "This is an empty buffer. Cant write to it";

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

    @Override
    public int getLowerBoundary() {
        return this.lowerBoundary;
    }

    @Override
    public int getUpperBoundary() {
        return this.upperBoundary;
    }

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
    public void setWriterIndex(final int index) {
        this.writerIndex = index;
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
        if (!hasReadableBytes()) {
            return Buffers.EMPTY_BUFFER;
        }
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
        throw new WriteNotSupportedException(EMPTY_BUFFER_CANT_WRITE);
    }

    @Override
    public void write(final String s) throws IndexOutOfBoundsException, WriteNotSupportedException,
            UnsupportedEncodingException {
        throw new WriteNotSupportedException(EMPTY_BUFFER_CANT_WRITE);
    }

    @Override
    public void write(final String s, final String charset) throws IndexOutOfBoundsException,
            WriteNotSupportedException, UnsupportedEncodingException {
        throw new WriteNotSupportedException(EMPTY_BUFFER_CANT_WRITE);
    }

    @Override
    public abstract boolean equals(Object other);

    @Override
    public abstract int hashCode();

}
