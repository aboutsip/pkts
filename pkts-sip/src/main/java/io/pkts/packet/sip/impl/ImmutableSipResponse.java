package io.pkts.packet.sip.impl;

import io.pkts.buffer.Buffer;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.SipResponse;
import io.pkts.packet.sip.header.CSeqHeader;
import io.pkts.packet.sip.header.SipHeader;

import java.util.List;
import java.util.Map;

/**
 * @author jonas@jonasborjesson.com
 */
public class ImmutableSipResponse extends ImmutableSipMessage implements SipResponse {

    /**
     * @param message            the full immutable buffer which has the entire SIP message in it, including all headers, body
     *                           initial line etc.
     * @param initialLine        the parsed initial line (which is just a reference into the message buffer)
     * @param headers
     * @param body
     */
    protected ImmutableSipResponse(final Buffer message,
                                   final SipResponseLine initialLine,
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
        super(message,
              initialLine,
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
    final public SipResponse clone() {
        // everything is immutable so no real reason to actually clone
        // TODO: remove the clone method alltogether?
        return this;
    }

}
