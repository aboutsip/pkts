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

    public int getLength() {
        return (this.buffer[1] & 0xff) << 16
                | (this.buffer[2] & 0xff) << 8 | (this.buffer[3] & 0xff) << 0;
    }

}
