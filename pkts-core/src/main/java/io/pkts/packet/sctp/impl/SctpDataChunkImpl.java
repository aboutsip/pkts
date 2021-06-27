package io.pkts.packet.sctp.impl;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sctp.SctpDataChunk;

import static io.pkts.packet.PreConditions.assertNotNull;

public class SctpDataChunkImpl extends DelegatingSctpChunk implements SctpDataChunk {

    public static final SctpDataChunk of(final DefaultSctpChunk chunk) {
        assertNotNull(chunk, "The chunk we are wrapping cannot be null");
        return new SctpDataChunkImpl(chunk);
    }

    private final Buffer dataHeader;
    private final Buffer payload;

    private SctpDataChunkImpl(final DefaultSctpChunk chunk) {
        super(chunk);
        final Buffer header = chunk.getHeader();
        final Buffer value = chunk.getValue();
        final Buffer remainingDataHeader = value.slice(12);
        payload = value.slice(12, value.capacity());
        dataHeader = Buffers.wrap(header, remainingDataHeader);
    }

    @Override
    public Buffer getHeader() {
        return dataHeader;
    }

    @Override
    public boolean isUnordered() {
        return dataHeader.getBit2(1);
    }

    @Override
    public boolean isBeginningFragment() {
        return dataHeader.getBit1(1);
    }

    @Override
    public boolean isEndingFragment() {
        return dataHeader.getBit0(1);
    }

    @Override
    public boolean isImmediate() {
        return dataHeader.getBit3(1);
    }

    @Override
    public long getTransmissionSequenceNumber() {
        return dataHeader.getUnsignedInt(4);
    }

    @Override
    public int getStreamIdentifier() {
        return dataHeader.getUnsignedShort(8);
    }

    @Override
    public int getStreamSequenceNumber() {
        return dataHeader.getUnsignedShort(10);
    }

    @Override
    public long getPayloadProtocolIdentifier() {
        return dataHeader.getUnsignedInt(12);
    }

    @Override
    public Buffer getUserData() {
        return payload;
    }
}
