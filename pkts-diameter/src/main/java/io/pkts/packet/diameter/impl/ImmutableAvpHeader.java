package io.pkts.packet.diameter.impl;

import java.util.Optional;

import io.pkts.packet.diameter.AvpHeader;

/**
 * @author jonas@jonasborjesson.com
 */
public class ImmutableAvpHeader implements AvpHeader {

    @Override
    public int getCode() {
        return 0;
    }

    @Override
    public long getLength() {
        return 0;
    }

    @Override
    public Optional<Long> getVendorId() {
        return null;
    }
}
