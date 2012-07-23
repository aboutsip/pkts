/**
 * 
 */
package com.aboutsip.yajpcap.packet.impl.sip;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.yajpcap.packet.SipMessage;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public abstract class SipMessageImpl implements SipMessage {

    /**
     * The initial line of the sip message, which is either a request or a
     * response line
     */
    private final SipInitialLine initialLine;

    /**
     * All the headers of the sip message
     */
    private final Buffer headers;

    /**
     * The payload, which may be null
     */
    private final Buffer payload;

    /**
     * 
     * @param initialLine the initial line, which is either a request or a
     *            response line
     * @param headers all the headers (un-parsed) of the SIP message
     * @param payload the payload or null if there is none
     */
    public SipMessageImpl(final SipInitialLine initialLine, final Buffer headers, final Buffer payload) {
        assert initialLine != null;
        assert headers != null;

        this.initialLine = initialLine;
        this.headers = headers;
        this.payload = payload;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isResponse() {
        return this.initialLine.isResponseLine();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isRequest() {
        return this.initialLine.isRequestLine();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract Buffer getMethod();

    /**
     * {@inheritDoc}
     */
    @Override
    public void verify() {
        // TODO Auto-generated method stub
    }

}
