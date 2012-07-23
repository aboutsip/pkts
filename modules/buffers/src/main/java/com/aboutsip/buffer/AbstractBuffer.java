/**
 * 
 */
package com.aboutsip.buffer;

import java.io.IOException;
import java.util.Arrays;


/**
 * @author jonas@jonasborjesson.com
 */
public abstract class AbstractBuffer implements Buffer {

    /**
     * From where we will continue reading
     */
    protected int readerIndex;

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
    protected AbstractBuffer(final int readerIndex, final int lowerBoundary, final int upperBoundary) {
        assert lowerBoundary <= upperBoundary;
        this.readerIndex = readerIndex;
        this.markedReaderIndex = readerIndex;
        this.lowerBoundary = lowerBoundary;
        this.upperBoundary = upperBoundary;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public int capacity() {
        return this.upperBoundary - this.lowerBoundary;
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
        return this.slice(getReaderIndex(), capacity());
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public int readableBytes() {
        return this.upperBoundary - this.readerIndex - this.lowerBoundary;
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
        final int start = getReaderIndex();
        while (hasReadableBytes()) {
            if (b == readByte()) {
                return slice(start, this.readerIndex - 1);
            }
        }

        throw new ByteNotFoundException(b);
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
                    return slice(start, (this.lowerBoundary + this.readerIndex) - 1);
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
        return readableBytes() >= length;
    }

    /**
     * Convenience method for checking if we can read at the index
     * 
     * @param index
     * @throws IndexOutOfBoundsException
     */
    protected void checkIndex(final int index) throws IndexOutOfBoundsException {
        if (index >= (this.lowerBoundary + capacity())) {
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

}
