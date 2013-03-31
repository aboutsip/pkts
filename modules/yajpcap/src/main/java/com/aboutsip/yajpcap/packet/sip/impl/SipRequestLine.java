/**
 * 
 */
package com.aboutsip.yajpcap.packet.sip.impl;

import java.io.IOException;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.buffer.Buffers;
import com.aboutsip.yajpcap.packet.sip.SipParseException;
import com.aboutsip.yajpcap.packet.sip.address.URI;
import com.aboutsip.yajpcap.packet.sip.address.impl.SipURIImpl;

/**
 * Class representing a sip request line
 * 
 * @author jonas@jonasborjesson.com
 */
public final class SipRequestLine extends SipInitialLine {

    private final Buffer method;
    private final Buffer requestUriBuffer;
    private Buffer requestLine;

    /**
     * The parsed request uri, which may be null if no one has asked about it
     * yet.
     */
    private URI requestURI;

    public SipRequestLine(final Buffer method, final Buffer requestUri) {
        super();
        assert method != null;
        assert requestUri != null;
        this.method = method;
        this.requestUriBuffer = requestUri;

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

    public URI getRequestUri() throws SipParseException {
        if (this.requestURI == null) {
            try {
                this.requestURI = SipURIImpl.frame(this.requestUriBuffer);
            } catch (final IOException e) {
                throw new SipParseException(0, "Unable to parse the request uri", e);
            }
        }
        return this.requestURI;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer getBuffer() {
        // TODO: redo
        if (this.requestLine == null) {
            final StringBuilder sb = new StringBuilder();
            sb.append(this.method.toString()).append(" ").append(this.requestUriBuffer.toString()).append(" SIP/2.0");
            this.requestLine = Buffers.wrap(sb.toString());
        }

        return this.requestLine;
    }

    @Override
    public String toString() {
        return getBuffer().toString();
    }

}
