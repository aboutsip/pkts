/**
 * 
 */
package com.aboutsip.yajpcap.packet.sip.impl;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.yajpcap.frame.Layer7Frame;
import com.aboutsip.yajpcap.packet.TransportPacket;
import com.aboutsip.yajpcap.packet.sip.SipParseException;
import com.aboutsip.yajpcap.packet.sip.SipRequest;
import com.aboutsip.yajpcap.packet.sip.address.URI;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public final class SipRequestImpl extends SipMessageImpl implements SipRequest {

    private final SipRequestLine requestLine;

    /**
     * 
     */
    public SipRequestImpl(final TransportPacket parent, final SipRequestLine requestLine, final Buffer headers,
            final Buffer payload, final Layer7Frame sipFrame) {
        super(parent, requestLine, headers, payload, sipFrame);
        this.requestLine = requestLine;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer getMethod() {
        return this.requestLine.getMethod();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URI getRequestUri() throws SipParseException {
        return this.requestLine.getRequestUri();
    }

    @Override
    public SipRequest toRequest() throws ClassCastException {
        return this;
    }

    /**
     * Get the request line of this request.
     * 
     * @return
     */
    protected SipRequestLine getRequestLine() {
        return this.requestLine.clone();
    }

    @Override
    public SipRequest clone() {
        final TransportPacket transportPkt = getTransportPacket().clone();
        final SipRequestLine requestLine = this.requestLine.clone();
        final Buffer headers = this.cloneHeaders();
        final Buffer payload = this.clonePayload();
        return new SipRequestImpl(transportPkt, requestLine, headers, payload, null);
    }

}
