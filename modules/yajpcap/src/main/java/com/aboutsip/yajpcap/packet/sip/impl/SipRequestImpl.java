/**
 * 
 */
package com.aboutsip.yajpcap.packet.sip.impl;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.yajpcap.frame.SipFrame;
import com.aboutsip.yajpcap.packet.TransportPacket;
import com.aboutsip.yajpcap.packet.sip.SipRequest;

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
            final Buffer payload, final SipFrame sipFrame) {
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
    public Buffer getRequestUri() {
        return this.requestLine.getRequestUri();
    }

}
