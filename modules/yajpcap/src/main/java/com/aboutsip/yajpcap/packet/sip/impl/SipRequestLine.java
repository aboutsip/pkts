/**
 * 
 */
package com.aboutsip.yajpcap.packet.sip.impl;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.buffer.Buffers;

/**
 * Class representing a sip request line
 * 
 * @author jonas@jonasborjesson.com
 */
public final class SipRequestLine extends SipInitialLine {

    private final Buffer method;
    private final Buffer requestUri;
    private Buffer requestLine;

    public SipRequestLine(final Buffer method, final Buffer requestUri) {
        super();
        assert method != null;
        assert requestUri != null;
        this.method = method;
        this.requestUri = requestUri;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRequestLine() {
        return true;
    }

    public Buffer getMethod() {
        return this.method;
    }

    public Buffer getRequestUri() {
        return this.requestUri;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer getBuffer() {
        // TODO: redo
        if (this.requestLine == null) {
            final StringBuilder sb = new StringBuilder();
            sb.append(this.method.toString()).append(" ").append(this.requestUri.toString()).append(" SIP/2.0");
            this.requestLine = Buffers.wrap(sb.toString());
        }

        return this.requestLine;
    }

    @Override
    public String toString() {
        return getBuffer().toString();
    }

}
