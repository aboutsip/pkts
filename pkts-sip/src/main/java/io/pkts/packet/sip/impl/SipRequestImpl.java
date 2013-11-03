/**
 * 
 */
package io.pkts.packet.sip.impl;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.SipRequest;
import io.pkts.packet.sip.SipResponse;
import io.pkts.packet.sip.address.URI;
import io.pkts.packet.sip.header.CSeqHeader;
import io.pkts.packet.sip.header.CallIdHeader;
import io.pkts.packet.sip.header.FromHeader;
import io.pkts.packet.sip.header.MaxForwardsHeader;
import io.pkts.packet.sip.header.SipHeader;
import io.pkts.packet.sip.header.ToHeader;
import io.pkts.packet.sip.header.ViaHeader;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public final class SipRequestImpl extends SipMessageImpl implements SipRequest {

    /**
     * 
     */
    public SipRequestImpl(final Buffer requestLine, final Buffer headers,
            final Buffer payload) {
        super(requestLine, headers, payload);
    }

    /**
     * 
     */
    public SipRequestImpl(final SipRequestLine requestLine, final Buffer headers,
            final Buffer payload) {
        super(requestLine, headers, payload);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer getMethod() {
        return getRequestLine().getMethod();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URI getRequestUri() throws SipParseException {
        return getRequestLine().getRequestUri();
    }

    @Override
    public SipRequest toRequest() throws ClassCastException {
        return this;
    }

    @Override
    public SipRequest clone() {
        final SipRequestLine requestLine = getRequestLine().clone();
        final Buffer headers = this.cloneHeaders();
        final Buffer payload = this.clonePayload();
        return new SipRequestImpl(requestLine, headers, payload);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SipResponse createResponse(final int statusCode) throws SipParseException, ClassCastException {
        final SipResponseLine initialLine = new SipResponseLine(statusCode, Buffers.wrap("OK"));
        final SipResponse response = new SipResponseImpl(initialLine, null, null);
        final CallIdHeader callID = getCallIDHeader();
        final FromHeader from = getFromHeader();
        final ToHeader to = getToHeader();
        final CSeqHeader cseq = getCSeqHeader();

        // TODO: need to extract all via headers
        final ViaHeader via = getViaHeader();
        final SipHeader maxForwards = getHeader(MaxForwardsHeader.NAME);
        response.setHeader(from);
        response.setHeader(to);
        response.setHeader(callID);
        response.setHeader(cseq);
        response.setHeader(via);
        response.setHeader(maxForwards);

        // The TimeStamp header should be there as well but screw it.
        // TODO: need to add any record-route headers

        return response;

    }

}
