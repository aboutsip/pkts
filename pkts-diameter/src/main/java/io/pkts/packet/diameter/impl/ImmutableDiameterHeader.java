package io.pkts.packet.diameter.impl;

import io.pkts.buffer.Buffer;
import io.pkts.packet.diameter.DiameterHeader;

/**
 *
 * @author jonas@jonasborjesson.com
 */
public class ImmutableDiameterHeader implements DiameterHeader {
    private final byte[] buffer;

    protected ImmutableDiameterHeader(final Buffer buffer) {
        this.buffer = buffer.getRawArray();
    }

    @Override
    public int getLength() {
        return (buffer[1] & 0xff) << 16
                | (buffer[2] & 0xff) << 8 | (buffer[3] & 0xff) << 0;
    }

    @Override
    public boolean isRequest() {
        return false;
    }

    @Override
    public boolean isProxiable() {
        return false;
    }

    @Override
    public boolean isError() {
        return false;
    }

    @Override
    public boolean isPossiblyRetransmission() {
        return false;
    }

    @Override
    public long getApplicationId() {
        return 0;
    }

    @Override
    public long getHopByHopId() {
        return 0;
    }

    @Override
    public long getEndToEndId() {
        return 0;
    }

}
