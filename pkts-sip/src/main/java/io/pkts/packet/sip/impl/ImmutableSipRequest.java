package io.pkts.packet.sip.impl;

import io.pkts.buffer.Buffer;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.SipRequest;
import io.pkts.packet.sip.address.URI;
import io.pkts.packet.sip.header.RouteHeader;
import io.pkts.packet.sip.header.SipHeader;

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
        throw new RuntimeException("not implemented yet");
    }

    @Override
    public RouteHeader popRouteHeader() {
        throw new RuntimeException("No longer allowed");
    }

    @Override
    final public SipRequest clone() {
        // everything is immutable so no real reason to actually clone
        // TODO: remove the clone method alltogether?
        return this;
    }

}
