/**
 * 
 */
package io.pkts.packet.sip.impl;

import io.pkts.buffer.Buffer;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.SipRequest;
import io.pkts.packet.sip.address.URI;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public final class SipRequestImpl extends SipMessageImpl implements SipRequest {

    /**
     * 
     */
    public SipRequestImpl(final Buffer requestLine, final Buffer headers,
            final Buffer payload) {
        super(requestLine, headers, payload);
    }

    /**
     * 
     */
    public SipRequestImpl(final SipRequestLine requestLine, final Buffer headers,
            final Buffer payload) {
        super(requestLine, headers, payload);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer getMethod() {
        return getRequestLine().getMethod();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URI getRequestUri() throws SipParseException {
        return getRequestLine().getRequestUri();
    }

    @Override
    public SipRequest toRequest() throws ClassCastException {
        return this;
    }

    @Override
    public SipRequest clone() {
        final SipRequestLine requestLine = getRequestLine().clone();
        final Buffer headers = this.cloneHeaders();
        final Buffer payload = this.clonePayload();
        return new SipRequestImpl(requestLine, headers, payload);
    }

}
