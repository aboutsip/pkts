/**
 * 
 */
package io.pkts.packet.sip.address;

import static io.pkts.packet.sip.impl.PreConditions.assertArgument;
import static io.pkts.packet.sip.impl.PreConditions.assertNotEmpty;
import static io.pkts.packet.sip.impl.PreConditions.assertNotNull;
import static io.pkts.packet.sip.impl.PreConditions.checkIfNotEmpty;
import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.address.impl.SipURIImpl;
import io.pkts.packet.sip.header.impl.ParametersSupport;
import io.pkts.packet.sip.impl.SipParser;

/**
 * A builder for building {@link SipURI}s.
 * 
 * @author jonas@jonasborjesson.com
 */
public class SipURIBuilder {

    private static final Buffer udp = Buffers.wrap("udp");
    private static final Buffer tcp = Buffers.wrap("tcp");
    private static final Buffer tls = Buffers.wrap("tls");
    private static final Buffer sctp = Buffers.wrap("sctp");
    private static final Buffer ws = Buffers.wrap("ws");

    private Buffer user;
    private Buffer host;
    private int port = -1;
    private boolean isSecure;
    private final ParametersSupport paramSupport = new ParametersSupport(null);

    private SipURIBuilder() {
        // let empty intentionally
    }

    /**
     * Factory method for creating a new {@link SipURIBuilder}.
     * 
     * Note, when we move over to Java 8 this method would rather be
     * on the actual {@link SipURI} interface itself and everything would
     * look so much nicer. You would then write:
     * <p>
     * <code>
     * SipURI.with().host(someHost).user(someUser).build();
     * </code>
     * </p>
     * 
     * which is must more fluent and intuitive.
     * 
     * @return
     */
    public static SipURIBuilder with() {
        return new SipURIBuilder();
    }

    /**
     * 
     * @param user
     * @return
     */
    public SipURIBuilder user(final Buffer user) {
        if (user != null) {
            this.user = user.slice();
        } else {
            this.user = null;
        }
        return this;
    }

    /**
     * 
     * @param user
     * @return
     */
    public SipURIBuilder user(final String user) {
        if (checkIfNotEmpty(user)) {
            this.user = Buffers.wrap(user);
        }
        return this;
    }

    /**
     * Specify the host. The host portion of a {@link SipURI} is mandatory so
     * 
     * @param host
     * @return
     */
    public SipURIBuilder host(final Buffer host) throws SipParseException {
        assertNotNull(host, "Host cannot be null");
        this.host = host.slice();
        return this;
    }

    public SipURIBuilder host(final String host) throws SipParseException {
        assertNotEmpty(host, "Host cannot be null or the empty string");
        this.host = Buffers.wrap(host);
        return this;
    }

    public SipURIBuilder setParameter(final Buffer name, final Buffer value) throws SipParseException,
    IllegalArgumentException {
        this.paramSupport.setParameter(name, value);
        return this;
    }

    public SipURIBuilder setParameter(final String name, final String value) throws SipParseException,
    IllegalArgumentException {
        this.paramSupport.setParameter(name, value);
        return this;
    }

    /**
     * Mark this {@link SipURI} as a secure sip address, i.e. "sips". Same as calling
     * {@link #secure(true)}
     * 
     * @return
     */
    public SipURIBuilder secure() {
        this.isSecure = true;
        return this;
    }

    /**
     * Mark this {@link SipURI} as a secure sip address, i.e. "sips" or as a non-secure, i.e. "sip"
     * 
     * @return
     */
    public SipURIBuilder secure(final boolean secure) {
        this.isSecure = secure;
        return this;
    }

    /**
     * Set the port. Specify -1 (negative one) means that we should use the default port, as in we
     * won't include the port at all in the resulting buffer. However, if you explicitly set the
     * port to 5060 then it will be included in the final buffer..
     * 
     * @param port
     * @return
     */
    public SipURIBuilder port(final int port) throws SipParseException {
        assertArgument(port > 0 || port == -1, "Port must be greater than zero or negative one (use default)");
        this.port = port;
        return this;
    }

    /**
     * Use UDP as the transport.
     * 
     * @return
     */
    public SipURIBuilder useUDP() {
        this.paramSupport.setParameter(SipParser.TRANSPORT, udp);
        return this;
    }

    /**
     * Use TCP as the transport.
     * 
     * @return
     */
    public SipURIBuilder useTCP() {
        this.paramSupport.setParameter(SipParser.TRANSPORT, tcp);
        return this;
    }

    /**
     * Use SCTP as the transport.
     * 
     * @return
     */
    public SipURIBuilder useSCTP() {
        this.paramSupport.setParameter(SipParser.TRANSPORT, sctp);
        return this;
    }

    /**
     * Use WebSocket as the transport.
     * 
     * @return
     */
    public SipURIBuilder useWS() {
        this.paramSupport.setParameter(SipParser.TRANSPORT, ws);
        return this;
    }

    /**
     * Construct a {@link SipURI}.
     * 
     * @return
     * @throws SipParseException in case mandatory parameters, such as {@link #host(Buffer)} is missing
     * or if other aspects of constructing the {@link SipURI} fails.
     */
    public SipURI build() throws SipParseException {
        assertNotEmpty(this.host, "Host cannot be empty");
        final Buffer port = convertPort();
        return new SipURIImpl(this.isSecure, this.user, this.host, port, this.paramSupport.toBuffer(), null);
    }

    private Buffer convertPort() {
        if (this.port == -1) {
            return null;
        }
        return Buffers.wrap(this.port);
    }

}
