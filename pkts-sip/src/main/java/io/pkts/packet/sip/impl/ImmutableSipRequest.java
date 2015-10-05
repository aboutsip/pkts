package io.pkts.packet.sip.impl;

import io.pkts.buffer.Buffer;
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

import java.util.List;

/**
 * @author jonas@jonasborjesson.com
 */
public class ImmutableSipRequest extends ImmutableSipMessage implements SipRequest {


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
    protected ImmutableSipRequest(final Buffer message,
                                  final SipRequestLine initialLine,
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
    public URI getRequestUri() throws SipParseException {
        return getInitialLineAsObject().toRequestLine().getRequestUri();
    }

    @Override
    public Buffer getMethod() throws SipParseException {
        return getInitialLineAsObject().toRequestLine().getMethod();
    }

    @Override
    public SipRequest.Builder copy() {
        final SipRequest.Builder builder = SipRequest.withMethod(getMethod()).withRequestURI(getRequestUri());
        builder.withHeaders(getAllHeaders());
        builder.withBody(getRawContent());
        return builder;
    }

    @Override
    public SipResponse.Builder createResponse(final int responseCode, final Buffer content) throws SipParseException, ClassCastException {
        final CallIdHeader callID = getCallIDHeader();
        final FromHeader from = getFromHeader();
        final ToHeader to = getToHeader();
        final CSeqHeader cseq = getCSeqHeader();
        final MaxForwardsHeader max = getMaxForwards();

        final SipResponse.Builder builder = SipResponse.withStatusCode(responseCode);
        builder.withFromHeader(from);
        builder.withToHeader(to);
        builder.withCSeqHeader(cseq);
        builder.withMaxForwardsHeader(max);
        builder.withCallIdHeader(callID);

        builder.withViaHeaders(getViaHeaders());

        // TODO: allow for a List of RR headers
        builder.withRecordRouteHeaders(getRecordRouteHeaders());

        // TODO: allow for a list of route headers
        // builder.withRouteHeaders(getRouteheaders());

        return builder;
    }


    @Override
    final public SipRequest clone() {
        // everything is immutable so no real reason to actually clone
        // TODO: remove the clone method alltogether?
        return this;
    }

}
