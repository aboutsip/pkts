/**
 * 
 */
package com.aboutsip.yajpcap.packet.impl.sip;

import com.aboutsip.buffer.Buffer;

/**
 * Class representing a sip request line
 * 
 * @author jonas@jonasborjesson.com
 */
public final class SipRequestLine extends SipInitialLine {

    private final Buffer method;
    private final Buffer requestUri;

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

}
