/**
 * 
 */
package io.pkts.packet.sip;

import static io.pkts.packet.sip.impl.PreConditions.assertNotNull;
import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.address.SipURI;
import io.pkts.packet.sip.header.CSeqHeader;
import io.pkts.packet.sip.header.ContactHeader;
import io.pkts.packet.sip.header.FromHeader;
import io.pkts.packet.sip.header.ToHeader;

/**
 * @author jonas@jonasborjesson.com
 */
public class SipRequestBuilder {

    private static final Buffer INVITE = Buffers.wrap("INVITE");
    private static final Buffer ACK = Buffers.wrap("ACK");

    private final Buffer method;

    private final SipURI requestURI;
    private ToHeader to;
    private FromHeader from;
    private ContactHeader contact;


    /**
     * 
     */
    private SipRequestBuilder(final Buffer method, final SipURI requestURI) {
        this.requestURI = requestURI;
        this.method = method;
    }

    /**
     * Create a new builder for a SIP INVITE request.
     * 
     * @param requestURI the request uri of the INVITE request
     * @return a new {@link SipRequestBuilder}
     */
    public static SipRequestBuilder invite(final String requestURI) {
        final SipRequestBuilder builder = new SipRequestBuilder(INVITE, null);
        return builder;
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

}
