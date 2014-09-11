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
 * @author jonas@jonasborjesson.com
 * 
 */
public interface SipURI extends URI {

    // Once we swap over to Java 8 we can do this instead,
    // which looks so much better...
    // public static SipURIBuilder with() {
    // return SipURIBuilder.with();
    // }

    /**
     * Get the user portion of this URI.
     * 
     * @return the user portion of this URI or an empty buffer if there is no
     *         user portion
     */
    Buffer getUser();

    /**
     * Get the host portion of this URI.
     * 
     * @return
     */
    Buffer getHost();

    /**
     * Get the port. If the port isn't set then -1 (negative one) will be
     * returned.
     * 
     * @return
     */
    int getPort();

    /**
     * Set the port.
     * 
     * If you specify -1 (negative one), then no port will be included in the final SIP URI.
     * However, if you explicitly set the port to something, then it will be included in the final
     * result.
     * 
     * @param port
     * @return
     */
    void setPort(int port);

    /**
     * Check whether this is a sips URI.
     * 
     * @return true if this indeed is a sips URI, false otherwise.
     */
    boolean isSecure();

    /**
     * Get the value of the named parameter. If the named parameter is a so-called flag parameter,
     * then the value returned will be an empty {@link Buffer}, which can be checked with
     * {@link Buffer#isEmpty()} or {@link Buffer#capacity()}, which will return zero. As with any
     * empty {@link Buffer}, if you do {@link Buffer#toString()} you will be getting an empty
     * {@link String} back, which would be yet another way to check for a flag parameter.
     * 
     * @param name the name of the parameter we are looking for.
     * @return the value of the named parameter or null if there is no such parameter. If the named
     *         parameter is a flag parameter, then an empty buffer will be returned.
     * @throws SipParseException in case anything goes wrong while extracting the parameter.
     * @throws IllegalArgumentException in case the name is null.
     */
    Buffer getParameter(Buffer name) throws SipParseException, IllegalArgumentException;

    /**
     * Same as {@link #getParameter(Buffer)}.
     * 
     * @param name
     * @return
     * @throws SipParseException in case anything goes wrong while extracting the parameter.
     * @throws IllegalArgumentException in case the name is null.
     */
    Buffer getParameter(String name) throws SipParseException, IllegalArgumentException;

    /**
     * Sets the value of the specified parameter. If there already is a parameter with the same name
     * its value will be overridden.
     * 
     * A value of null or a zero length buffer means that this parameter is a flag parameter
     * 
     * @param name the name of the parameter
     * @param value the value of the parameter or null if you just want to set a flag parameter
     * @return the previous value or null if there were none associated with this parameter name
     * @throws SipParseException in case anything goes wrong when setting the parameter.
     * @throws IllegalArgumentException in case the name is null or empty.
     */
    void setParameter(Buffer name, Buffer value) throws SipParseException, IllegalArgumentException;

    /**
     * 
     * @param name
     * @param value
     * @throws SipParseException
     * @throws IllegalArgumentException
     */
    void setParameter(String name, String value) throws SipParseException, IllegalArgumentException;


    /**
     * Get the entire content of the {@link SipURI} as a {@link Buffer}.
     * 
     * @return
     */
    Buffer toBuffer();

    public static Builder with() {
        return new Builder();
    }

    static class Builder {

        private Buffer user;
        private Buffer host;
        private int port = -1;
        private boolean isSecure;
        private final ParametersSupport paramSupport = new ParametersSupport(null);

        private Builder() {
            // let empty intentionally
        }

        /**
         * 
         * @param user
         * @return
         */
        public Builder user(final Buffer user) {
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
        public Builder user(final String user) {
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
        public Builder host(final Buffer host) throws SipParseException {
            assertNotNull(host, "Host cannot be null");
            this.host = host.slice();
            return this;
        }

        public Builder host(final String host) throws SipParseException {
            assertNotEmpty(host, "Host cannot be null or the empty string");
            this.host = Buffers.wrap(host);
            return this;
        }

        public Builder parameter(final Buffer name, final Buffer value) throws SipParseException,
        IllegalArgumentException {
            this.paramSupport.setParameter(name, value);
            return this;
        }

        public Builder parameter(final String name, final String value) throws SipParseException,
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
        public Builder secure() {
            this.isSecure = true;
            return this;
        }

        /**
         * Mark this {@link SipURI} as a secure sip address, i.e. "sips" or as a non-secure, i.e.
         * "sip"
         * 
         * @return
         */
        public Builder secure(final boolean secure) {
            this.isSecure = secure;
            return this;
        }

        /**
         * Set the port. Specify -1 (negative one) means that we should use the default port, as in
         * we won't include the port at all in the resulting buffer. However, if you explicitly set
         * the port to 5060 then it will be included in the final buffer..
         * 
         * @param port
         * @return
         */
        public Builder port(final int port) throws SipParseException {
            assertArgument(port > 0 || port == -1, "Port must be greater than zero or negative one (use default)");
            this.port = port;
            return this;
        }

        /**
         * Use UDP as the transport.
         * 
         * @return
         */
        public Builder useUDP() {
            this.paramSupport.setParameter(SipParser.TRANSPORT, SipParser.UDP);
            return this;
        }

        /**
         * Use TCP as the transport.
         * 
         * @return
         */
        public Builder useTCP() {
            this.paramSupport.setParameter(SipParser.TRANSPORT, SipParser.TCP);
            return this;
        }

        /**
         * Use TLS as the transport.
         * 
         * @return
         */
        public Builder useTLS() {
            this.paramSupport.setParameter(SipParser.TRANSPORT, SipParser.TLS);
            return this;
        }

        /**
         * Use SCTP as the transport.
         * 
         * @return
         */
        public Builder useSCTP() {
            this.paramSupport.setParameter(SipParser.TRANSPORT, SipParser.SCTP);
            return this;
        }

        /**
         * Use WebSocket as the transport.
         * 
         * @return
         */
        public Builder useWS() {
            this.paramSupport.setParameter(SipParser.TRANSPORT, SipParser.WS);
            return this;
        }

        /**
         * Construct a {@link SipURI}.
         * 
         * @return
         * @throws SipParseException in case mandatory parameters, such as {@link #host(Buffer)} is
         *         missing or if other aspects of constructing the {@link SipURI} fails.
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

}
