/**
 * 
 */
package io.pkts.packet.sip.address;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.Transport;
import io.pkts.packet.sip.address.impl.SipURIImpl;
import io.pkts.packet.sip.header.impl.ParametersSupport;
import io.pkts.packet.sip.impl.SipParser;
import io.pkts.packet.sip.impl.SipUserHostInfo;

import java.io.IOException;
import java.util.Optional;

import static io.pkts.packet.sip.impl.PreConditions.assertArgument;
import static io.pkts.packet.sip.impl.PreConditions.assertNotEmpty;
import static io.pkts.packet.sip.impl.PreConditions.assertNotNull;
import static io.pkts.packet.sip.impl.PreConditions.checkIfNotEmpty;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public interface SipURI extends URI {

    /**
     * Get the user portion of this URI.
     * 
     * @return the user portion of this URI or an empty optional if there is no
     *         user portion
     */
    Optional<Buffer> getUser();

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

    @Override
    default SipURI toSipURI() {
        return this;
    }

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
    // void setPort(int port);

    /**
     * Check whether this is a sips URI.
     * 
     * @return true if this indeed is a sips URI, false otherwise.
     */
    boolean isSecure();

    /**
     * Same as {@link #getParameter("transport")}
     * 
     * @return
     * @throws SipParseException
     */
    Optional<Transport> getTransportParam() throws SipParseException;

    /**
     * Get the user parameter. This is the same as {@link #getParameter("user")}
     * 
     * @return
     * @throws SipParseException
     */
    Optional<Buffer> getUserParam() throws SipParseException;

    /**
     * Get the ttl parameter. This is the same as {@link #getParameter("ttl")}
     * 
     * @return the value of the TTL parameter or -1 (negative one) if it is not set.
     * @throws SipParseException
     */
    int getTTLParam() throws SipParseException;

    /**
     * Get the maddr parameter. This is the same as {@link #getParameter("maddr")}
     * 
     * @return
     * @throws SipParseException
     */
    Optional<Buffer> getMAddrParam() throws SipParseException;

    /**
     * Get the method parameter. This is the same as {@link #getParameter("method")}
     * 
     * @return
     * @throws SipParseException
     */
    Optional<Buffer> getMethodParam() throws SipParseException;

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
    Optional<Buffer> getParameter(Buffer name) throws SipParseException, IllegalArgumentException;

    /**
     * Same as {@link #getParameter(Buffer)}.
     * 
     * @param name
     * @return
     * @throws SipParseException in case anything goes wrong while extracting the parameter.
     * @throws IllegalArgumentException in case the name is null.
     */
    Optional<Buffer> getParameter(String name) throws SipParseException, IllegalArgumentException;

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
    // void setParameter(Buffer name, Buffer value) throws SipParseException, IllegalArgumentException;

    /**
     * 
     * @param name
     * @param value
     * @throws SipParseException
     * @throws IllegalArgumentException
     */
    // void setParameter(String name, String value) throws SipParseException, IllegalArgumentException;

    /**
     * 
     * @param name
     * @param value
     * @throws SipParseException
     * @throws IllegalArgumentException
     */
    // void setParameter(Buffer name, int value) throws SipParseException, IllegalArgumentException;

    /**
     * 
     * @param name
     * @param value
     * @throws SipParseException
     * @throws IllegalArgumentException
     */
    // void setParameter(String name, int value) throws SipParseException, IllegalArgumentException;

    /**
     * See rules for comparing URI's in RFC3261 section 19.1.4.
     * 
     * @param o
     * @return
     */
    @Override
    boolean equals(final Object o);


    @Override
    SipURI clone();

    @Override
    Builder copy();

    /**
     * Frame a sip or sips-uri, which according to RFC3261 is:
     * 
     * <pre>
     * SIP-URI          =  "sip:" [ userinfo ] hostport
     *                      uri-parameters [ headers ]
     * SIPS-URI         =  "sips:" [ userinfo ] hostport
     *                      uri-parameters [ headers ]
     * </pre>
     * 
     * Remember though that all these frame-functions will only do a basic
     * verification that all things are ok so just because this function return
     * without an exception doesn't mean that you actually framed a valid URI.
     * Everything is done lazily so things may blow up later.
     * 
     * Also note that this function assumes that someone else has already determined the boundaries for this
     * sip(s)-uri and as such, this function does not expect '<' etc.
     * 
     * @param buffer
     * @return
     * @throws SipParseException
     * @throws IOException
     * @throws IndexOutOfBoundsException
     */
    static SipURI frame(final Buffer buffer) throws SipParseException, IndexOutOfBoundsException, IOException {
        final Buffer original = buffer.slice();
        final int originalIndex = buffer.getReaderIndex();

        // Determine if the URI begins with sip: or sips:, or throw a meaningful error message if not
        final boolean isSips;
        try {
            isSips = SipParser.isSips(buffer);
        } catch (final SipParseException e) {
            throw new SipParseException(e.getErrorOffset() - 1, "SIP URI must start with sip: or sips:");
        }

        // Parse and validate the user/host portion
        final SipUserHostInfo userHost;
        final int userHostStartIndex = buffer.getReaderIndex();
        try {
            userHost = SipParser.consumeUserInfoHostPort(buffer);
        } catch (final SipParseException e) {
            // Re-throw the exception with the same message, but index adjusted for its position within
            // the entire URI.
            throw new SipParseException(userHostStartIndex + e.getErrorOffset(), e.getTemplate(), e);
        }


        // Validate the port number
        final Buffer port = userHost.getPort();
        if (port != null) {
            try {
                port.parseToInt();
            } catch (final NumberFormatException e) {
                throw new SipParseException(0, "The SIP URI had a port but it was not an integer: \"" + port.toString()
                        + "\"");
            }
        }

        return new SipURIImpl(isSips, userHost.getUser(), userHost.getHost(), port, buffer, original);
    }

    static SipURI frame(final String buffer) throws SipParseException, IndexOutOfBoundsException, IOException {
        assertNotEmpty(buffer, "Cannot frame a null or empty string into a SIP URI");
        return SipURI.frame(Buffers.wrap(buffer));
    }


    static Builder with() {
        return new Builder();
    }

    static Builder builder() {
        return new Builder();
    }

    /**
     * Create a new builder based where the user portion has been specified.
     *
     * Note, even though the user portion of a SIP URI isn't mandatory it will be
     * checked for empty or null by this method. The reason for this is simply
     * if you call a method called "withUser" then it is assumed you actually do
     * want the user portion to be present. If this is not what you want, simply
     * call another "withXXX" method.
     *
     * @param user
     * @return
     */
    static Builder withUser(final String user) {
        final Builder builder = new Builder();
        return builder.withUser(user);
    }

    static Builder withUser(final Buffer user) {
        final Builder builder = new Builder();
        return builder.withUser(user);
    }

    static Builder withHost(final String host) {
        final Builder builder = new Builder();
        return builder.withHost(host);
    }

    static Builder withParameters(final Buffer params) {
        final ParametersSupport paramSupport = new ParametersSupport(params);
        return new Builder(paramSupport);
    }

    static Builder withHost(final Buffer host) {
        final Builder builder = new Builder();
        return builder.withHost(host);
    }

    /**
     * Create a new {@link Builder} based on the {@link SipURI}.
     * 
     * <ul>
     *     <li>user</li>
     *     <li>host</li>
     *     <li>port</li>
     *     <li>transport</li>
     *     <li>schema (sip or sips)</li>
     * </ul>
     * 
     * Any other parameter will be ignored.
     * 
     * @param uri
     * @return
     */
    static Builder withTemplate(final SipURI uri) {
        final Builder b = new Builder();
        b.withUser(uri.getUser().orElse(null));
        b.withHost(uri.getHost());
        b.withPort(uri.getPort());
        b.withSecure(uri.isSecure());
        uri.getTransportParam().ifPresent(t -> b.withParameter(SipParser.TRANSPORT, t.toBuffer()));
        return b;
    }

    class Builder extends URI.Builder<SipURI> {

        private Buffer user;
        private Buffer host;
        private Buffer port;
        private boolean isSecure;
        private ParametersSupport paramSupport;

        /**
         * The final size of the buffer. We keep track of this so we at the end
         * can allocate a final buffer to which we will write all the various
         * parts to this single buffer.
         *
         * Note, we start counting at 4 because by default the scheme is "sip", which
         * will be 4 long including the ':' (colon, as in "sip:").
         */
        private int size = 4;

        private Builder() {
            this(new ParametersSupport(null));
        }

        private Builder(final ParametersSupport params) {
            this.paramSupport = params;
        }

        /**
         * 
         * @param user
         * @return
         */
        public Builder withUser(final Buffer user) {
            if (user != null && !user.isEmpty()) {
                // note that we must subtract the previous capacity of the user portion.
                // the +1 is for the '@' sign that we will write into the buffer if there
                // indeed is a user portion in this URI.
                size += user.capacity() + 1 - (this.user != null ? this.user.capacity() + 1 : 0);
                this.user = user.slice();
            } else {
                size -= this.user != null ? this.user.capacity() + 1 : 0;
                this.user = null;
            }
            return this;
        }

        /**
         * 
         * @param user
         * @return
         */
        public Builder withUser(final String user) {
            if (checkIfNotEmpty(user)) {
                withUser(Buffers.wrap(user));
            }
            return this;
        }

        /**
         * Specify the host. The host portion of a {@link SipURI} is mandatory so
         * 
         * @param host
         * @return
         */
        public Builder withHost(final Buffer host) throws SipParseException {
            assertNotNull(host, "Host cannot be null");
            size += host.capacity() - (this.host != null ? this.host.capacity() : 0);
            this.host = host.slice();
            return this;
        }

        public Builder withHost(final String host) throws SipParseException {
            assertNotEmpty(host, "Host cannot be null or the empty string");
            return withHost(Buffers.wrap(host));
        }


        public Builder withTransport(final Transport transport) throws SipParseException {
            assertNotNull(transport, "Transport cannot be null or empty");
            this.paramSupport.setParameter(SipParser.TRANSPORT, transport.toBuffer());
            return this;
        }

        public Builder withTransport(final Buffer transport) throws SipParseException {
            // will check so that it is actually ok
            Transport.of(transport);
            this.paramSupport.setParameter(SipParser.TRANSPORT, transport);
            return this;
        }

        public Builder withTransport(final String transport) throws SipParseException {
            // will check so that it is actually ok
            Transport.of(transport);
            this.paramSupport.setParameter(SipParser.TRANSPORT, Buffers.wrap(transport));
            return this;
        }

        public Builder withParameter(final Buffer name, final Buffer value) throws SipParseException,
        IllegalArgumentException {
            this.paramSupport.setParameter(name, value);
            return this;
        }

        public Builder withParameter(final String name, final String value) throws SipParseException,
        IllegalArgumentException {
            this.paramSupport.setParameter(name, value);
            return this;
        }

        public Builder withParameter(final String name, final int value) throws SipParseException,
                IllegalArgumentException {
            this.paramSupport.setParameter(Buffers.wrap(name), Buffers.wrap(value));
            return this;
        }

        public Builder withParameter(final Buffer name, final int value) throws SipParseException,
                IllegalArgumentException {
            this.paramSupport.setParameter(name, Buffers.wrap(value));
            return this;
        }

        /**
         * Wipe out all parameters. Useful if you e.g. create a copy of a SipURI but you want to
         * remove any potential parameters that may be on the SIP URI.
         *
         * @return
         */
        public Builder withNoParameters() {
            this.paramSupport = new ParametersSupport(null);
            return this;
        }

        /**
         * Mark this {@link SipURI} as a secure sip address, i.e. "sips". Same as calling
         * {@link #secure(true)}
         * 
         * @return
         */
        public Builder secure() {
            if (!this.isSecure) {
                this.isSecure = true;
                ++size;
            }

            return this;
        }

        /**
         * Mark this {@link SipURI} as a secure sip address, i.e. "sips" or as a non-secure, i.e.
         * "sip"
         * 
         * @return
         */
        public Builder withSecure(final boolean secure) {
            if (secure == this.isSecure) {
                return this;
            }

            if (secure && !this.isSecure) {
                ++size;
            } else {
                --size;
            }

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
        public Builder withPort(final int port) throws SipParseException {
            assertArgument(port > 0 || port == -1, "Port must be greater than zero or negative one (use default)");
            if (port == -1) {
                return withPort(null);
            } else {
                return withPort(Buffers.wrap(port));
            }
        }

        /**
         * Specify the port as a buffer.
         * @param port if null the port will be reset.
         * @return
         * @throws SipParseException in case the port is
         */
        public Builder withPort(final Buffer port) throws SipParseException {
            if (port != null) {
                try {
                    assertArgument(port.parseToInt() > 0, "Port must be greater than zero or null (use default)");
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }

            if (this.port != null) {
                size -= this.port.capacity() + 1;
            }

            if (port == null) {
                this.port = null;
            } else {
                this.port = port;
                size += this.port.capacity() + 1;
            }

            return this;
        }

        /**
         * Convenience method for removing the port from this SIP URI. Same as
         * {@link io.pkts.packet.sip.address.SipURI.Builder#withPort(null)}
         *
         * @return
         * @throws SipParseException
         */
        public Builder withNoPort() throws SipParseException {
            return withPort(null);
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

        public Builder useWSS() {
            this.paramSupport.setParameter(SipParser.TRANSPORT, SipParser.WSS);
            return this;
        }

        public boolean hasParameters() {
            return this.paramSupport.hasParameters();
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

            final Buffer scheme = isSecure ? SipParser.SCHEME_SIPS_COLON : SipParser.SCHEME_SIP_COLON;
            final Buffer params = this.paramSupport.toBuffer();
            if (params != null) {
                size += params.capacity();
            }

            final Buffer uri = Buffers.createBuffer(size);
            transferbytes(uri, params);

            return new SipURIImpl(this.isSecure, this.user, this.host, port, params, uri);
        }

        private void transferbytes(final Buffer dst, final Buffer params) {
                if (this.isSecure) {
                    SipParser.SCHEME_SIPS_COLON.getBytes(0, dst);
                } else {
                    SipParser.SCHEME_SIP_COLON.getBytes(0, dst);
                }
                if (this.user != null) {
                    this.user.getBytes(0, dst);
                    dst.write(SipParser.AT);
                }
                this.host.getBytes(0, dst);
                if (this.port != null) {
                    dst.write(SipParser.COLON);
                    this.port.getBytes(0, dst);
                }

                params.getBytes(0, dst);
        }

    }

}
