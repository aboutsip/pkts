package io.pkts.buffer;

import java.io.UnsupportedEncodingException;

/**
 * The {@link ReadOnlyBuffer} do not allow you to do any write operations on the buffer, which
 * includes any set operations. Hence, the content of the buffer is immutable but the reason
 * why we do not call the entire interface "immutable" is, technically, it does mutate when
 * you read because it will change the internal reader index.
 */
public interface ReadOnlyBuffer extends Buffer {

    static ReadOnlyBuffer of(final byte[] buffer) {
        return new ReadOnlyByteBuffer(buffer);
    }

    static ReadOnlyBuffer of(final String buffer) {
        return Buffers.wrapAsReadOnly(buffer);
    }

    @Override
    default boolean hasWriteSupport() {
        return false;
    }

    @Override
    default void setWriterIndex(final int index) {
        throw new WriteNotSupportedException();
    }

    @Override
    default int getWritableBytes() {
        return 0;
    }

    @Override
    default boolean hasWritableBytes() {
        return false;
    }

    @Override
    default void write(final byte b) throws IndexOutOfBoundsException, WriteNotSupportedException {
        throw new WriteNotSupportedException();
    }

    @Override
    default void write(final byte[] bytes) throws IndexOutOfBoundsException, WriteNotSupportedException {
        throw new WriteNotSupportedException();
    }

    @Override
    default void write(final int value) throws IndexOutOfBoundsException, WriteNotSupportedException {
        throw new WriteNotSupportedException();
    }

    @Override
    default void write(final long value) throws IndexOutOfBoundsException, WriteNotSupportedException {
        throw new WriteNotSupportedException();
    }

    @Override
    default void write(final String s) throws IndexOutOfBoundsException, WriteNotSupportedException, UnsupportedEncodingException {
        throw new WriteNotSupportedException();
    }

    @Override
    default void writeAsString(final int value) throws IndexOutOfBoundsException, WriteNotSupportedException {
        throw new WriteNotSupportedException();
    }

    @Override
    default void writeAsString(final long value) throws IndexOutOfBoundsException, WriteNotSupportedException {
        throw new WriteNotSupportedException();
    }

    @Override
    default void write(final String s, final String charset) throws IndexOutOfBoundsException, WriteNotSupportedException,
            UnsupportedEncodingException {
        throw new WriteNotSupportedException();
    }
}
