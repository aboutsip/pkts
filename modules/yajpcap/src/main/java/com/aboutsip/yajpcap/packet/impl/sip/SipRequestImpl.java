/**
 * 
 */
package com.aboutsip.yajpcap.packet.impl.sip;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.yajpcap.packet.SipRequest;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public final class SipRequestImpl extends SipMessageImpl implements SipRequest {

    private final SipRequestLine requestLine;

    /**
     * 
     */
    public SipRequestImpl(final SipRequestLine requestLine, final Buffer headers, final Buffer payload) {
        super(requestLine, headers, payload);
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
