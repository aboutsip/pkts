/**
 * 
 */
package com.aboutsip.yajpcap.packet.impl.sip;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.buffer.Buffers;
import com.aboutsip.yajpcap.packet.SipMessage;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public abstract class SipMessageImpl implements SipMessage {

    private static final Buffer FROM_HEADER = Buffers.wrap("From".getBytes());

    private static final Buffer TO_HEADER = Buffers.wrap("To".getBytes());

    private static final Buffer Call_ID_HEADER = Buffers.wrap("Call-ID".getBytes());

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

    private Buffer fromHeader;

    private Buffer toHeader;

    private Buffer callIDHeader;

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
    public Buffer getHeader(final Buffer headerName) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer getFromHeader() {
        if (this.fromHeader != null) {
            return this.fromHeader;
        }

        this.fromHeader = getHeader(FROM_HEADER);
        return this.fromHeader;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer getToHeader() {
        return null;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer getCallIDHeader() {
        return null;

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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.initialLine.toString()).append("\n");
        sb.append(this.headers.toString()).append("\n");
        if (this.payload != null) {
            sb.append(this.payload.toString()).append("\n");
        }
        return sb.toString();
    }

}
