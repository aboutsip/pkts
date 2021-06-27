package io.pkts.packet.sctp.impl;

import io.pkts.buffer.Buffer;
import io.pkts.packet.sctp.SctpChunk;
import io.pkts.packet.sctp.SctpParseException;

import java.io.IOException;

import static io.pkts.buffer.Buffers.assertNotEmpty;
import static io.pkts.packet.PreConditions.assertArgument;

public class DefaultSctpChunk implements SctpChunk {

    private final SctpChunk.Type type;
    private final Buffer header;

    /**
     * This contains the full value as was seen on the wire, including padding.
     * The reason we save this is if we have to write back the chunk to the
     * wire (or rather in a pcap) then it is ready to go.
     */
    // private final Buffer valueOnWire;

    /**
     * This is just the value excluding padding.
     */
    private final Buffer value;

    private final int padding;

    public static DefaultSctpChunk frame(final Buffer buffer) {
        try {
            assertNotEmpty(buffer, "The buffer cannot be null or empty");
            assertArgument(buffer.getReadableBytes() >= 4, "There must be at least 4 bytes to read");
            final Buffer header = buffer.readBytes(4);

            final SctpChunk.Type type = SctpChunk.lookup(header.getUnsignedByte(0));
            final int length = header.getUnsignedShort(2) - 4;
            final int padding = DefaultSctpChunk.calculatePadding(length);

            assertArgument(buffer.getReadableBytes() >= length + padding, "Unable to read Chunk Value. Not enough bytes. Needed "
                    + (length + padding) + " bytes but only " + buffer.getReadableBytes() + " bytes available");
            final Buffer value = buffer.readBytes(length);
            buffer.readBytes(padding);
            return new DefaultSctpChunk(type, header, value, padding);
        } catch (final IOException e) {
            throw new SctpParseException(buffer.getReaderIndex(), "Unable to read from buffer. Message (if any) " + e.getMessage());
        }
    }

    private DefaultSctpChunk(final Type type, final Buffer header, final Buffer valueOnWire, final int padding) {
        this.type = type;
        this.header = header;
        this.value = valueOnWire;
        this.padding = padding;
    }


    @Override
    public Type getType() {
        return type;
    }

    @Override
    public int getLength() {
        return 4 + value.capacity() + padding;
    }

    @Override
    public Buffer getHeader() {
        return header;
    }

    @Override
    public Buffer getValue() {
        return value;
    }

    @Override
    public byte getFlags() {
        try {
            return header.getByte(1);
        } catch (final IOException e) {
            throw new SctpParseException(1, "IOException when reading flags");
        }
    }

    /**
     * The length of the value of the chunk.
     * <p>
     * Note that the length as encoded into the Chunk "header" includes the 4 bytes containing the size of
     * the fields making up the header. This length is JUST the length of the actual value. Also, it does not
     * include padding.
     */
    @Override
    public int getValueLength() {
        return value.capacity();
    }

    @Override
    public int getPadding() {
        return padding;
    }

    private static int calculatePadding(final int length) {
        final int padding = length % 4;
        if (padding != 0) {
            return 4 - padding;
        }
        return 0;
    }


}
