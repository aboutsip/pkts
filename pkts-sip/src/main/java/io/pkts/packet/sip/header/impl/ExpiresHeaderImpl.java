package io.pkts.packet.sip.header.impl;

import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.header.ExpiresHeader;

public class ExpiresHeaderImpl extends SipHeaderImpl implements ExpiresHeader {

    private int expires;

    public ExpiresHeaderImpl(final int expires) {
        super(ExpiresHeader.NAME, Buffers.wrap(expires));
        this.expires = expires;
    }

    @Override
    public int getExpires() {
        return this.expires;
    }

    @Override
    public ExpiresHeader clone() {
        return new ExpiresHeaderImpl(this.expires);
    }

    @Override
    public ExpiresHeader ensure() {
        return this;
    }

    @Override
    public ExpiresHeader.Builder copy() {
        return new ExpiresHeader.Builder(this.expires);
    }
}
