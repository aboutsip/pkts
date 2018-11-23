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
        return DiameterParser.getIntFromThreeOctets(buffer[1], buffer[2], buffer[3]);
    }

    @Override
    public boolean isRequest() {
        // 5th byte is the command flags
        return (buffer[4] & 0b10000000) == 0b10000000;
    }

    @Override
    public boolean isProxiable() {
        return (buffer[4] & 0b01000000) == 0b01000000;
    }

    @Override
    public boolean isError() {
        return (buffer[4] & 0b00100000) == 0b00100000;
    }

    @Override
    public boolean isPossiblyRetransmission() {
        return (buffer[4] & 0b00010000) == 0b00010000;
    }

    @Override
    public int getCommandCode() {
        return DiameterParser.getIntFromThreeOctets(buffer[5], buffer[6], buffer[7]);
    }

    @Override
    public long getApplicationId() {
        return getLong(8);
    }

    @Override
    public long getHopByHopId() {
        return getLong(12);
    }

    @Override
    public long getEndToEndId() {
        return getLong(16);
    }

    @Override
    public boolean validate() {
        // the version must be 1 so if it isn't, bail out.
        if (!((buffer[0] & 0b00000001) == 0b00000001)) {
            return false;
        }

        // then, in the 5th byte we have the Command Flags. Currently, the last 4 bits of that
        // byte is always NOT set. These are reserved bits which currently isn't being used so
        // expect them to indeed be zero...
        // Perhaps this check is dangerous in that if one of the bits is being used, we won't accept the
        // message even though perhaps it really doesn't matter.
        if (!((buffer[4] & 0b00001111) == 0b00000000)) {
            return false;
        }

        // also, the length must be a multiple of 4 according to spec since the AVPs will be padded
        // if need be.
        return getLength() % 4 == 0;
    }

    private long getLong(final int i) {
        return (buffer[i] & 0xff) << 24 | (buffer[i + 1] & 0xff) << 16
                | (buffer[i + 2] & 0xff) << 8 | (buffer[i + 3] & 0xff) << 0;
    }

}
