/**
 * 
 */
package io.pkts.packet.sip;

import static io.pkts.packet.sip.impl.PreConditions.assertNotEmpty;
import static io.pkts.packet.sip.impl.PreConditions.assertNotNull;
import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.address.SipURI;
import io.pkts.packet.sip.address.URI;
import io.pkts.packet.sip.address.impl.SipURIImpl;
import io.pkts.packet.sip.header.CSeqHeader;
import io.pkts.packet.sip.header.ContactHeader;
import io.pkts.packet.sip.header.FromHeader;
import io.pkts.packet.sip.header.ToHeader;
import io.pkts.packet.sip.header.ViaHeader;
import io.pkts.packet.sip.impl.SipRequestImpl;
import io.pkts.packet.sip.impl.SipRequestLine;

import java.io.IOException;

/**
 * @author jonas@jonasborjesson.com
 */
public interface SipRequest extends SipMessage {

    /**
     * Get the request uri of the sip request
     * 
     * @return
     */
    URI getRequestUri() throws SipParseException;

    @Override
    SipRequest clone();

    /**
     * Factory method for creating a new INVITE request builder.
     * 
     * @param requestURI the request-uri of the INVITE request.
     * @return a {@link SipRequestBuilder}
     * @throws SipParseException in case the request uri cannot be parsed
     */
    static SipRequestBuilder invite(final String requestURI) throws SipParseException {
        assertNotEmpty(requestURI, "RequestURI canot be null or the empty string");
        try {
            final SipURI uri = SipURIImpl.frame(Buffers.wrap(requestURI));
            return new SipRequestBuilder(SipRequestBuilder.INVITE, uri);
        } catch (IndexOutOfBoundsException | IOException e) {
            throw new SipParseException(0, "Unable to parse the request-uri", e);
        }
    }

    public static class SipRequestBuilder {

        private static final Buffer INVITE = Buffers.wrap("INVITE");
        private static final Buffer ACK = Buffers.wrap("ACK");

        private final Buffer method;

        private final SipURI requestURI;

        private ToHeader to;
        private FromHeader from;
        private ContactHeader contact;
        private CSeqHeader cseq;

        /**
         * 
         */
        private SipRequestBuilder(final Buffer method, final SipURI requestURI) {
            this.requestURI = requestURI;
            this.method = method;
        }

        public SipRequestBuilder to(final ToHeader to) {
            this.to = assertNotNull(to, "The To-header cannot be null");
            return this;
        }

        public SipRequestBuilder from(final FromHeader from) {
            this.from = assertNotNull(from, "The From-header cannot be null");
            return this;
        }

        public SipRequestBuilder cseq(final CSeqHeader cseq) {
            this.cseq = assertNotNull(cseq, "The CSeq-header cannot be null");
            return this;
        }

        /**
         * Build a new {@link SipRequest}. The only mandatory value is the request-uri and the
         * From-address. CSeq will be set to a default value and the {@link ContactHeader} can be
         * specified later and is typically manipulated by the transport layer when the message is
         * about to be sent, which is also true for the {@link ViaHeader}.
         * 
         * @return
         * @throws SipParseException
         */
        public SipRequest build() throws SipParseException {
            assertNotNull(from, "The From-header has not been specified");
            final SipRequestLine initialLine = new SipRequestLine(method, requestURI);
            final SipRequest request = new SipRequestImpl(initialLine, null, null);
            request.setHeader(getToHeader());
            request.setHeader(from);
            request.setHeader(getCSeq());
            return request;
        }

        private CSeqHeader getCSeq() {
            if (this.cseq == null) {

            }
            return this.cseq;
        }

        /**
         * Get the To-header but if the user hasn't explicitly speficied one then base it off of the
         * request uri.
         * 
         * @param requestURI
         * @return
         */
        private ToHeader getToHeader() {
            if (this.to == null) {
                final Buffer user = this.requestURI.getUser();
                final Buffer host = this.requestURI.getHost();
                this.to = ToHeader.with().user(user).host(host).build();
            }
            return this.to;
        }

    }

}
