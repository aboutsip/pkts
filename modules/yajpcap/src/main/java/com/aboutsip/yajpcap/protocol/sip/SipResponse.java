/**
 * 
 */
package com.aboutsip.yajpcap.protocol.sip;

import com.aboutsip.buffer.Buffer;

/**
 * @author jonas@jonasborjesson.com
 */
public final class SipResponse extends SipMessage {

    private final Buffer responseLine;

    /**
     * 
     */
    public SipResponse(final Buffer responseLine) {
        this.responseLine = responseLine;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isResponse() {
        return true;
    }

}
