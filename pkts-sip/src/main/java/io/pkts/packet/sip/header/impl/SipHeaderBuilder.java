package io.pkts.packet.sip.header.impl;

import io.pkts.buffer.Buffer;
import io.pkts.packet.sip.header.SipHeader;

/**
 * @author jonas@jonasborjesson.com
 */
public class SipHeaderBuilder implements SipHeader.Builder<SipHeader> {

    private final Buffer name;
    private Buffer value;

    public SipHeaderBuilder(final Buffer name, final Buffer value) {
        this.name = name;
    }

    @Override
    public SipHeader.Builder<SipHeader> withValue(final Buffer value) {
        this.value = value;
        return this;
    }

    @Override
    public SipHeader build() {
        return new SipHeaderImpl(name, value);
    }
}
