/**
 *
 */
package io.pkts.packet.sip;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.address.SipURI;
import io.pkts.packet.sip.address.URI;
import io.pkts.packet.sip.header.RouteHeader;
import io.pkts.packet.sip.impl.PreConditions;
import io.pkts.packet.sip.impl.SipParser;
import io.pkts.packet.sip.impl.SipRequestBuilder;

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

    /**
     * Pop the top-most route header.
     * 
     * This is a convenience method for calling {@link SipMessage#popHeader(Buffer)}.
     * 
     * @return the top-most {@link RouteHeader} or null if this {@link SipRequest} contained no
     *         {@link RouteHeader}s.
     */
    default RouteHeader popRouteHeader() {
        throw new RuntimeException("No longer allowed because I'm immutable");
    }

    @Override
    SipRequest clone();

    @Override
    default SipRequest toRequest() throws ClassCastException {
        return this;
    }

    @Override
    default boolean isRequest() {
        return true;
    }

    /**
     * Factory method for creating a new INVITE request builder.
     * 
     * @param requestURI the request-uri of the INVITE request.
     * @return a {@link SipRequestBuilder}
     * @throws SipParseException in case the request uri cannot be parsed
     */
    static Builder invite(final String requestURI) throws SipParseException {
        return withMethod(SipParser.INVITE).withRequestURI(requestURI);
    }

    static Builder invite(final URI requestURI) throws SipParseException {
        return withMethod(SipParser.INVITE).withRequestURI(requestURI);
    }

    static Builder ack(final String requestURI) throws SipParseException {
        return withMethod(SipParser.ACK).withRequestURI(requestURI);
    }

    static Builder ack(final URI requestURI) throws SipParseException {
        return withMethod(SipParser.ACK).withRequestURI(requestURI);
    }

    static Builder cancel(final SipURI requestURI) throws SipParseException {
        return withMethod(SipParser.CANCEL).withRequestURI(requestURI);
    }

    static Builder cancel(final String requestURI) throws SipParseException {
        return withMethod(SipParser.CANCEL).withRequestURI(requestURI);
    }

    static Builder bye(final String requestURI) throws SipParseException {
        return withMethod(SipParser.BYE).withRequestURI(requestURI);
    }

    static Builder bye(final URI requestURI) throws SipParseException {
        return withMethod(SipParser.BYE).withRequestURI(requestURI);
    }

    static Builder register(final String requestURI) throws SipParseException {
        return withMethod(SipParser.REGISTER).withRequestURI(requestURI);
    }

    static Builder register(final URI requestURI) throws SipParseException {
        return withMethod(SipParser.REGISTER).withRequestURI(requestURI);
    }

    static Builder update(final String requestURI) throws SipParseException {
        return withMethod(SipParser.UPDATE).withRequestURI(requestURI);
    }

    static Builder update(final URI requestURI) throws SipParseException {
        return withMethod(SipParser.UPDATE).withRequestURI(requestURI);
    }

    static Builder subscribe(final String requestURI) throws SipParseException {
        return withMethod(SipParser.SUBSCRIBE).withRequestURI(requestURI);
    }

    static Builder subscribe(final URI requestURI) throws SipParseException {
        return withMethod(SipParser.SUBSCRIBE).withRequestURI(requestURI);
    }

    static Builder notify(final String requestURI) throws SipParseException {
        return withMethod(SipParser.NOTIFY).withRequestURI(requestURI);
    }

    static Builder notify(final URI requestURI) throws SipParseException {
        return withMethod(SipParser.NOTIFY).withRequestURI(requestURI);
    }

    static Builder publish(final String requestURI) throws SipParseException {
        return withMethod(SipParser.PUBLISH).withRequestURI(requestURI);
    }

    static Builder publish(final URI requestURI) throws SipParseException {
        return withMethod(SipParser.PUBLISH).withRequestURI(requestURI);
    }

    static Builder info(final String requestURI) throws SipParseException {
        return withMethod(SipParser.INFO).withRequestURI(requestURI);
    }

    static Builder info(final URI requestURI) throws SipParseException {
        return withMethod(SipParser.INFO).withRequestURI(requestURI);
    }

    static Builder options(final String requestURI) throws SipParseException {
        return withMethod(SipParser.OPTIONS).withRequestURI(requestURI);
    }

    static Builder options(final URI requestURI) throws SipParseException {
        return withMethod(SipParser.OPTIONS).withRequestURI(requestURI);
    }

    static Builder prack(final String requestURI) throws SipParseException {
        return withMethod(SipParser.PRACK).withRequestURI(requestURI);
    }

    static Builder prack(final URI requestURI) throws SipParseException {
        return withMethod(SipParser.PRACK).withRequestURI(requestURI);
    }

    static Builder refer(final String requestURI) throws SipParseException {
        return withMethod(SipParser.REFER).withRequestURI(requestURI);
    }

    static Builder refer(final URI requestURI) throws SipParseException {
        return withMethod(SipParser.REFER).withRequestURI(requestURI);
    }

    static Builder message(final String requestURI) throws SipParseException {
        return withMethod(SipParser.MESSAGE).withRequestURI(requestURI);
    }

    static Builder message(final URI requestURI) throws SipParseException {
        return withMethod(SipParser.MESSAGE).withRequestURI(requestURI);
    }

    static Builder request(final Buffer method, final String requestURI) throws SipParseException {
        /*
        assertNotEmpty(requestURI, "RequestURI canot be null or the empty string");
        try {
            final SipURI uri = SipURI.frame(Buffers.wrap(requestURI));
            return new Builder(method, uri);
        } catch (IndexOutOfBoundsException | IOException e) {
            throw new SipParseException(0, "Unable to parse the request-uri", e);
        }
        */
        return null;
    }

    /**
     * Convenience method for just replying with a 200 to a request.
     *
     * @return the 200 OK response.
     */
    default SipResponse ok() {
        return createResponse(200).build();
    }

    /**
     * Convenience method for just replying with a 405 Method Not Allowed to a request.
     *
     * @return the 405 response.
     */
    default SipResponse methodNotAllowed() {
        return createResponse(405).build();
    }

    @Override
    Builder copy();

    static Builder request(final Buffer method, final URI requestURI) throws SipParseException {
        // TODO since URI is mutable we have to make a copy so for now just delegate to string method
        // return request(method, requestURI.toString());
        return null;
    }

    static Builder withMethod(final Buffer method) throws SipParseException {
        PreConditions.assertNotEmpty(method, "The method cannot be empty or null");
        return new SipRequestBuilder(method);
    }

    static Builder withMethod(final String method) throws SipParseException {
        PreConditions.assertNotEmpty(method, "The method cannot be empty or null");
        return new SipRequestBuilder(Buffers.wrap(method));
    }

    interface Builder extends SipMessage.Builder<SipRequest> {

        @Override
        default SipMessage.Builder<SipRequest> toSipRequestBuilder() {
            return this;
        }

        @Override
        default boolean isSipRequestBuilder() {
            return true;
        }

        /**
         *
         * @param uri
         * @return
         * @throws SipParseException in case the request uri is null
         */
        Builder withRequestURI(URI uri) throws SipParseException;

        Builder withRequestURI(String uri) throws SipParseException;

        @Override
        SipRequest build();
    }


}
