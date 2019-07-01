package io.pkts.gtp.control.impl;

import io.pkts.gtp.control.TypeLengthInstanceValue;
import io.snice.buffer.Buffer;
import io.snice.buffer.ReadableBuffer;

import static io.snice.preconditions.PreConditions.assertArgument;
import static io.snice.preconditions.PreConditions.assertNotNull;

public class TypeLengthInstanceValueImpl implements TypeLengthInstanceValue {

    private static final byte EXTENSION_TYPE = (byte) 0xFE;

    private final Buffer header;
    private final Buffer value;

    private TypeLengthInstanceValueImpl(final Buffer header, final Buffer value) {
        this.header = header;
        this.value = value;
    }

    public static TypeLengthInstanceValue frame(final Buffer buffer) {
        assertNotNull(buffer, "The buffer cannot be null");
        return frame(buffer.toReadableBuffer());
    }

    public static TypeLengthInstanceValue frame(final ReadableBuffer buffer) {
        assertNotNull(buffer, "The buffer cannot be null");
        assertArgument(buffer.getReadableBytes() >= 4, "A GTPv2 TLIV has at least 4 bytes");

        final byte type = buffer.getByte(buffer.getReaderIndex());
        if (type == EXTENSION_TYPE) {
            throw new RuntimeException("Haven't implemented the extension type just yet");
        }


        final Buffer header = buffer.readBytes(4);
        final int length = header.getUnsignedShort(1);
        final Buffer value = buffer.readBytes(length);

        return new TypeLengthInstanceValueImpl(header, value);
    }

    @Override
    public byte getType() {
        return header.getByte(0);
    }

    @Override
    public int getLength() {
        return header.getUnsignedShort(1);
    }

    @Override
    public Buffer getValue() {
        return value;
    }
}
