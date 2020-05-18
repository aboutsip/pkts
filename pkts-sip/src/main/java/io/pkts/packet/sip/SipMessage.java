package io.pkts.packet.sip;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.address.SipURI;
import io.pkts.packet.sip.header.AddressParametersHeader;
import io.pkts.packet.sip.header.CSeqHeader;
import io.pkts.packet.sip.header.CallIdHeader;
import io.pkts.packet.sip.header.ContactHeader;
import io.pkts.packet.sip.header.ContentLengthHeader;
import io.pkts.packet.sip.header.ContentTypeHeader;
import io.pkts.packet.sip.header.ExpiresHeader;
import io.pkts.packet.sip.header.FromHeader;
import io.pkts.packet.sip.header.MaxForwardsHeader;
import io.pkts.packet.sip.header.RecordRouteHeader;
import io.pkts.packet.sip.header.RouteHeader;
import io.pkts.packet.sip.header.SipHeader;
import io.pkts.packet.sip.header.ToHeader;
import io.pkts.packet.sip.header.ViaHeader;
import io.pkts.packet.sip.impl.SipInitialLine;
import io.pkts.packet.sip.impl.SipParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static io.pkts.packet.sip.impl.PreConditions.assertNotEmpty;
import static io.pkts.packet.sip.impl.PreConditions.assertNotNull;

/**
 * Packet representing a SIP message.
 * 
 * @author jonas@jonasborjesson.com
 * 
 */
public interface SipMessage extends Cloneable {

    String UNABLE_TO_PARSE_OUT_THE_METHOD_DUE_TO_UNDERLYING_IO_EXCEPTION = "Unable to parse out the method due to underlying IOException";

    /**
     * The first line of a sip message, which is either a request or a response
     * line
     * 
     * @return
     */
    Buffer getInitialLine();

    SipInitialLine initialLine();

    /**
     * Got tired of casting the {@link SipMessage} into a {@link SipRequest} so
     * you can use this method instead. Just a short cut for:
     * 
     * <code>
     *     (SipRequest)sipMessage;
     * </code>
     * 
     * @return this but casted into a {@link SipRequest}
     * @throws ClassCastException
     *             in case this {@link SipMessage} is actually a
     *             {@link SipResponse}.
     */
    default SipRequest toRequest() throws ClassCastException {
        throw new ClassCastException("Unable to cast a " + this.getClass().getName() + " into a " + SipRequest.class.getName());
    }

    /**
     * Got tired of casting the {@link SipMessage} into a {@link SipResponse} so
     * you can use this method instead. Just a short cut for:
     * 
     * <code>
     *     (SipResponse)sipMessage;
     * </code>
     * 
     * @return this but casted into a {@link SipResponse}
     * @throws ClassCastException
     *             in case this {@link SipMessage} is actually a
     *             {@link SipResponse}.
     */
    default SipResponse toResponse() throws ClassCastException {
        throw new ClassCastException("Unable to cast a " + this.getClass().getName() + " into a " + SipResponse.class.getName());
    }

    /**
     * Create a new response based on this {@link SipRequest}. If this
     * {@link SipMessage} is not a {@link SipRequest} then a
     * {@link ClassCastException} will be thrown. Only the mandatory headers
     * from the {@link SipRequest} are copied. Those mandatory headers are:
     * <ul>
     * <li>{@link ToHeader}</li>
     * <li>{@link FromHeader}</li>
     * <li>{@link CallIdHeader}.</li>
     * <li>{@link CSeqHeader}</li>
     * <li>{@link ViaHeader}</li>
     * </ul>
     * 
     * @param responseCode
     * @return
     * @throws SipParseException
     *             in case anything goes wrong when parsing out headers from the
     *             {@link SipRequest}
     */
    default SipResponse.Builder createResponse(final int responseCode) throws SipParseException, ClassCastException {
        return createResponse(responseCode, null);
    }

    default SipResponse.Builder createResponse(int responseCode, Buffer content) throws SipParseException, ClassCastException {
        throw new ClassCastException("Unable to cast this SipMessage into a SipRequest");
    }

    /**
     * Check whether this sip message is a response or not
     * 
     * @return
     */
    default boolean isResponse() {
        return false;
    }

    /**
     * Check whether this sip message is a request or not
     * 
     * @return
     */
    default boolean isRequest() {
        return false;
    }

    default boolean isInviteRequest() {
        return isRequest() && isInvite();
    }

    default boolean isByeRequest() {
        return isRequest() && isBye();
    }

    default boolean isCancelRequest() {
        return isRequest() && isCancel();
    }

    default boolean isRegisterRequest() {
        return isRequest() && isRegister();
    }

    default boolean isOptionsRequest() {
        return isRequest() && isOptions();
    }

    default boolean isInfoRequest() {
        return isRequest() && isInfo();
    }

    default boolean isMessageRequest() {
        return isRequest() && isMessage();
    }

    /**
     * Convenience method for checking whether this an error response is >= 400.
     *
     * @return
     */
    default boolean isError() {
        return isResponse() && toResponse().isError();
    }

    /**
     * Convenience method for checking whether this is a 1xx response or not.
     *
     * @return
     */
    default boolean isProvisional() {
        return isResponse() && toResponse().isProvisional();
    }

    /**
     * Convenience method for checking whether this response is a final response, i.e. any response
     * >= 200.
     *
     * @return
     */
    default boolean isFinal() {
        return isResponse() && toResponse().isFinal();
    }

    /**
     * Convenience method for checking whether this is a 2xx response or not.
     *
     * @return
     */
    default boolean isSuccess() {
        return isResponse() && toResponse().isSuccess();
    }

    /**
     * Convenience method for checking whether this is a 300 - 699. I.e. it's a final non-2xx response.
     *
     * @return
     */
    default boolean isFinalNon2xx() {
        return isFinal() && !isSuccess();
    }

    /**
     * Convenience method for checking whether this is a 3xx response or not.
     *
     * @return
     */
    default boolean isRedirect() {
        return isResponse() && toResponse().isRedirect();
    }

    /**
     * Convenience method for checking whether this is a 4xx response or not.
     *
     * @return
     */
    default boolean isClientError() {
        return isResponse() && toResponse().isClientError();
    }

    /**
     * Convenience method for checking whether this is a 5xx response or not.
     *
     * @return
     */
    default boolean isServerError() {
        return isResponse() && toResponse().isServerError();
    }

    /**
     * Convenience method for checking whether this is a 6xx response or not.
     *
     * @return
     */
    default boolean isGlobalError() {
        return isResponse() && toResponse().isGlobalError();
    }

    /**
     * Convenience method for checking whether this is a 100 Trying response or
     * not.
     *
     * @return
     */
    default boolean is100Trying() {
        return isResponse() && toResponse().is100Trying();
    }

    /**
     * Convenience method for checking whether this is a 180 Ringing response or
     * or a 183 Early Media response.
     *
     * @return true if this response is a 180 or a 183 response, false otherwise
     */
    default boolean isRinging() {
        return isResponse() && toResponse().isRinging();
    }

    /**
     * Convenience method for checking whether this is a 480 Timeout response or
     * not.
     *
     * @return
     */
    default boolean isTimeout() {
        return isResponse() && toResponse().isTimeout();
    }

    /**
     * Get the content as a {@link Buffer}.
     *
     * @return
     */
    Buffer getContent();

    /**
     * Checks whether this {@link SipMessage} is carrying anything in its
     * message body.
     * 
     * @return true if this {@link SipMessage} has a message body, false
     *         otherwise.
     */
    boolean hasContent();

    /**
     * Get the method of this sip message
     * 
     * @return
     */
    Buffer getMethod() throws SipParseException;

    /**
     * Get the header as a buffer
     * 
     * @param headerName
     *            the name of the header we wish to fetch
     * @return the header as a {@link SipHeader} or null if not found
     * @throws SipParseException
     */
    Optional<SipHeader> getHeader(Buffer headerName) throws SipParseException;

    /**
     * Same as {@link #getHeader(Buffers.wrap(keyParameter)}.
     * 
     * @param headerName
     *            the name of the header we wish to fetch
     * @return the header as a {@link SipHeader} or null if not found
     * @throws SipParseException
     */
    Optional<SipHeader> getHeader(String headerName) throws SipParseException;

    /**
     * Get all headers with the given name.
     *
     * @param headerName
     * @return a list of all headers or an empty list if none is found.
     * @throws SipParseException
     */
    List<SipHeader> getHeaders(String headerName) throws SipParseException;

    List<SipHeader> getHeaders(Buffer headerName) throws SipParseException;

    /**
     * Convenience method for fetching the from-header
     * 
     * @return the from header as a buffer
     * @throws SipParseException
     *             TODO
     */
    FromHeader getFromHeader() throws SipParseException;

    /**
     * Convenience method for fetching the to-header
     * 
     * @return the to header as a buffer
     */
    ToHeader getToHeader() throws SipParseException;

    /**
     * Get the top-most {@link ViaHeader} if present. If this is a request that
     * has been sent then there should always be a {@link ViaHeader} present.
     * However, you just created a {@link SipMessage} youself then this method
     * may return null so please check for it.
     * 
     * @return the top-most {@link ViaHeader} or null if there are no
     *         {@link ViaHeader}s on this message just yet.
     * @throws SipParseException
     */
    ViaHeader getViaHeader() throws SipParseException;

    /**
     * Get all the Via-headers in this {@link SipMessage}. If there are no
     * {@link ViaHeader}s then an empty list will be returned.
     *
     * @return
     * @throws SipParseException
     */
    List<ViaHeader> getViaHeaders() throws SipParseException;

    /**
     * 
     * @return
     * @throws SipParseException
     */
    MaxForwardsHeader getMaxForwards() throws SipParseException;

    /**
     * Get the top-most {@link RecordRouteHeader} header if present.
     * 
     * @return the top-most {@link RecordRouteHeader} header or null if there
     *         are no {@link RecordRouteHeader} headers found in this
     *         {@link SipMessage}.
     * @throws SipParseException
     */
    RecordRouteHeader getRecordRouteHeader() throws SipParseException;

    /**
     * Get all the RecordRoute-headers in this {@link SipMessage}. If there are
     * no {@link RecordRouteHeader}s in this {@link SipMessage} then an empty
     * list will be returned.
     * 
     * @return
     * @throws SipParseException
     */
    List<RecordRouteHeader> getRecordRouteHeaders() throws SipParseException;

    /**
     * Get the top-most {@link RouteHeader} header if present.
     * 
     * @return the top-most {@link RouteHeader} header or null if there are no
     *         {@link RouteHeader} headers found in this {@link SipMessage}.
     * @throws SipParseException
     */
    RouteHeader getRouteHeader() throws SipParseException;

    /**
     * Get all the Route-headers in this {@link SipMessage}. If there are no
     * {@link RouteHeader}s in this {@link SipMessage} then an empty list will
     * be returned.
     * 
     * @return
     * @throws SipParseException
     */
    List<RouteHeader> getRouteHeaders() throws SipParseException;

    /**
     * Get the {@link ExpiresHeader}
     * 
     * @return
     * @throws SipParseException
     */
    ExpiresHeader getExpiresHeader() throws SipParseException;

    /**
     * Get the {@link ContactHeader}
     * 
     * @return
     * @throws SipParseException
     */
    ContactHeader getContactHeader() throws SipParseException;

    /**
     * Get the {@link ContentTypeHeader} for this message. If there is no
     * Content-Type header in this SIP message then null will be returned.
     * 
     * @return the {@link ContentTypeHeader} or null if there is none.
     * @throws SipParseException
     */
    ContentTypeHeader getContentTypeHeader() throws SipParseException;

    /**
     * Return the content length. If the header isn't present then zero will
     * be returned which DOES NOT mean that there isn't a body. Remember,
     * this API doesn't enforce any rules regarding what headers you must/must not
     * include in the message, this have to be enforced by business logic.
     *
     * @return the value of the Content-Length header if present or zero if
     * there was no such header (or of course if the Content-Length header was
     * present but actually had the value of zero)
     *
     * @throws SipParseException
     */
    int getContentLength() throws SipParseException;

    /**
     * Convenience method for fetching the call-id-header
     * 
     * @return the call-id header as a buffer
     */
    CallIdHeader getCallIDHeader() throws SipParseException;

    /**
     * Convenience method for fetching the CSeq header
     * 
     * @return
     * @throws SipParseException
     */
    CSeqHeader getCSeqHeader() throws SipParseException;

    /**
     * Convenience method for determining whether the method of this message is
     * an INVITE or not.
     *
     * Note, this method only determined if it is an INVITE, which then could
     * be either a request or a response, which you can check with {@link SipMessage#isRequest()} and
     *  {@link SipMessage#isResponse()}
     *
     * @return true if the method of this message is a INVITE, false otherwise.
     * @throws SipParseException
     *             in case the method could not be parsed out of the underlying
     *             buffer.
     */
    default boolean isInvite() throws SipParseException {
        final Buffer m = getMethod();
        try {
            return m.getByte(0) == 'I' && m.getByte(1) == 'N' && m.getByte(2) == 'V' && m.getByte(3) == 'I'
                    && m.getByte(4) == 'T' && m.getByte(5) == 'E';
        } catch (final IOException e) {
            throw new SipParseException(0, UNABLE_TO_PARSE_OUT_THE_METHOD_DUE_TO_UNDERLYING_IO_EXCEPTION, e);
        }
    }

    /**
     * Convenience method for determining whether the method of this message is an REGISTER or not.
     *
     * @return true if the method of this message is a REGISTER, false otherwise.
     * @throws SipParseException in case the method could not be parsed out of the underlying
     *         buffer.
     */
    default boolean isRegister() throws SipParseException {
        final Buffer m = getMethod();
        try {
            return m.getByte(0) == 'R' && m.getByte(1) == 'E' && m.getByte(2) == 'G' && m.getByte(3) == 'I'
                    && m.getByte(4) == 'S' && m.getByte(5) == 'T' && m.getByte(6) == 'E' && m.getByte(7) == 'R';
        } catch (final IOException e) {
            throw new SipParseException(0, UNABLE_TO_PARSE_OUT_THE_METHOD_DUE_TO_UNDERLYING_IO_EXCEPTION, e);
        }
    }

    /**
     * Convenience method for determining whether the method of this message is a BYE or not. Hence,
     * this is NOT to the method to determine whether this is a BYE Request or not!
     *
     * @return true if the method of this message is a BYE, false otherwise.
     * @throws SipParseException in case the method could not be parsed out of the underlying
     *         buffer.
     */
    default boolean isBye() throws SipParseException {
        final Buffer m = getMethod();
        try {
            return m.getByte(0) == 'B' && m.getByte(1) == 'Y' && m.getByte(2) == 'E';
        } catch (final IOException e) {
            throw new SipParseException(0, UNABLE_TO_PARSE_OUT_THE_METHOD_DUE_TO_UNDERLYING_IO_EXCEPTION, e);
        }
    }

    /**
     * Convenience method for determining whether the method of this message is
     * an ACK or not. Hence, this is NOT to the method to determine whether this
     * is an ACK Request or not!
     *
     * @return true if the method of this message is a ACK, false otherwise.
     * @throws SipParseException
     *             in case the method could not be parsed out of the underlying
     *             buffer.
     */
    default boolean isAck() throws SipParseException {
        final Buffer m = getMethod();
        try {
            return m.getByte(0) == 'A' && m.getByte(1) == 'C' && m.getByte(2) == 'K';
        } catch (final IOException e) {
            throw new SipParseException(0, UNABLE_TO_PARSE_OUT_THE_METHOD_DUE_TO_UNDERLYING_IO_EXCEPTION, e);
        }
    }

    /**
     * Convenience method for determining whether the method of this message is
     * a CANCEL or not
     *
     * @return true if the method of this message is a CANCEL, false otherwise.
     * @throws SipParseException
     *             in case the method could not be parsed out of the underlying
     *             buffer.
     */
    default boolean isCancel() throws SipParseException {
        final Buffer m = getMethod();
        try {
            return m.getByte(0) == 'C' && m.getByte(1) == 'A' && m.getByte(2) == 'N' && m.getByte(3) == 'C'
                    && m.getByte(4) == 'E' && m.getByte(5) == 'L';
        } catch (final IOException e) {
            throw new SipParseException(0, UNABLE_TO_PARSE_OUT_THE_METHOD_DUE_TO_UNDERLYING_IO_EXCEPTION, e);
        }
    }

    /**
     * Convenience method for determining whether the method of this message is
     * a OPTIONS or not. Hence, this is NOT to the method to determine whether
     * this is an OPTIONS Request or not!
     *
     * @return true if the method of this message is a OPTIONS, false otherwise.
     * @throws SipParseException
     *             in case the method could not be parsed out of the underlying
     *             buffer.
     */
    default boolean isOptions() throws SipParseException {
        final Buffer m = getMethod();
        try {
            return m.getByte(0) == 'O' && m.getByte(1) == 'P' && m.getByte(2) == 'T' && m.getByte(3) == 'I'
                    && m.getByte(4) == 'O' && m.getByte(5) == 'N' && m.getByte(6) == 'S';
        } catch (final IOException e) {
            throw new SipParseException(0, UNABLE_TO_PARSE_OUT_THE_METHOD_DUE_TO_UNDERLYING_IO_EXCEPTION, e);
        }
    }


    /**
     * Convenience method for determining whether the method of this message is
     * a MESSAGE or not. Hence, this is NOT to the method to determine whether
     * this is an MESSAGE Request or not!
     *
     * @return true if the method of this message is a MESSAGE, false otherwise.
     * @throws SipParseException
     *             in case the method could not be parsed out of the underlying
     *             buffer.
     */
    default boolean isMessage() throws SipParseException {
        final Buffer m = getMethod();
        try {
            return m.getByte(0) == 'M' && m.getByte(1) == 'E' && m.getByte(2) == 'S' && m.getByte(3) == 'S'
                    && m.getByte(4) == 'A' && m.getByte(5) == 'G' && m.getByte(6) == 'E';
        } catch (final IOException e) {
            throw new SipParseException(0, UNABLE_TO_PARSE_OUT_THE_METHOD_DUE_TO_UNDERLYING_IO_EXCEPTION, e);
        }
    }

    /**
     * Convenience method for determining whether the method of this message is
     * a INFO or not. Hence, this is NOT to the method to determine whether this
     * is an INFO Request or not!
     *
     * @return true if the method of this message is a INFO, false otherwise.
     * @throws SipParseException
     *             in case the method could not be parsed out of the underlying
     *             buffer.
     */
    default boolean isInfo() throws SipParseException {
        final Buffer m = getMethod();
        try {
            return m.getByte(0) == 'I' && m.getByte(1) == 'N' && m.getByte(2) == 'F' && m.getByte(3) == 'O';
        } catch (final IOException e) {
            throw new SipParseException(0, UNABLE_TO_PARSE_OUT_THE_METHOD_DUE_TO_UNDERLYING_IO_EXCEPTION, e);
        }
    }

    /**
     * Checks whether or not this request is considered to be an "initial"
     * request, i.e., a request that does not go within a dialog.
     * 
     * @return
     * @throws SipParseException
     */
    boolean isInitial() throws SipParseException;

    default boolean isSubsequent() throws SipParseException {
        return !isInitial();
    }

    /**
     * <p>
     * <ul>
     * <li>ruri sip version - checks if the SIP version in the request URI is
     * supported, currently only 2.0.</li>
     * <li>ruri scheme - checks if the URI scheme of the request URI is
     * supported (sip[s]|tel[s]) by SIP-router.</li>
     * <li>required headers - checks if the minimum set of required headers to,
     * from, cseq, callid and via is present in the request.</li>
     * <li>via sip version - not working because parser fails already when
     * another version then 2.0 is present.</li>
     * <li>via protocol - not working because parser fails already if an
     * unsupported transport is present.</li>
     * <li>cseq method - checks if the method from the cseq header is equal to
     * the request method.</li>
     * <li>cseq value - checks if the number in the cseq header is a valid
     * unsigned integer.</li>
     * <li>content length - checks if the size of the body matches with the
     * value from the content length header.</li>
     * <li>expires value - checks if the value of the expires header is a valid
     * unsigned integer.</li>
     * <li>proxy require - checks if all items of the proxy require header are
     * present in the list of the extensions from the module parameter
     * proxy_require.</li>
     * 
     * <li>parse uri's - checks if the specified URIs are present and parseable
     * by the SIP-router parsers</li>
     * <li>digest credentials - Check all instances of digest credentials in a
     * message. The test checks whether there are all required digest parameters
     * and have meaningful values.</li>
     * </ul>
     * </p>
     * 
     * <p>
     * This list is taken from <a href=
     * "http://kamailio.org/docs/modules/stable/modules/sanity.html#sanity_check"
     * >Kamailio.org</a>
     * </p>
     * 
     */
    void verify();

    /**
     * Get the {@link Buffer} that is representing this {@link SipMessage}.
     *
     * @return
     */
    Buffer toBuffer();

    /**
     * Perform a deep clone of this SipMessage.
     * 
     * @return
     */
    SipMessage clone();

    /**
     * Frame the supplied buffer into a {@link SipMessage}. No deep analysis of the message will be
     * performed so there is no guarantee that this {@link SipMessage} is actually a well formed
     * message.
     * 
     * @param buffer
     * @return the framed {@link SipMessage}
     */
    static SipMessage frame(final Buffer buffer) throws SipParseException, IOException {
        assertNotNull(buffer);
        return SipParser.frame(buffer);
    }

    /**
     * 
     * @param buffer
     * @return
     * @throws IOException
     */
    static SipMessage frame(final String buffer) throws SipParseException, IOException {
        assertNotEmpty(buffer, "Buffer cannot be null or the empty string");
        return SipParser.frame(Buffers.wrap(buffer));
    }

    static SipMessage frame(final byte[] buffer) throws SipParseException, IOException {
        assertNotNull(buffer, "Byte-array cannot be null");
        return SipParser.frame(Buffers.wrap(buffer));
    }


    default int countNoOfHeaders() {
        return getAllHeaders().size();
    }

    Map<String, List<SipHeader>> getHeaderValues();

    default List<SipHeader> getAllHeaders() {
        // TODO: can't be a default implementation of this. Just doing this while refactoring...
        return new ArrayList<>();
    }

    Builder<? extends SipMessage> copy();

    /**
     * Whenever you create a new {@link SipMessage} you will end up with a {@link Builder}.
     * The pattern of the {@link SipMessage} builders is that you can specify the various
     * headers, request-uri, body etc through the withXXX-methods and then upon build time,
     * the builder will call out to any registered functions (as registered through the onXXX-methods)
     * allowing the application to make any last minute changes.
     *
     * In a real SIP stack there are headers within a {@link SipMessage} that typically needs to be
     * manipulated before being sent out over the network. One such header is the {@link ViaHeader} where
     * the transport layer typically has the responsibility to fill out the ip and port from which the
     * message was sent as well as specify the transport used.
     *
     * The idea is as follows: everything within this SIP library is immutable, which includes {@link SipRequest}
     * and {@link SipResponse}s but if you build a stack, the stack may actually need to change headers
     * before creating the "final" version of the {@link SipMessage} which is then sent out over the network.
     * The typical use case is of course the {@link ViaHeader} where it typically isn't known by the application
     * which protocol, or interface of the stack will be used. This is only known at the time the message
     * is about to be sent out and will (should) be filled out by the transport layer. Therefore, if you build
     * a stack you probably want to pass down a {@link Builder} to be "sent" all the way down to the transport layer
     * which will
     *
     * All callback as registered through the various onXXXX-methods allow for multiple callbacks to be registered.
     * They are called in reverse order from the order of registration.
     *
     * TODO: don't think I will do this anymore. If you add the same header via an of the three methods then
     * TODO: that header will be included three times. I.e., if you call withHeaderBuilder and withHeader and
     * TODO: the same header also exists in the template then it will be included that many times.
     * Order of precedence where the top one "wins" over the others:
     * <ul>
     *     <li>Any header added as a builder object will ALWAYS take precedence</li>
     *     <li>Any header added through a withXXX-method</li>
     *     <li>Any header copied from a "template"</li>
     * </ul>
     *
     * I.e., if you e.g. have added a from-header builder object
     * through {@link Builder#withFromHeader(AddressParametersHeader.Builder)}
     * method then that builder will be used even if this builder is based off
     * another {@link SipMessage} (which then serves as a "template")
     *
     * TODO: add and/or clarify how headers are treated in general, i.e.:
     *
     * A header can be added to the final {@link SipMessage} that is being built via 1 out of 3 ways:
     * <ul>
     *     <li>Either via explicitly calling withXXXBuilder to add a builder object for that header </li>
     *     <li>Or by explicitly calling withXXXHeader</li>
     *     <li>Or by copying an existing header from the {@link SipMessage} we are using as a template
     *     for constructing this new sip message.</li>
     * </ul>
     *
     * No matter how a header is added to the final message (via one of the three ways described above)
     * you will be given the opportunity to change the value of the header by registering a function
     * for manipulating the header just before it gets added. You do so through two methods:
     * <ul>
     *     <li>{@link io.pkts.packet.sip.SipMessage.Builder#onHeader(Function)}</li>
     *     <li>or {@link io.pkts.packet.sip.SipMessage.Builder#onHeaderBuilder(Consumer)}</li>
     * </ul>
     *
     * The first method is called when a header was added without a builder already created from it,
     * which is the case when a header is copied from the template or when you called onXXXHeader(header).
     * The reason for this is unless you actually want to change it, we don't want to waste time on
     * constructing a builder that you are not going to use. However, if you added the header
     * as a builder object then we will of coruse use that builder.
     *
     */
    interface Builder<T extends SipMessage> {

        default boolean isSipRequestBuilder() {
            return false;
        }

        default boolean isSipResponseBuilder() {
            return false;
        }

        default SipMessage.Builder<SipRequest> toSipRequestBuilder() {
            throw new ClassCastException("Cannot cast " + getClass().getName() + " into a SipRequest builder");
        }

        default SipMessage.Builder<SipResponse> toSipResponseBuilder() {
            throw new ClassCastException("Cannot cast " + getClass().getName() + " into a SipResponse builder");
        }

        /**
         * By default, the following headers will automatically be generated if not
         * explicitly provided (note: there is a slight difference between request/response):
         *
         * <ul>
         *     <li>{@link ToHeader} - the request-uri will be used to construct the to-header
         *     in the case of a request. For a response you have to supply it</li>
         *     <li>{@link CSeqHeader} - a new CSeq header will be added where the
         *     method is the same as this message and the sequence number is set to 1</li>
         *     <li>{@link CallIdHeader} - a new random call-id will be added</li>
         *     <li>{@link MaxForwardsHeader} - if we are building a request, a max forwards of 70 will be added</li>
         *     <li>{@link ContentLengthHeader} - Will be added if there is a body
         *     on the message and the length set to the correct length.</li>
         * </ul>
         *
         * but if you don't want that, simply call this method and all the defaults
         * of this builder will be suspended. Of course, if you wish to actually
         * construct a valid {@link SipMessage} you are then responsible for adding
         * the mandatory headers to this builder (unless you don't care of course
         * because perhaps you are building a test tool meant to torture test
         * a SIP server).
         *
         * @return
         */
        Builder<T> withNoDefaults();

        /**
         * A header can be added to the new {@link SipMessage} in two ways,
         * either by being copied from the {@link SipMessage} used
         * as a template, or the header can be explicitly added through one of the withXXX-methods.
         * Those headers that are copied from the template are subject to filtering and
         * those headers that have been explicitly added are not (since if you
         * did add them it is assumed you actually want to include them. If not, why add them
         * in the first place?)
         *
         * Any header, which includes those copied headers that "survived" the filtering
         * step, can be manipulated before it is added to the new SIP message by registering
         * a function with the method {@link Builder#onHeader(Function)}.
         *
         * Note, this API allows you to filter out mandatory headers, such as the {@link ContactHeader} etc,
         * and of course, if you are building a real SIP stack you need to include the mandatory
         * headers but the reason why there are no restrictions imposed by this library is because
         * perhaps you want to build a tool that actually sends bad {@link SipMessage}s? You should be
         * able to do so and therefore, there are no restrictions on how you create your messages.
         * It is up to your stack/application to enforce any rules you see fit.
         *
         * @param filter
         * @throws IllegalStateException in case a filter already had been registered with
         * this builder.
         */
        // Builder<T> filter(Predicate<SipHeader> filter) throws IllegalStateException;

        /**
         * Whenever a header is about to be pushed onto the new {@link SipMessage}
         * you have a chance to change the value of that header. You do so
         * by registering a function that accepts a {@link SipHeader} as an argument and that
         * returns a {@link SipHeader}, which is the header that will be pushed onto the new
         * {@link SipMessage}. If you do not want to include the header, then simply return
         * null and the header will be dropped.
         *
         * If you wish to leave the header un-touched, then simply return it has is.
         *
         * Also note that the following headers have explicit "on" methods (they are considered
         * to be "system" headers):
         *
         * <ul>
         *     <li>{@link FromHeader}</li>
         *     <li>{@link ToHeader}</li>
         *     <li>{@link ContactHeader}</li>
         *     <li>{@link ViaHeader}</li>
         *     <li>{@link RouteHeader}</li>
         *     <li>{@link RecordRouteHeader}</li>
         *     <li>{@link MaxForwardsHeader}</li>
         *     <li>{@link CSeqHeader}</li>
         * </ul>
         *
         * The reason is simply because these are typically manipulated before
         * copying them over to a new request or response (e.g., Max Forwards is decremented,
         * CSeq may increase etc) and therefore it makes life easier if those headers are
         * "down casted" to their specific types.
         *
         *
         * @param f
         * @return
         * @throws IllegalStateException in case a function already had been registered with
         * this builder.
         */
        Builder<T> onHeader(Function<SipHeader, SipHeader> f) throws IllegalStateException;

        /**
         * Adds the header to the list of headers already specified within this builder.
         * The header will be added last to the list of headers. Any already existing
         * headers with the same name will be preserved as is.
         *
         * If there are any headers with the same name as part of the {@link SipMessage}
         * used as a template, then those headers will
         *
         * TODO: this is essentially an "add header" so should it be called that?
         * TODO: and then should there be a set version? Just goes bad with a fluent
         * TODO: naming.
         * @param header
         * @return
         */
        Builder<T> withHeader(SipHeader header);

        Builder<T> withHeaders(List<SipHeader> headers);

        /**
         * Push the header to be the first on the list of existing headers already
         * added to this builder.
         *
         * TODO: naming
         * @return
         */
        Builder<T> withPushHeader(SipHeader header);

        Builder<T> onFromHeader(Consumer<AddressParametersHeader.Builder<FromHeader>> f);

        /**
         * Set the {@link FromHeader} to be used by the new {@link SipMessage}. If there already
         * is a {@link FromHeader} present, either through a template or because this method
         * has been called previously, that value will be overwritten.
         *
         * @param from
         * @return
         */
        Builder<T> withFromHeader(FromHeader from);
        Builder<T> withFromHeader(String from);

        Builder<T> onToHeader(Consumer<AddressParametersHeader.Builder<ToHeader>> f);
        Builder<T> withToHeader(ToHeader to);
        Builder<T> withToHeader(String to);

        Builder<T> onContactHeader(Consumer<AddressParametersHeader.Builder<ContactHeader>> f);
        Builder<T> withContactHeader(ContactHeader contact);

        Builder<T> onCSeqHeader(Consumer<CSeqHeader.Builder> f);
        Builder<T> withCSeqHeader(CSeqHeader cseq);

        Builder<T> onMaxForwardsHeader(Consumer<MaxForwardsHeader.Builder> f);
        Builder<T> withMaxForwardsHeader(MaxForwardsHeader maxForwards);

        Builder<T> withCallIdHeader(CallIdHeader callID);

        /**
         * Called when the top-most Route header is processed. If there are
         * more than one Route header present, the other ones will be
         * processed via the {@link io.pkts.packet.sip.SipMessage.Builder#onRouteHeader(Consumer)}
         *
         * @param f
         * @return
         */
        Builder<T> onTopMostRouteHeader(Consumer<AddressParametersHeader.Builder<RouteHeader>> f);

        /**
         * Called when a Route header is processed (except for the top-most one,
         * then {@link io.pkts.packet.sip.SipMessage.Builder#onTopMostRouteHeader(Consumer)}
         * is called instead)
         *
         * @param f
         * @return
         */
        Builder<T> onRouteHeader(Consumer<AddressParametersHeader.Builder<RouteHeader>> f);

        /**
         * Set a Router header to be used on the message that is being built. This will
         * replace any previously set Route headers. If you wish to add a number of
         * Route headers, use {@link io.pkts.packet.sip.SipMessage.Builder#withRouteHeaders(RouteHeader...)}.
         * If you want to push a Route header to a potentially already existing list
         * of Record Route headers, then use {@link io.pkts.packet.sip.SipMessage.Builder#withTopMostRouteHeader(RouteHeader)}
         *
         * @param route
         * @return
         */
        Builder<T> withRouteHeader(RouteHeader route);

        /**
         * Set a list of Route headers. Any previously Route headers
         * will be replaced by this list.
         *
         * @param routes
         * @return
         */
        Builder<T> withRouteHeaders(RouteHeader ... routes);

        Builder<T> withRouteHeaders(List<RouteHeader> routes);

        /**
         * Push the given Route header to the top of the potential list of existing
         * Route headers.
         *
         * @param route
         * @return
         */
        Builder<T> withTopMostRouteHeader(RouteHeader route);

        /**
         * Pop the top-most route. Note, if you e.g. add a {@link RouteHeader} via the method
         * {@link io.pkts.packet.sip.SipMessage.Builder#withTopMostRouteHeader(RouteHeader)} followed
         * by this method, then the route you just added will be removed again. Order is important!
         *
         * Note: if you actually wanted to know the value of that {@link RouteHeader} then you should
         * really have checked it on the {@link SipMessage} you received and not on the builder
         * object.
         *
         * @return
         */
        Builder<T> withPoppedRoute();

        /**
         * Sometimes you may want to just wipe out all the potential {@link RouteHeader}s
         * that e.g. were automatically copied from another {@link SipRequest} that was
         * used as a template. A common scenario is that you are building a B2BUA acting
         * as the border police to your network and you simply cannot trust incoming requests
         * and as such, you should not honor externally pushed routes, since if you did, an
         * attacker could by-pass your next hop by forcing the request to go to somewhere else.
         *
         * @return
         */
        Builder<T> withNoRoutes();

        // TODO: CSeq, MaxForwards

        /**
         * Called when the top-most Record Route header is processed. If there are
         * more than one Record Route header present, the other ones will be
         * processed via the {@link io.pkts.packet.sip.SipMessage.Builder#onRecordRouteHeader(Consumer)}
         *
         * @param f
         * @return
         */
        Builder<T> onTopMostRecordRouteHeader(Consumer<AddressParametersHeader.Builder<RecordRouteHeader>> f);

        /**
         * Called when a Record-Route header is processed (except for the top-most one,
         * then {@link io.pkts.packet.sip.SipMessage.Builder#onTopMostRecordRouteHeader(Consumer)}
         * is called instead)
         *
         * @param f
         * @return
         */
        Builder<T> onRecordRouteHeader(Consumer<AddressParametersHeader.Builder<RecordRouteHeader>> f);

        /**
         * Set a Record Router header to be used on the message that is being built. This will
         * replace any previously set Record Route headers. If you wish to add a number of
         * Record Route headers, use {@link io.pkts.packet.sip.SipMessage.Builder#withRecordRouteHeaders(RecordRouteHeader...)}.
         * If you want to push a Record Route header to a potentially already existing list
         * of Record Route headers, then use
         *
         * @param recordRoute
         * @return
         */
        Builder<T> withRecordRouteHeader(RecordRouteHeader recordRoute);

        /**
         * Set a list of Record Route headers. Any previously Record Route headers
         * will be replaced by this list.
         *
         * @param recordRoute
         * @return
         */
        Builder<T> withRecordRouteHeaders(RecordRouteHeader ... recordRoute);

        Builder<T> withRecordRouteHeaders(List<RecordRouteHeader> recordRoute);

        /**
         * Push the given Record Route header to the top of the potential list of existing
         * Record Route headers.
         *
         * @param recordRoute
         * @return
         */
        Builder<T> withTopMostRecordRouteHeader(RecordRouteHeader recordRoute);

        /**
         *
         * @param f
         * @return
         */
        Builder<T> onRequestURI(Function<SipURI, SipURI> f);

        // Builder withTopMostVia(ViaHeader.Builder via);
        // Builder pushVia(ViaHeader.Builder via);
        // Builder withPushedVia(ViaHeader.Builder via);
        // Builder pushVia(ViaHeader via);
        // void onTopMostVia(Function<ViaHeader, SipHeader> f);

        /**
         * Called when the top-most Via header is processed. If there are
         * more than one Via header present, the other ones will be
         * processed via the {@link io.pkts.packet.sip.SipMessage.Builder#onViaHeader(Consumer)}
         * consumer
         *
         * @param f
         * @return
         */
        Builder<T> onTopMostViaHeader(Consumer<ViaHeader.Builder> f);

        /**
         * Called when a Via header is processed and the first argument is the
         * index of the Via being processed. The top-most Via header will NEVER
         * be passed to this registered function but rather to the one explicitly
         * meant for processing the top-most via (see {@link Builder#onTopMostViaHeader(Consumer)}.
         *
         * I.e., Let's say you have the following message (request or response, same same):
         *
         * <code>
         *     ...
         *     Via: SIP/2.0/TCP 12.13.14.15;branch=z9hG4bK-asdf
         *     Via: SIP/2.0/UDP 60.61.62.63;branch=z9hG4bK-1234
         *     Via: SIP/2.0/UDP 96.97.98.99;branch=z9hG4bK-wxyz
         *     ...
         * </code>
         *
         * <ul>
         *     <li>When the top-most via header (12.13.14.15) is processed, the function registered with
         *         {@link Builder#onTopMostViaHeader(Consumer)} will be called.
         *     </li>
         *     <li>When the second via is processed (60.61.62.63), the function registered with
         *         {@link Builder#onViaHeader(BiConsumer)} will be called where the index will
         *         be '1' since this is the second via on the list and we of course start counting
         *         at zero.
         *     </li>
         *     <li>When the third via is processed (96.97.98.99), the function registered with
         *         {@link Builder#onViaHeader(BiConsumer)} will be called where the index will
         *         be '2' since this is the third via on the list and so on...
         *     </li>
         * </ul>
         *
         * @param f
         * @return
         */
        Builder<T> onViaHeader(BiConsumer<Integer, ViaHeader.Builder> f);

        /**
         * Add a Via header to be used on the message that is being built. This will
         * replace any previously set Via headers. If you wish to add a number of
         * Via headers, use {@link io.pkts.packet.sip.SipMessage.Builder#withViaHeaders(ViaHeader...)}.
         * If you want to push a Via header to a potentially already existing list
         * of Via headers, then use {@link io.pkts.packet.sip.SipMessage.Builder#withTopMostViaHeader(ViaHeader)}.
         *
         * @param via
         * @return
         */
        Builder<T> withViaHeader(ViaHeader via);

        /**
         * Set a list of Via headers. Any previously Via headers
         * will be replaced by this list.
         *
         * @param vias
         * @return
         */
        Builder<T> withViaHeaders(ViaHeader ... vias);

        /**
         * Set a list of Via headers. Any previously Via headers
         * will be replaced by this list.
         *
         * @param vias
         * @return
         */
        Builder<T> withViaHeaders(List<ViaHeader> vias);

        /**
         * Push the given Via header to the top of the potential list of existing
         * Via headers.
         *
         * @param via
         * @return
         */
        Builder<T> withTopMostViaHeader(ViaHeader via);

        /**
         * Typically the {@link ViaHeader} will have to be filled out by the stack at some
         * later point, which is when the message is about to be sent, so when you create
         * the message you don't have all the details just yet. However, just to create
         * a new Via with bogus information only to be re-written later by registering
         * a function with {@link Builder#onTopMostViaHeader(Consumer)} is rather silly
         * so therefore you can just indicate that you want a new top-most via header
         * but you will fill out all details later.
         *
         * NOTE: if you do NOT register a function to handle this "empty" Via-header
         * through the method {@link Builder#onTopMostViaHeader(Consumer)} things will
         * blow up later with you try to build this message.
         *
         * @return
         */
        Builder<T> withTopMostViaHeader();

        /**
         * Pop the top-most via. Note, if you e.g. add a {@link ViaHeader} through the method
         * {@link io.pkts.packet.sip.SipMessage.Builder#withTopMostViaHeader(ViaHeader)} followed
         * by this method, then the Via you just added will be removed again. Order is important!
         *
         * Note: if you actually wanted to know the value of that {@link ViaHeader} then you should
         * really have checked it on the {@link SipMessage} you received and not on the builder
         * object.
         *
         * @return
         */
        Builder<T> withPoppedVia();

        Builder<T> withBody(Buffer body);

        T build();

        /**
         * After the {@link SipMessage} has been fully built and created the "end result"
         * will be conveyed to the registered function. It is utterly important
         * that the function returns as quickly as possible since the build method
         * will not be able to return until the call to this function has been completed.
         *
         * @param f
         */
        Builder<T> onCommit(Consumer<SipMessage> f);
    }

}
