package io.pkts.packet.sctp.impl;

import io.pkts.buffer.Buffer;
import io.pkts.packet.sctp.SctpChunk;

public class DelegatingSctpChunk implements SctpChunk {
    private final DefaultSctpChunk chunk;

    protected DelegatingSctpChunk(final DefaultSctpChunk chunk) {
        this.chunk = chunk;
    }

    @Override
    public Type getType() {
        return chunk.getType();
    }

    @Override
    public int getLength() {
        return chunk.getLength();
    }

    @Override
    public Buffer getHeader() {
        return chunk.getHeader();
    }

    @Override
    public Buffer getValue() {
        return chunk.getValue();
    }

    @Override
    public int getPadding() {
        return chunk.getPadding();
    }

    @Override
    public byte getFlags() {
        return chunk.getFlags();
    }

    @Override
    public int getValueLength() {
        return chunk.getValueLength();
    }
}
