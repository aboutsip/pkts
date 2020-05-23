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
import java.util.Map;
import java.util.function.Function;





/**
 * @author jonas@jonasborjesson.com
 */
public class SipRequestBuilder extends SipMessageBuilder<SipRequest> implements SipRequest.Builder {

    private final Buffer method;

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
            return ToHeader.withHost(sipURI.getHost()).withUser(sipURI.getUser().orElse(null)).build();
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
        final Function<SipURI, SipURI> f = getRequestURIFunction();
        URI finalURI = requestURI;
        if (finalURI.isSipURI() && f != null) {
            try {
                finalURI = f.apply(finalURI.toSipURI());
            } catch (final Exception e) {
                throw new SipParseException(0,
                                            "Unable to construct request URI due exception from registered function", e);
            }
        }
        return new SipRequestLine(method, finalURI);
    }

    @Override
    protected SipRequest internalBuild(final Buffer msg,
                                       final SipInitialLine initialLine,
                                       final Map<String, List<SipHeader>> headers,
                                       final SipHeader toHeader,
                                       final SipHeader fromHeader,
                                       final SipHeader cSeqHeader,
                                       final SipHeader callIdHeader,
                                       final SipHeader maxForwardsHeader,
                                       final SipHeader viaHeader,
                                       final SipHeader routeHeader,
                                       final SipHeader recordRouteHeader,
                                       final SipHeader contactHeader,
                                       final Buffer body) {

        return new ImmutableSipRequest(msg,
                                       initialLine.toRequestLine(),
                                       headers,
                                       toHeader,
                                       fromHeader,
                                       cSeqHeader,
                                       callIdHeader,
                                       maxForwardsHeader,
                                       viaHeader,
                                       routeHeader,
                                       recordRouteHeader,
                                       contactHeader,
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
