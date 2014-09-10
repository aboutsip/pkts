/**
 * 
 */
package io.pkts.packet.sip;

import static io.pkts.packet.sip.impl.PreConditions.assertNotNull;
import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.address.SipURI;
import io.pkts.packet.sip.address.impl.SipURIImpl;
import io.pkts.packet.sip.header.CSeqHeader;
import io.pkts.packet.sip.header.ContactHeader;
import io.pkts.packet.sip.header.FromHeader;
import io.pkts.packet.sip.header.ToHeader;

import java.io.IOException;

/**
 * @author jonas@jonasborjesson.com
 */
public class SipRequestBuilder {

    private static final Buffer INVITE = Buffers.wrap("INVITE");
    private static final Buffer ACK = Buffers.wrap("ACK");

    private final Buffer method;

    /**
     * The user can pass in either a fully constructed request uri or just a raw buffer, which then
     * later will be parsed to a {@link SipURI}.
     */
    private final SipURI requestURI;
    private final Buffer requestURIBuffer;

    private ToHeader to;
    private FromHeader from;
    private ContactHeader contact;


    /**
     * 
     */
    private SipRequestBuilder(final Buffer method, final SipURI requestURI) {
        this.requestURI = requestURI;
        this.requestURIBuffer = null;
        this.method = method;
    }

    private SipRequestBuilder(final Buffer method, final Buffer requestURI) {
        this.requestURI = null;
        this.requestURIBuffer = requestURI;
        this.method = method;
    }

    /**
     * Create a new builder for a SIP INVITE request.
     * 
     * @param requestURI the request uri of the INVITE request
     * @return a new {@link SipRequestBuilder}
     * @throws SipParseException if we are unable to parse the request URI.
     */
    public static SipRequestBuilder invite(final String requestURI) {
        return new SipRequestBuilder(INVITE, Buffers.wrap(requestURI));
    }

    /**
     * Create a new builder for a SIP INVITE request.
     * 
     * @param requestURI the request uri of the INVITE request
     * @return a new {@link SipRequestBuilder}
     */
    public static SipRequestBuilder invite(final SipURI requestURI) {
        return new SipRequestBuilder(INVITE, assertNotNull(requestURI, "Request URI cannot be null"));
    }

    /**
     * Create a new builder for a SIP ACK request based off of a {@link SipResponse}.
     * 
     * @param requestURI the request uri of the INVITE request
     * @return a new {@link SipRequestBuilder}
     */
    public static SipRequestBuilder ack(final SipResponse response) {
        // return new SipRequestBuilder(INVITE, assertNotNull(response,
        // "Request URI cannot be null"));
        return null;
    }

    public SipRequestBuilder to(final ToHeader to) {
        return this;
    }

    public SipRequestBuilder from(final FromHeader from) {
        return this;
    }

    public SipRequestBuilder cseq(final CSeqHeader cseq) {
        return this;
    }

    public SipRequest build() throws SipParseException {
        final SipURI requestURI = getRequestURI();
        return null;

    }

    /**
     * Get the To-header but if the user hasn't explicitly speficied one then base it off of the
     * request uri.
     * 
     * @param requestURI
     * @return
     */
    private ToHeader getToHeader(final SipURI requestURI) {
        if (this.to != null) {
            return this.to;
        }

        // final ToHeaderImpl;
        return null;

    }

    private SipURI getRequestURI() throws SipParseException {
        try {
            return this.requestURI != null ? this.requestURI : SipURIImpl.frame(this.requestURIBuffer);
        } catch (IndexOutOfBoundsException | IOException e) {
            throw new SipParseException(0, "Unable to parse requestURI " + e.getMessage(), e);
        } catch (final SipParseException e) {
            throw new SipParseException(e.getErrorOffset(), "Unable to parse requestURI " + e.getMessage(), e);
        }
    }

}
