/**
 * 
 */
package com.aboutsip.yajpcap.protocol.sip;

import com.aboutsip.buffer.Buffer;

/**
 * @author jonas@jonasborjesson.com
 */
public final class SipRequest extends SipMessage {

    /**
     * Our raw request line.
     */
    private final Buffer requestLine;

    /**
     * Create a new SipRequest based on the raw request line.
     * 
     * Note, since this library is all about framing we don't actually know at
     * this point in time whether the passed in request line actually contains
     * the information we need. For all we know, it can be garbage but we will
     * check if the user ever asks us to actually parse the message
     */
    public SipRequest(final Buffer requestLine) {
        assert requestLine != null;
        this.requestLine = requestLine;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRequest() {
        return true;
    }

}
