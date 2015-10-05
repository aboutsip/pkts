/**
 * 
 */
package io.pkts.packet.sip.impl;

import io.pkts.buffer.Buffer;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.SipResponse;
import io.pkts.packet.sip.header.CSeqHeader;
import io.pkts.packet.sip.header.SipHeader;
import io.pkts.packet.sip.header.ViaHeader;

/**
 * @author jonas@jonasborjesson.com
 */
public final class SipResponseImpl extends SipMessageImpl implements SipResponse {

    private CSeqHeader cseq;

    /**
     * @param initialLine
     * @param headers
     * @param payload
     */
    public SipResponseImpl(final Buffer initialLine, final Buffer headers,
            final Buffer payload) {
        super(initialLine, headers, payload);
    }

    public SipResponseImpl(final SipResponseLine initialLine, final Buffer headers,
            final Buffer payload) {
        super(initialLine, headers, payload);
    }

    @Override
    public Buffer getReasonPhrase() {
        return getResponseLine().getReason().slice();
    }

    /**
     * {@inheritDoc}
     * 
     * @throws SipParseException
     */
    @Override
    public Buffer getMethod() throws SipParseException {
        return getCSeqHeader().getMethod();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getStatus() {
        return getResponseLine().getStatusCode();
    }

    @Override
    public SipResponse clone() {
        throw new RuntimeException("Sorry, not implemented right now");
    }

    @Override
    public ViaHeader popViaHeader() throws SipParseException {
        final SipHeader header = popHeader(ViaHeader.NAME);
        if (header instanceof ViaHeader) {
            return (ViaHeader) header;
        }

        if (header == null) {
            return null;
        }


        final Buffer buffer = header.getValue();
        return ViaHeader.frame(buffer);
    }

}
