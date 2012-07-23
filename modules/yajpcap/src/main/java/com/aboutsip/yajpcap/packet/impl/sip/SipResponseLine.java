/**
 * 
 */
package com.aboutsip.yajpcap.packet.impl.sip;

import com.aboutsip.buffer.Buffer;

/**
 * @author jonas@jonasborjesson.com
 */
public final class SipResponseLine extends SipInitialLine {

    /**
     * The status code of the response. I.e., 180, 200, 404 etc etx
     */
    private final int statusCode;

    /**
     * The response reason
     */
    private final Buffer reason;

    public SipResponseLine(final int statusCode, final Buffer reason) {
        super();
        assert reason != null;
        this.statusCode = statusCode;
        this.reason = reason;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isResponseLine() {
        return true;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public Buffer getReason() {
        return this.reason;
    }

}
