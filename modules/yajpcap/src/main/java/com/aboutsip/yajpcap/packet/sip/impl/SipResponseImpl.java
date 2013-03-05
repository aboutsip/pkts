/**
 * 
 */
package com.aboutsip.yajpcap.packet.sip.impl;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.yajpcap.frame.SipFrame;
import com.aboutsip.yajpcap.packet.TransportPacket;
import com.aboutsip.yajpcap.packet.sip.SipHeader;
import com.aboutsip.yajpcap.packet.sip.SipResponse;
import com.aboutsip.yajpcap.packet.sip.header.CSeqHeader;
import com.aboutsip.yajpcap.packet.sip.header.impl.CSeqHeaderImpl;

/**
 * @author jonas@jonasborjesson.com
 */
public final class SipResponseImpl extends SipMessageImpl implements SipResponse {

    private CSeqHeader cseq;

    private final SipResponseLine initialLine;

    /**
     * @param initialLine
     * @param headers
     * @param payload
     */
    public SipResponseImpl(final TransportPacket pkt, final SipResponseLine initialLine, final Buffer headers,
            final Buffer payload, final SipFrame sipFrame) {
        super(pkt, initialLine, headers, payload, sipFrame);
        this.initialLine = initialLine;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws SipParseException
     */
    @Override
    public Buffer getMethod() throws SipParseException {
        if (this.cseq == null) {
            final SipHeader header = getHeader(CSEQ_HEADER);
            this.cseq = CSeqHeaderImpl.parseValue(header.getValue());
        }
        return this.cseq.getMethod();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getStatus() {
        return this.initialLine.getStatusCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isProvisional() {
        return (getStatus() / 100) == 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSuccess() {
        return (getStatus() / 200) == 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRedirect() {
        return (getStatus() / 300) == 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isClientError() {
        return (getStatus() / 400) == 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isServerError() {
        return (getStatus() / 500) == 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isGlobalError() {
        return (getStatus() / 600) == 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean is100Trying() {
        return getStatus() == 100;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRinging() {
        return getStatus() == 180;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTimeout() {
        return getStatus() == 480;
    }

}
