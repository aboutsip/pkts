package io.pkts.packet.sip.impl;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.SipRequest;
import io.pkts.packet.sip.address.SipURI;
import io.pkts.packet.sip.address.URI;
import io.pkts.packet.sip.header.CSeqHeader;
import io.pkts.packet.sip.header.SipHeader;
import io.pkts.packet.sip.header.ToHeader;

import java.io.IOException;
import java.util.List;

/**
 * @author jonas@jonasborjesson.com
 */
public class SipRequestBuilder extends SipMessageBuilder<SipRequest> implements SipRequest.Builder {

    private Buffer method;

    private URI requestURI;

    public SipRequestBuilder(final Buffer method) {
        // TODO: do some research on how many headers an average request has.
        super(15);
        this.method = method;
    }

    @Override
    protected CSeqHeader generateDefaultCSeqHeader() {
        return CSeqHeader.withMethod(method).build();
    }

    @Override
    protected ToHeader generateDefaultToHeader() {
        PreConditions.assertNotNull(requestURI, "No request-uri has been specified so cannot generate a default To-header");
        if (requestURI.isSipURI()) {
            final SipURI sipURI = requestURI.toSipURI();
            return ToHeader.withHost(sipURI.getHost()).withUser(sipURI.getUser()).build();
        } else {
            throw new SipParseException("Not sure how to generate a default To-header off of a " + requestURI.getClass());
        }
    }

    @Override
    final protected boolean isBuildingRequest() {
        return true;
    }

    @Override
    protected SipInitialLine buildInitialLine() throws SipParseException {
        PreConditions.assertNotNull(requestURI, "You must specify the request URI");
        return new SipRequestLine(method, requestURI);
    }

    @Override
    protected SipRequest internalBuild(final Buffer msg,
                                       final SipInitialLine initialLine,
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
        return new ImmutableSipRequest(msg, initialLine.toRequestLine(), headers,
                indexOfTo,
                indexOfFrom,
                indexOfCSeq,
                indexOfCallId,
                indexOfMaxForwards,
                indexOfVia,
                indexOfRoute,
                indexOfRecordRoute,
                indexOfContact,
                body);
    }

    @Override
    public SipRequest.Builder withRequestURI(final URI uri) throws SipParseException {
        PreConditions.assertNotNull(uri, "The request URI cannot be null");
        this.requestURI = uri;
        return this;
    }

    @Override
    public SipRequest.Builder withRequestURI(final String uri) throws SipParseException {
        PreConditions.assertNotEmpty(uri, "The URI cannot be empty string or null");
        try {
            this.requestURI = URI.frame(Buffers.wrap(uri));
        } catch (final IOException e) {
            // shouldn't really be able to happen
            throw new SipParseException("Received IOException when trying to read from the Buffer");
        }
        return this;
    }
}
