package io.pkts.buffer;

import java.io.UnsupportedEncodingException;

public final class ReadOnlyByteBuffer extends ByteBuffer implements ReadOnlyBuffer {

    protected ReadOnlyByteBuffer(final byte[] buffer) {
        this(0, 0, buffer.length, buffer);
    }

    protected ReadOnlyByteBuffer(final int readerIndex, final int lowerBoundary, final int upperBoundary, final byte[] buffer) {
        this(readerIndex, lowerBoundary, upperBoundary, upperBoundary, buffer);
    }

    protected ReadOnlyByteBuffer(final int readerIndex, final int lowerBoundary, final int upperBoundary,
                                 final int writerIndex, final byte[] buffer) {
        super(readerIndex, lowerBoundary, upperBoundary, writerIndex, buffer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer clone() {
        // because you cannot write to this buffer, we'll just use the same underlying buffer.
        return new ReadOnlyByteBuffer(readerIndex, lowerBoundary, upperBoundary, writerIndex, buffer);
    }

    @Override
    protected Buffer createBuffer(final int readerIndex, final int lowerBoundary, final int upperBoundary,
                                  final int writerIndex, final byte[] buffer) {
        return new ReadOnlyByteBuffer(0, lowerBoundary, upperBoundary, writerIndex, buffer);
    }

    @Override
    public boolean hasWriteSupport() {
        return false;
    }

    @Override
    public void setWriterIndex(final int index) {
        throw new WriteNotSupportedException();
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
    public void write(final byte b) throws IndexOutOfBoundsException, WriteNotSupportedException {
        throw new WriteNotSupportedException();
    }

    @Override
    public void write(final byte[] bytes) throws IndexOutOfBoundsException, WriteNotSupportedException {
        throw new WriteNotSupportedException();
    }

    @Override
    public void write(final int value) throws IndexOutOfBoundsException, WriteNotSupportedException {
        throw new WriteNotSupportedException();
    }

    @Override
    public void write(final long value) throws IndexOutOfBoundsException, WriteNotSupportedException {
        throw new WriteNotSupportedException();
    }

    @Override
    public void write(final String s) throws IndexOutOfBoundsException, WriteNotSupportedException, UnsupportedEncodingException {
        throw new WriteNotSupportedException();
    }

    @Override
    public void writeAsString(final int value) throws IndexOutOfBoundsException, WriteNotSupportedException {
        throw new WriteNotSupportedException();
    }

    @Override
    public void writeAsString(final long value) throws IndexOutOfBoundsException, WriteNotSupportedException {
        throw new WriteNotSupportedException();
    }

    @Override
    public void write(final String s, final String charset) throws IndexOutOfBoundsException, WriteNotSupportedException,
            UnsupportedEncodingException {
        throw new WriteNotSupportedException();
    }

    @Override
    public void setInt(final int index, final int value) throws IndexOutOfBoundsException {
        throw new WriteNotSupportedException();
    }

    @Override
    public void setUnsignedInt(final int index, final long value) throws IndexOutOfBoundsException {
        throw new WriteNotSupportedException();
    }

    @Override
    public void setUnsignedShort(final int index, final int value) throws IndexOutOfBoundsException {
        throw new WriteNotSupportedException();
    }

    @Override
    public void setUnsignedByte(final int index, final short value) throws IndexOutOfBoundsException {
        throw new WriteNotSupportedException();
    }

    @Override
    public void setByte(final int index, final byte value) throws IndexOutOfBoundsException {
        throw new WriteNotSupportedException();
    }


}
