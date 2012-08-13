/**
 * 
 */
package com.aboutsip.yajpcap.packet.layer7.sip.impl;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.yajpcap.packet.layer4.TransportPacket;
import com.aboutsip.yajpcap.packet.layer7.sip.SipRequest;

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
            final Buffer payload) {
        super(parent, requestLine, headers, payload);
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
    public Buffer getRequestUri() {
        return this.requestLine.getRequestUri();
    }

}
