package io.pkts.packet.sip.header.impl;

import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.header.ContentLengthHeader;

public class ContentLengthHeaderImpl extends SipHeaderImpl implements ContentLengthHeader {

    private int length;

    public ContentLengthHeaderImpl(final int length) {
        super(ContentLengthHeader.NAME, Buffers.wrap(length));
        this.length = length;
    }

    @Override
    public int getContentLength() {
        return this.length;
    }

    @Override
    public ContentLengthHeader clone() {
        return new ContentLengthHeaderImpl(this.length);
    }

    @Override
    public ContentLengthHeader ensure() {
        return this;
    }

    @Override
    public ContentLengthHeader.Builder copy() {
        return new ContentLengthHeader.Builder(this.length);
    }
}
