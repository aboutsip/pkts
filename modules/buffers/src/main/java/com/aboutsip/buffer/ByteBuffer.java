/**
 * 
 */
package com.aboutsip.buffer;

import java.io.IOException;
import java.util.Arrays;

/**
 * A buffer directly backed by a byte-array
 * 
 * @author jonas@jonasborjesson.com
 */
public final class ByteBuffer extends AbstractBuffer {

    /**
     * The actual buffer
     */
    protected final byte[] buffer;


    /**
     * 
     */
    protected ByteBuffer(final byte[] buffer) {
        this(0, 0, buffer.length, buffer);
    }

    protected ByteBuffer(final int readerIndex, final int lowerBoundary, final int upperBoundary, final byte[] buffer) {
        super(readerIndex, lowerBoundary, upperBoundary);
        assert buffer != null;
        this.buffer = buffer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer slice(final int start, final int stop) {
        if (start == stop) {
            return Buffers.EMPTY_BUFFER;
        }
        checkIndex(this.lowerBoundary + start);
        checkIndex(this.lowerBoundary + stop - 1);
        return new ByteBuffer(0, this.lowerBoundary + start, this.lowerBoundary + stop, this.buffer);
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public Buffer readBytes(final int length) throws IndexOutOfBoundsException {
        checkReadableBytes(length);
        final int lowerBoundary = this.readerIndex + this.lowerBoundary;
        this.readerIndex += length;
        return new ByteBuffer(0, lowerBoundary, this.readerIndex + this.lowerBoundary, this.buffer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasReadableBytes() {
        return readableBytes() > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return readableBytes() == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte getByte(final int index) throws IndexOutOfBoundsException {
        checkIndex(this.lowerBoundary + index);
        return this.buffer[this.lowerBoundary + index];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getArray() {
        final int length = readableBytes();
        final byte[] array = new byte[length];
        System.arraycopy(this.buffer, this.lowerBoundary + this.readerIndex, array, 0, length);
        return array;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte readByte() throws IndexOutOfBoundsException {
        return getByte(this.readerIndex++);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte peekByte() throws IndexOutOfBoundsException, IOException {
        return getByte(this.readerIndex);
    }

    public long unsignedInt(final byte a, final byte b, final byte c, final byte d) {
        return (a & 0xff) << 24 | (b & 0xff) << 16 | (c & 0xff) << 8 | d & 0xff;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long readUnsignedInt() throws IndexOutOfBoundsException {
        return getInt(this.readerIndex) & 0xFFFFFFFFL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int readInt() throws IndexOutOfBoundsException {
        final int value = getInt(this.readerIndex);
        this.readerIndex += 4;
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public short readShort() throws IndexOutOfBoundsException {
        final short value = getShort(this.readerIndex);
        this.readerIndex += 2;
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int readUnsignedShort() {
        return readShort() & 0xFFFF;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getInt(final int index) {
        final int i = this.lowerBoundary + index;
        checkIndex(i);
        checkIndex(i + 4);
        return (this.buffer[i] & 0xff) << 24 | (this.buffer[i + 1] & 0xff) << 16
                | (this.buffer[i + 2] & 0xff) << 8 | (this.buffer[i + 3] & 0xff) << 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public short getShort(final int index) {
        final int i = this.lowerBoundary + index;
        checkIndex(i);
        checkIndex(i + 1);

        // big endian
        return (short) (this.buffer[i] << 8 | this.buffer[i + 1] & 0xFF);

        // little endian
        // return (short) (this.buffer[i] & 0xFF | this.buffer[i + 1] << 8);
    }

    @Override
    public void setUnsignedShort(final int index, final int value) {
        final int i = this.lowerBoundary + index;
        checkIndex(i);
        checkIndex(i + 1);
        this.buffer[i] = (byte) (value >> 8);
        this.buffer[i + 1] = (byte) value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getUnsignedShort(final int index) throws IndexOutOfBoundsException {
        return getShort(index) & 0xFFFF;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public short getUnsignedByte(final int index) throws IndexOutOfBoundsException {
        return (short) (getByte(index) & 0xFF);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String dumpAsHex() {
        return "dumpAsHex isn't implemented just yet";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer clone() {
        final int size = capacity();
        final byte[] copy = new byte[size];
        System.arraycopy(this.buffer, this.lowerBoundary, copy, 0, size);
        return new ByteBuffer(copy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = 1;
        for (int i = this.lowerBoundary + this.readerIndex; i < this.upperBoundary; ++i) {
            result = 31 * result + this.buffer[i];
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ByteBuffer other = (ByteBuffer) obj;
        // TODO: compare them byte by byte "manually" instead of having to
        // copy them first.
        return Arrays.equals(getArray(), other.getArray());
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public void setByte(final int index, final byte value) throws IndexOutOfBoundsException {
        final int i = this.lowerBoundary + index;
        checkIndex(i);
        this.buffer[this.lowerBoundary + index] = value;
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public void setUnsignedByte(final int index, final short value) throws IndexOutOfBoundsException {
        final int i = this.lowerBoundary + index;
        checkIndex(i);
        this.buffer[this.lowerBoundary + index] = (byte) value;
    }

    @Override
    public String toString() {
        return new String(getArray());
    }

}
