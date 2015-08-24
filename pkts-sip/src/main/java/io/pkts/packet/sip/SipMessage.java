package io.pkts.packet.sip;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.address.SipURI;
import io.pkts.packet.sip.header.AddressParametersHeader;
import io.pkts.packet.sip.header.CSeqHeader;
import io.pkts.packet.sip.header.CallIdHeader;
import io.pkts.packet.sip.header.ContactHeader;
import io.pkts.packet.sip.header.ContentTypeHeader;
import io.pkts.packet.sip.header.ExpiresHeader;
import io.pkts.packet.sip.header.FromHeader;
import io.pkts.packet.sip.header.MaxForwardsHeader;
import io.pkts.packet.sip.header.RecordRouteHeader;
import io.pkts.packet.sip.header.RouteHeader;
import io.pkts.packet.sip.header.SipHeader;
import io.pkts.packet.sip.header.ToHeader;
import io.pkts.packet.sip.header.ViaHeader;
import io.pkts.packet.sip.impl.SipParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static io.pkts.packet.sip.impl.PreConditions.assertNotEmpty;
import static io.pkts.packet.sip.impl.PreConditions.assertNotNull;

/**
 * Packet representing a SIP message.
 * 
 * @author jonas@jonasborjesson.com
 * 
 */
public interface SipMessage extends Cloneable {

    /**
     * The first line of a sip message, which is either a request or a response
     * line
     * 
     * @return
     */
    Buffer getInitialLine();

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
    SipRequest toRequest() throws ClassCastException;

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
    SipResponse toResponse() throws ClassCastException;

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
     * <li>{@link MaxForwardsHeader}</li>
     * </ul>
     * 
     * @param statusCode
     * @param request
     * @return
     * @throws SipParseException
     *             in case anything goes wrong when parsing out headers from the
     *             {@link SipRequest}
     */
    default SipResponse createResponse(int responseCode) throws SipParseException, ClassCastException {
        return createResponse(responseCode, null);
    }


    SipResponse createResponse(int responseCode, Buffer content) throws SipParseException, ClassCastException;

    /**
     * Check whether this sip message is a response or not
     * 
     * @return
     */
    boolean isResponse();

    /**
     * Check whether this sip message is a request or not
     * 
     * @return
     */
    boolean isRequest();

    /**
     * Returns the content (payload) of the {@link SipMessage} as an
     * {@link Object}. If the {@link ContentTypeHeader} indicates a content type
     * that is known (such as an sdp) then an attempt to parse the content into
     * that type is made. If the payload is unknown then a {@link Buffer}
     * representing the payload will be returned.
     * 
     * @return
     * @throws SipParseException
     *             in case anything goes wrong when trying to frame the content
     *             in any way.
     */
    Object getContent() throws SipParseException;

    /**
     * Get the content as a {@link Buffer}.
     * 
     * @return
     */
    Buffer getRawContent();

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

    void addHeader(SipHeader header) throws SipParseException;

    void addHeaderFirst(SipHeader header) throws SipParseException;

    /**
     * Remove and return the top-most header.
     * 
     * @param headerName the name of the header to pop.
     * @return the removed header or null if there was no such header.
     * @throws SipParseException
     */
    SipHeader popHeader(Buffer headerNme) throws SipParseException;

    /**
     * Set the specified header, which will replace the existing header of the
     * same name. If there are multiple headers of this header, then all "old"
     * ones are removed.
     * 
     * @param header
     */
    void setHeader(SipHeader header) throws SipParseException;

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
     * Get all the Via-headers in this {@link SipMessage}. If this is a request
     * that just was created then this may return an empty list.
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
     * an INVITE or not. Hence, this is NOT to the method to determine whether
     * this is a INVITE Request or not!
     * 
     * @return true if the method of this message is a INVITE, false otherwise.
     * @throws SipParseException
     *             in case the method could not be parsed out of the underlying
     *             buffer.
     */
    boolean isInvite() throws SipParseException;

    /**
     * Convenience method for determining whether the method of this message is an REGISTER or not.
     * 
     * @return true if the method of this message is a REGISTER, false otherwise.
     * @throws SipParseException in case the method could not be parsed out of the underlying
     *         buffer.
     */
    boolean isRegister() throws SipParseException;

    /**
     * Convenience method for determining whether the method of this message is a BYE or not. Hence,
     * this is NOT to the method to determine whether this is a BYE Request or not!
     * 
     * @return true if the method of this message is a BYE, false otherwise.
     * @throws SipParseException in case the method could not be parsed out of the underlying
     *         buffer.
     */
    boolean isBye() throws SipParseException;

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
    boolean isAck() throws SipParseException;

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
    boolean isOptions() throws SipParseException;

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
    boolean isMessage() throws SipParseException;

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
    boolean isInfo() throws SipParseException;

    /**
     * Convenience method for determining whether the method of this message is
     * a CANCEL or not
     * 
     * @return true if the method of this message is a CANCEL, false otherwise.
     * @throws SipParseException
     *             in case the method could not be parsed out of the underlying
     *             buffer.
     */
    boolean isCancel() throws SipParseException;

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
     * Note, the data behind the buffer is shared with the actual
     * {@link SipMessage} so any changes to the {@link Buffer} will affect this
     * {@link SipMessage}. Hence, by changing this buffer directly, you bypass
     * all checks for valid inputs and the end-result of doing so is undefined
     * (most likely you will either blow up at some point or you will end up
     * sending garbage across the network).
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

    default List<SipHeader> getAllHeaders() {
        return new ArrayList<>();
    }

    default Builder copy() {
        return null;
    }

    // Builder copy();

    /**
     *
     */
    interface Builder2 {

        // use this from header, which then will override any From headers
        // there may be from the potential sip message we copied.
        Builder2 withFrom(FromHeader from);
        Builder2 withFrom(FromHeader.Builder from);

        Builder2 onFrom(FromHeader.Builder from);

        FromHeader.Builder getFromBuilder();

        /**
         * After the {@link SipMessage} has been fully built and created the "end result"
         * will be conveyed to the registered function. It is utterly important
         * that the function returns as quickly as possible since the build method
         * will not be able to return until the call to this function has been completed.
         *
         * @param f
         */
        void onMessageCommited(Consumer<SipMessage> f);
    }

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
     *
     */
    interface Builder<T extends SipMessage> {

        /**
         * By default, the following headers will automatically be generated if not explicitly provided:
         *
         * <ul>
         *     <li>{@link ToHeader} - the request-uri will be used to construct the to-header</li>
         *     <li>{@link CSeqHeader} - a new CSeq header will be added where the
         *     method is the same as this message and the sequence number is set to zero</li>
         *     <li>{@link CallIdHeader} - a new random call-id will be added</li>
         *     <li>{@link MaxForwardsHeader} - a max forwards of 70 will be added</li>
         * </ul>
         *
         * but if you don't want that, simply call this method and all the defaults
         * of this builder will be suspended. Of course, if you wish to actually
         * construct a valid {@link SipMessage} you are then responsible for adding
         * the mandatory headers to this builder (unless you don't care of course).
         *
         * @return
         */
        Builder withNoDefaults();

        /**
         * A header can be added to the new {@link SipMessage} in two ways,
         * either by being copied from the {@link SipMessage} used
         * as a template, or the header can be explicitly added through one of the withXXX-methods.
         * Those headers that are copied from the template are subject to the result of the
         * filter function. Those headers that have been explicitly added are not (since if you
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
         */
        Builder filter(Predicate<SipHeader> filter);

        /**
         * Whenever a header is about to be pushed onto the new {@link SipMessage}
         * you have a chance to change the value of that header. You do so
         * by registering a function that takes a {@link SipHeader} as an argument and if you
         * wish to change that header, then simply {@link SipHeader#copy()} it, manipulate
         * it through its builder object and then return that builder.
         *
         * If you wish to leave the header un-touched, then simply return null (or an empty {@link Optional}).
         *
         * Also note that the following headers have explicit "on" methods.
         *
         * <ul>
         *     <li>{@link FromHeader}</li>
         *     <li>{@link ToHeader}</li>
         *     <li>{@link MaxForwardsHeader}</li>
         *     <li>{@link CSeqHeader}</li>
         * </ul>
         *
         * The reason is simply because these are typically manipulated before
         * copying them over to a new request or response (e.g., Max Forwards is decremented,
         * CSeq may increase etc)
         *
         * @param f
         * @return
         */
        Builder onHeader(Function<SipHeader, Optional<SipHeader.Builder>> f);

        /**
         * If this {@link Builder} is based off of a template (as in {@link SipRequest#createResponse(int)}
         * or {@link SipMessage#copy()}) and the {@link FromHeader} was not excluded in the filter step
         * then when the {@link FromHeader} is copied from the template to the new message being
         * built by this {@link Builder} then you have a chance to manipulate it before it gets commited.
         *
         *
         * NEW NEW NEW NEW text and idea
         *
         * When the {@link FromHeader} is about to get added to the new message you have the option
         * of manipulating that {@link FromHeader} by copy it and return a builder (wrapped in an {@link Optional}).
         *
         * Note, this method will ONLY be called in those cases where we don't already have a builder
         * registered through
         *
         *
         * @param f
         * @return If you wish to change the {@link FromHeader} then return an optional with
         * a {@link io.pkts.packet.sip.header.FromHeader.Builder} (where you obviously have made
         * the changes you wish to make).
         */
        Builder onFromHeader(Function<FromHeader, Optional<AddressParametersHeader.Builder<FromHeader>>> f);

        /**
         *
         * @param f
         * @return
         */
        Builder onFromHeaderBuilder(Consumer<AddressParametersHeader.Builder<FromHeader>> f);

        /**
         * Add this {@link FromHeader} to the builder and when it is about to be added to the {@link SipMessage}
         * we are building, the method {@link Builder#onFromHeader(Function)} will
         * be called giving you a chance to manipulate the header.
         *
         * Note, if you already have {@link io.pkts.packet.sip.header.FromHeader.Builder} object then
         * you probably just want to add the builder itself and in that case, the {@link Builder#onFromHeader(Function)}
         * will NOT be called.
         *
         *
         * @param from
         * @return
         */
        Builder withFromHeader(FromHeader from);

        /**
         *
         * @param builder
         * @return
         */
        Builder withFromHeader(AddressParametersHeader.Builder<FromHeader> builder);

        /**
         * Same as {@link io.pkts.packet.sip.SipMessage.Builder#onCopyFromHeader(Function)} but for
         * the {@link ToHeader}.
         *
         * @param f
         * @return
         */
        Builder onToHeader(Function<ToHeader, Optional<ToHeader.Builder>> f);

        /**
         * Same as {@link io.pkts.packet.sip.SipMessage.Builder#onCopyFromHeader(Function)} but for
         * the {@link CSeqHeader}.
         *
         * @param f
         * @return
         */
        Builder onCSeqHeader(Function<CSeqHeader, Optional<CSeqHeader.Builder>> f);

        /**
         * Same as {@link io.pkts.packet.sip.SipMessage.Builder#onCopyFromHeader(Function)} but for
         * the {@link MaxForwardsHeader}.
         *
         * @param f
         * @return
         */
        Builder onMaxForwardsHeader(Function<MaxForwardsHeader, Optional<MaxForwardsHeader.Builder>> f);

        /**
         *
         * @param f
         * @return
         */
        Builder onRequestURI(Function<SipURI, SipURI.Builder> f);

        /**
         *
         * @param f
         */
        void onFrom(Function<SipHeader, SipHeader> f);

        void onTo(Function<ToHeader, Optional<ToHeader.Builder>> f);

        Builder withTopMostVia(ViaHeader.Builder via);

        Builder pushVia(ViaHeader.Builder via);

        Builder withPushedVia(ViaHeader.Builder via);

        Builder pushVia(ViaHeader via);

        void onTopMostVia(Function<ViaHeader, SipHeader> f);

        void onContact(Function<SipHeader, SipHeader> f);

        void onRecordRoute(Function<SipHeader, SipHeader> f);

        T build();

        /**
         * After the {@link SipMessage} has been fully built and created the "end result"
         * will be conveyed to the registered function. It is utterly important
         * that the function returns as quickly as possible since the build method
         * will not be able to return until the call to this function has been completed.
         *
         * @param f
         */
        void onCommit(Consumer<SipMessage> f);
    }

}
