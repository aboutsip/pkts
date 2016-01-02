package io.pkts.packet.sip.impl;

import io.pkts.buffer.Buffer;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.SipResponse;
import io.pkts.packet.sip.header.CSeqHeader;
import io.pkts.packet.sip.header.SipHeader;
import io.pkts.packet.sip.header.ViaHeader;

import java.util.List;

/**
 * @author jonas@jonasborjesson.com
 */
public class ImmutableSipResponse extends ImmutableSipMessage implements SipResponse {

    /**
     * @param message            the full immutable buffer which has the entire SIP message in it, including all headers, body
     *                           initial line etc.
     * @param initialLine        the parsed initial line (which is just a reference into the message buffer)
     * @param headers
     * @param indexOfTo
     * @param indexOfFrom
     * @param indexOfCSeq
     * @param indexOfCallId
     * @param indexOfMaxForwards
     * @param indexOfVia
     * @param indexOfRoute
     * @param indexOfRecordRoute
     * @param indexOfContact
     * @param body
     */
    protected ImmutableSipResponse(final Buffer message,
                                   final SipResponseLine initialLine,
                                   final List<SipHeader> headers,
                                   final short indexOfTo,
                                   final short indexOfFrom,
                                   final short indexOfCSeq,
                                   final short indexOfCallId,
                                   final short indexOfMaxForwards,
                                   final short indexOfVia,
                                   final short indexOfRoute,
                                   final short indexOfRecordRoute,
                                   final short indexOfContact,
                                   final Buffer body) {
        super(message, initialLine, headers, indexOfTo,
                indexOfFrom, indexOfCSeq, indexOfCallId,
                indexOfMaxForwards, indexOfVia, indexOfRoute,
                indexOfRecordRoute, indexOfContact, body);
    }

    @Override
    public Buffer getMethod() throws SipParseException {
        final CSeqHeader cseq = getCSeqHeader();
        if (cseq != null) {
            return cseq.getMethod();
        }

        return null;
    }

    @Override
    public SipResponse.Builder copy() {
        final SipResponseLine responseLine = getInitialLineAsObject().toResponseLine();
        final SipResponse.Builder builder = SipResponse.withStatusCode(responseLine.getStatusCode());
        builder.withReasonPhrase(responseLine.getReason());
        builder.withHeaders(this.getAllHeaders());
        builder.withBody(this.getContent());
        return builder;
    }


    @Override
    public int getStatus() {
        return getInitialLineAsObject().toResponseLine().getStatusCode();
    }

    @Override
    public Buffer getReasonPhrase() {
        return getInitialLineAsObject().toResponseLine().getReason();
    }

    @Override
    public ViaHeader popViaHeader() throws SipParseException {
        throw new RuntimeException("No longer allowed");
    }

    @Override
    final public SipResponse clone() {
        // everything is immutable so no real reason to actually clone
        // TODO: remove the clone method alltogether?
        return this;
    }

}
