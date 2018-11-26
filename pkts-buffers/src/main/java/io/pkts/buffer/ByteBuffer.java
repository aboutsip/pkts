/**
 * 
 */
package io.pkts.buffer;

import com.google.polo.pairing.HexDump;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.UnsupportedEncodingException;

/**
 * A buffer directly backed by a byte-array
 * 
 * @author jonas@jonasborjesson.com
 */
public class ByteBuffer extends AbstractBuffer {

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
        this(readerIndex, lowerBoundary, upperBoundary, upperBoundary, buffer);
    }

    protected ByteBuffer(final int readerIndex, final int lowerBoundary, final int upperBoundary,
            final int writerIndex, final byte[] buffer) {
        super(readerIndex, lowerBoundary, upperBoundary, writerIndex);
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
        checkIndex(lowerBoundary + start);
        checkIndex(lowerBoundary + stop - 1);
        final int upperBoundary = lowerBoundary + stop;
        final int writerIndex = upperBoundary;
        return createBuffer(0, lowerBoundary + start, upperBoundary, writerIndex, buffer);
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public Buffer readBytes(final int length) throws IndexOutOfBoundsException {
        if (length == 0) {
            return Buffers.EMPTY_BUFFER;
        }
        checkReadableBytes(length);
        final int lowerBoundary = readerIndex + this.lowerBoundary;
        readerIndex += length;
        final int upperBoundary = readerIndex + this.lowerBoundary;
        final int writerIndex = upperBoundary;
        return createBuffer(0, lowerBoundary, upperBoundary, writerIndex, buffer);
    }

    /**
     * Sub-classes should override this method to return their specific version.
     *
     * TODO: should perhaps re-structure ByteBuffer into a new base class (DirectBuffer ala Netty?)
     * TODO: should probably also have this one return a T extends Buffer or something
     *
     * @param readerIndex
     * @param lowerBoundary
     * @param upperBoundary
     * @param writerIndex
     * @param buffer
     * @return
     */
    protected Buffer createBuffer(final int readerIndex, final int lowerBoundary, final int upperBoundary,
                                  final int writerIndex, final byte[] buffer) {
        return new ByteBuffer(0, lowerBoundary, upperBoundary, writerIndex, buffer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte getByte(final int index) throws IndexOutOfBoundsException {
        checkIndex(lowerBoundary + index);
        return buffer[lowerBoundary + index];
    }

    @Override
    public void write(final byte b) throws IndexOutOfBoundsException {
        checkWriterIndex(writerIndex);
        buffer[lowerBoundary + writerIndex] = b;
        ++writerIndex;
    }

    @Override
    public void write(final byte[] bytes) throws IndexOutOfBoundsException {
        if (!checkWritableBytesSafe(bytes.length)) {
            throw new IndexOutOfBoundsException("Unable to write the entire String to this buffer. Nothing was written");
        }

        System.arraycopy(bytes, 0, buffer, writerIndex, bytes.length);
        writerIndex += bytes.length;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getArray() {
        final int length = getReadableBytes();
        final byte[] array = new byte[length];
        System.arraycopy(buffer, lowerBoundary + readerIndex, array, 0, length);
        return array;
    }

    @Override
    public byte[] getRawArray() {
        return buffer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte readByte() throws IndexOutOfBoundsException {
        return getByte(readerIndex++);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte peekByte() throws IndexOutOfBoundsException, IOException {
        return getByte(readerIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long readUnsignedInt() throws IndexOutOfBoundsException {
        return readInt() & 0xFFFFFFFFL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int readInt() throws IndexOutOfBoundsException {
        final int value = getInt(readerIndex);
        readerIndex += 4;
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public short readShort() throws IndexOutOfBoundsException {
        final short value = getShort(readerIndex);
        readerIndex += 2;
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
        final int i = lowerBoundary + index;
        checkIndex(i);
        checkIndex(i + 3);
        return (buffer[i] & 0xff) << 24 | (buffer[i + 1] & 0xff) << 16
                | (buffer[i + 2] & 0xff) << 8 | (buffer[i + 3] & 0xff) << 0;
    }

    @Override
    public long getUnsignedInt(final int index) {
        final int i = lowerBoundary + index;
        checkIndex(i);
        checkIndex(i + 3);
        return Buffer.unsignedInt(buffer[i], buffer[i + 1], buffer[i + 2], buffer[i + 3]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public short getShort(final int index) {
        final int i = lowerBoundary + index;
        checkIndex(i);
        checkIndex(i + 1);

        // big endian
        return (short) (buffer[i] << 8 | buffer[i + 1] & 0xFF);

        // little endian
        // return (short) (this.buffer[i] & 0xFF | this.buffer[i + 1] << 8);
    }

    @Override
    public void setUnsignedShort(final int index, final int value) {
        final int i = lowerBoundary + index;
        checkIndex(i);
        checkIndex(i + 1);
        buffer[i] = (byte) (value >> 8);
        buffer[i + 1] = (byte) value;
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
        return HexDump.dumpHexString(buffer, lowerBoundary, upperBoundary - lowerBoundary);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer clone() {
        final int size = capacity();
        final byte[] copy = new byte[size];
        System.arraycopy(buffer, lowerBoundary, copy, 0, size);
        return new ByteBuffer(copy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = 1;
        for (int i = lowerBoundary + readerIndex; i < upperBoundary; ++i) {
            result = 31 * result + buffer[i];
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        return internalEquals(false, other);
    }

    @Override
    public boolean equalsIgnoreCase(final Object other) {
        return internalEquals(true, other);
    }

    private boolean internalEquals(final boolean ignoreCase, final Object other) {
        try {
            if (this == other) {
                return true;
            }
            final ByteBuffer b = (ByteBuffer) other;
            if (getReadableBytes() != b.getReadableBytes()) {
                return false;
            }

            final int length = getReadableBytes();
            for (int i = 0; i < length; ++i) {
                final byte a1 = buffer[lowerBoundary + i];
                final byte b1 = b.buffer[b.lowerBoundary + i];
                // Do a UTF-8-aware, possibly case-insensitive character match. Only considers
                // case of 7-bit ASCII characters 'a'-'z'. In UTF-8, all bytes of multi-byte
                // characters have thier most signifcant bit set, so they won't be erroneously
                // considered by this algorithm since they won't fall in the range 0x41-0x5a/
                // 0x61-0x7a.

                // This algorithm won't work with UTF-16, and could misfire on malformed UTF-8,
                // e.g. the first byte of a UTF-8 sequence marks the beginning of a multi-byte
                // sequence but the second byte does not have the two high-order bits set to 10.

                // For 7-bit ascii leters, upper and lower-case only differ by one bit,
                // i.e. 'A' is 0x41, and 'a' is 0x61. We need only compare the 5 least
                // signifcant bits.

                 if (a1 != b1) {
                    if (ignoreCase &&
                        ((a1 >= 'A' && a1 <= 'Z') || (a1 >= 'a' && a1 <= 'z')) &&
                        ((b1 >= 'A' && b1 <= 'Z') || (b1 >= 'a' && b1 <= 'z')) &&
                        (a1 & 0x1f) == (b1 & 0x1f)) {
                          continue;
                    }
                    return false;
                }
            }

            return true;
        } catch (final NullPointerException | ClassCastException e) {
            return false;
        }
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public void setByte(final int index, final byte value) throws IndexOutOfBoundsException {
        final int i = lowerBoundary + index;
        checkIndex(i);
        buffer[i] = value;
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public void setUnsignedByte(final int index, final short value) throws IndexOutOfBoundsException {
        final int i = lowerBoundary + index;
        checkIndex(i);
        buffer[lowerBoundary + index] = (byte) value;
    }

    @Override
    public String toString() {
        try {
            return new String(getArray(), "UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ReadOnlyBuffer toReadOnly() {
        final byte[] clone = getArray();
        return ReadOnlyBuffer.of(clone);
    }

    @Override
    public void getBytes(final Buffer dst) {
        getBytes(getReaderIndex(), dst);
    }

    @Override
    public void getBytes(final int index, final Buffer dst) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("Index less than zero");
        }
        final int max = dst.getWritableBytes();
        final int stop = Math.min(lowerBoundary + index + max, writerIndex);
        for (int i = lowerBoundary + index; i < stop; ++i) {
            dst.write(buffer[i]);
        }
    }

    @Override
    public void getBytes(final byte[] dst) throws IndexOutOfBoundsException {
        final int length = Math.min(dst.length, getReadableBytes());
        System.arraycopy(buffer, lowerBoundary + readerIndex, dst, 0, length);
    }

    public void getBytes(final int index, final java.nio.ByteBuffer dst) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("Index less than zero");
        }
        final int stop = lowerBoundary + index;
        // for (int i = this.lowerBoundary + index; i < stop; ++i) {
        for (int i = stop - 1; i >= lowerBoundary + index; --i) {
            dst.put(buffer[i]);
        }
    }

    public void writeExternal(final ObjectOutput out) throws IOException {
        out.write(buffer, lowerBoundary, writerIndex - lowerBoundary);
    }

    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean hasWriteSupport() {
        return true;
    }

    @Override
    public void write(final String s) throws IndexOutOfBoundsException, WriteNotSupportedException,
    UnsupportedEncodingException {
        write(s, "UTF-8");
    }

    @Override
    public void write(final String s, final String charset) throws IndexOutOfBoundsException,
    WriteNotSupportedException, UnsupportedEncodingException {
        final byte[] bytes = s.getBytes(charset);
        if (!checkWritableBytesSafe(bytes.length)) {
            throw new IndexOutOfBoundsException("Unable to write the entire String to this buffer. Nothing was written");
        }

        System.arraycopy(bytes, 0, buffer, writerIndex, bytes.length);
        writerIndex += bytes.length;
    }

    @Override
    public void setInt(final int index, final int value) throws IndexOutOfBoundsException {
        checkIndex(index);
        checkIndex(index + 3);
        buffer[lowerBoundary + index + 0] = (byte) (value >>> 24);
        buffer[lowerBoundary + index + 1] = (byte) (value >>> 16);
        buffer[lowerBoundary + index + 2] = (byte) (value >>> 8);
        buffer[lowerBoundary + index + 3] = (byte) value;
    }

    @Override
    public void setUnsignedInt(final int index, final long value) throws IndexOutOfBoundsException {
        checkIndex(index);
        checkIndex(index + 3);
        buffer[lowerBoundary + index + 0] = (byte) value;
        buffer[lowerBoundary + index + 1] = (byte) (value >>> 8);
        buffer[lowerBoundary + index + 2] = (byte) (value >>> 16);
        buffer[lowerBoundary + index + 3] = (byte) (value >>> 24);
    }

    @Override
    public void write(final int value) throws IndexOutOfBoundsException, WriteNotSupportedException {
        if (!checkWritableBytesSafe(4)) {
            throw new IndexOutOfBoundsException("Unable to write the entire String to this buffer. Nothing was written");
        }
        final int index = lowerBoundary + writerIndex;
        buffer[index + 0] = (byte) (value >>> 24);
        buffer[index + 1] = (byte) (value >>> 16);
        buffer[index + 2] = (byte) (value >>> 8);
        buffer[index + 3] = (byte) value;
        writerIndex += 4;
    }

    @Override
    public void write(final long value) throws IndexOutOfBoundsException, WriteNotSupportedException {
        if (!checkWritableBytesSafe(8)) {
            throw new IndexOutOfBoundsException("Unable to write the entire String to this buffer. Nothing was written");
        }
        final int index = lowerBoundary + writerIndex;
        buffer[index + 0] = (byte) (value >>> 56);
        buffer[index + 1] = (byte) (value >>> 48);
        buffer[index + 2] = (byte) (value >>> 40);
        buffer[index + 3] = (byte) (value >>> 32);
        buffer[index + 4] = (byte) (value >>> 24);
        buffer[index + 5] = (byte) (value >>> 16);
        buffer[index + 6] = (byte) (value >>> 8);
        buffer[index + 7] = (byte) value;
        writerIndex += 8;
    }

    @Override
    public void writeAsString(final int value) throws IndexOutOfBoundsException, WriteNotSupportedException {
        final int size = value < 0 ? Buffers.stringSize(-value) + 1 : Buffers.stringSize(value);
        if (!checkWritableBytesSafe(size)) {
            throw new IndexOutOfBoundsException();
        }
        Buffers.getBytes(value, lowerBoundary + writerIndex + size, buffer);
        writerIndex += size;
    }

    @Override
    public void writeAsString(final long value) throws IndexOutOfBoundsException, WriteNotSupportedException {
        final int size = value < 0 ? Buffers.stringSize(-value) + 1 : Buffers.stringSize(value);
        if (!checkWritableBytesSafe(size)) {
            throw new IndexOutOfBoundsException();
        }
        Buffers.getBytes(value, lowerBoundary + writerIndex + size, buffer);
        writerIndex += size;
    }
}
