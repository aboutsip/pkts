/**
 * 
 */
package io.pkts.packet.sip.address;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.buffer.ByteNotFoundException;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.address.impl.AddressImpl;
import io.pkts.packet.sip.impl.SipParser;

import java.io.IOException;

import static io.pkts.packet.sip.impl.PreConditions.assertArgument;
import static io.pkts.packet.sip.impl.PreConditions.assertNotNull;
import static io.pkts.packet.sip.impl.PreConditions.ifNull;

/**
 * Represents an address and like everything else, it is an immutable class.
 *
 * @author jonas@jonasborjesson.com
 */
public interface Address {

    /**
     * Get the display name of this {@link Address} or an empty buffer if it is
     * not set.
     * 
     * @return
     */
    Buffer getDisplayName();

    /**
     * Get the {@link URI} of this {@link Address}.
     * 
     * @return the {@link URI}
     * @throws SipParseException
     */
    URI getURI() throws SipParseException;

    /**
     * Get the {@link Address} as a raw buffer.
     * 
     * @return
     */
    Buffer toBuffer();

    void getBytes(Buffer dst);

    /**
     * An {@link Address} is an immutable object so if you wish to change something
     * you have to create a copy of it.
     *
     * @return
     */
    Builder copy();

    static Address frame(final String buffer) throws SipParseException, IndexOutOfBoundsException, IOException {
        return frame(Buffers.wrap(buffer));
    }

    /**
     *
     * Parses a SIP "name-addr" as defined by RFC3261 section 25.1:
     *
     * <pre>
     * name-addr      =  [ display-name ] LAQUOT addr-spec RAQUOT
     * addr-spec      =  SIP-URI / SIPS-URI / absoluteURI
     * display-name   =  *(token LWS)/ quoted-string
     * </pre>
     *
     * @param buffer
     * @return
     * @throws SipParseException
     * @throws IOException
     */
    static Address frame(final Buffer buffer) throws SipParseException, IndexOutOfBoundsException,
            IOException {
        SipParser.consumeWS(buffer);
        final Buffer original = buffer.slice();

        boolean doubleQuote = false;
        if (buffer.peekByte() == SipParser.DQUOT) {
            doubleQuote = true;
        }

        final Buffer displayName = SipParser.consumeDisplayName(doubleQuote, buffer);
        boolean leftAngleBracket = true;

        // handle the case of an address that looks like:
        // "" <sip:alice@example.com>
        // where the two double quotes is the ones that
        // caused a problem. This checks for that case and
        // consumes any potential white space that is left
        // after the consumption of that weird display name
        if (doubleQuote && displayName.isEmpty()) {
            SipParser.consumeWS(buffer);
        }

        // if no display name, then there may be a '<' present
        // and if so, consume it.
        if (displayName.isEmpty() && buffer.peekByte() == SipParser.LAQUOT) {
            buffer.readByte();
        } else if (!displayName.isEmpty()) {
            // if display name, we DO expect a '<'. Note, there may or may
            // not be white space before the <
            SipParser.consumeWS(buffer);
            SipParser.expect(buffer, SipParser.LAQUOT);
        } else {
            leftAngleBracket = false;
        }

        // if there is no angle bracket then we are not protected
        // by the '<' '>' construct so then we must actually break
        // when we hit a ';' or a '?' since those would then be part
        // of the header and not the URI
        Buffer addrSpec = null;
        if (!leftAngleBracket) {
            try {
                final int index = buffer.indexOf(1024, SipParser.SEMI, SipParser.QUESTIONMARK, SipParser.CR,
                        SipParser.LF);

                if (index >= 0) {
                    final Buffer temp = buffer.readBytes(index - buffer.getReaderIndex());
                    addrSpec = SipParser.consumeAddressSpec(temp);
                } else {
                    // none of the bytes we were looking for was found
                    // so we will just consume the entire buffer
                    addrSpec = SipParser.consumeAddressSpec(buffer);
                }

            } catch (final ByteNotFoundException e) {
                throw new SipParseException(buffer.getReaderIndex(),
                        "Unable to parse the uri (addr-spec) portion of the address");
            }
        } else {
            addrSpec = SipParser.consumeAddressSpec(true, buffer);
        }

        if (addrSpec == null) {
            throw new SipParseException(buffer.getReaderIndex(), "Unable to find the name-addr portion");
        }

        if (displayName.isEmpty() && buffer.hasReadableBytes() && buffer.peekByte() == SipParser.RAQUOT) {
            buffer.readByte();
        } else if (!displayName.isEmpty()) {
            // if display name, we DO expect a '>'
            SipParser.expect(buffer, SipParser.RAQUOT);
        }

        final URI uri = URI.frame(addrSpec);

        return new AddressImpl(original, displayName, uri);
    }

    static Builder builder() {
        return new Builder();
    }

    static Builder withHost(final Buffer host) {
        final Builder builder = new Builder();
        return builder.withHost(assertNotNull(host, "host cannot be null"));
    }

    static Builder withHost(final String host) {
        final Builder builder = new Builder();
        return builder.withHost(assertNotNull(host, "host cannot be null"));
    }

    static Builder withURI(final URI uri) {
        final Builder builder = new Builder();
        builder.withURI(uri);
        return builder;
    }

    class Builder {

        private SipURI.Builder uriBuilder;
        private Buffer displayName;

        private Builder() {
            // left empty intentionally
        }

        /**
         * Use this user for the {@link SipURI} that will be part of this {@link Address}. See
         * {@link #withHost(Buffer)} for more information.
         * 
         * @param user
         * @return
         */
        public Builder withUser(final Buffer user) {
            ensureURIBuilder().withUser(user);
            return this;
        }

        public Builder withUser(final String user) {
            ensureURIBuilder().withUser(user);
            return this;
        }

        /**
         * Use this port for the {@link SipURI} that will be part of this {@link Address}. See
         * {@link #withHost(Buffer)} for more information.
         * 
         * @param port
         * @return
         */
        public Builder withPort(final int port) {
            ensureURIBuilder().withPort(port);
            return this;
        }

        /**
         * Set a parameter on the URI within the {@link Address} object.
         *
         * @param name
         * @param value
         * @return
         * @throws SipParseException
         * @throws IllegalArgumentException
         */
        public Builder withURIParameter(final Buffer name, final Buffer value) throws SipParseException,
                IllegalArgumentException {
            ensureURIBuilder().withParameter(name, value);
            return this;
        }

        /**
         * Set a parameter on the URI within the {@link Address} object.
         *
         * @param name
         * @param value
         * @return
         * @throws SipParseException
         * @throws IllegalArgumentException
         */
        public Builder withURIParameter(final String name, final String value) throws SipParseException,
                IllegalArgumentException {
            ensureURIBuilder().withParameter(name, value);
            return this;
        }

        /**
         * Set a parameter on the URI within the {@link Address} object.
         *
         * @param name
         * @param value
         * @return
         * @throws SipParseException
         * @throws IllegalArgumentException
         */
        public Builder withURIParameter(final String name, final int value) throws SipParseException,
                IllegalArgumentException {
            ensureURIBuilder().withParameter(name, value);
            return this;
        }

        /**
         * Set a parameter on the URI within the {@link Address} object.
         *
         * @param name
         * @param value
         * @return
         * @throws SipParseException
         * @throws IllegalArgumentException
         */
        public Builder withURIParameter(final Buffer name, final int value) throws SipParseException,
                IllegalArgumentException {
            ensureURIBuilder().withParameter(name, value);
            return this;
        }

        /**
         * Wipe out all parameters on the URI within the {@link Address} object.
         *
         * Useful if you e.g. create a copy of a SipURI but you want to remove any potential parameters that may be on the SIP URI.
         *
         * @return
         */
        public Builder withNoParameters() {
            ensureURIBuilder().withNoParameters();
            return this;
        }

        /**
         * Set UDP as the transport on the wrapped SIP URI (assuming this is a SIP URI that this
         * {@link Address} object is indeed wrapping).
         *
         * @return
         */
        public Builder withTransportUDP() {
            ensureURIBuilder().useUDP();
            return this;
        }

        /**
         * Set TCP as the transport on the wrapped SIP URI (assuming this is a SIP URI that this
         * {@link Address} object is indeed wrapping).
         *
         * @return
         */
        public Builder withTransportTCP() {
            ensureURIBuilder().useTCP();
            return this;
        }

        /**
         * Set TLS as the transport on the wrapped SIP URI (assuming this is a SIP URI that this
         * {@link Address} object is indeed wrapping).
         *
         * @return
         */
        public Builder withTransportTLS() {
            ensureURIBuilder().useTLS();
            return this;
        }

        /**
         * Set SCTP as the transport on the wrapped SIP URI (assuming this is a SIP URI that this
         * {@link Address} object is indeed wrapping).
         *
         * @return
         */
        public Builder withTransportSCTP() {
            ensureURIBuilder().useSCTP();
            return this;
        }

        public Builder withTransport(final Buffer transport) throws SipParseException {
            ensureURIBuilder().withTransport(transport);
            return this;
        }

        public Builder withTransport(final String transport) throws SipParseException {
            ensureURIBuilder().withTransport(transport);
            return this;
        }

        /**
         * Set WS as the transport on the wrapped SIP URI (assuming this is a SIP URI that this
         * {@link Address} object is indeed wrapping).
         *
         * @return
         */
        public Builder withTransportWS() {
            ensureURIBuilder().useWS();
            return this;
        }

        /**
         * Set WSS as the transport on the wrapped SIP URI (assuming this is a SIP URI that this
         * {@link Address} object is indeed wrapping).
         *
         * @return
         */
        public Builder withTransportWSS() {
            ensureURIBuilder().useWSS();
            return this;
        }

        /**
         * Use this host for the URI with this {@link Address}. This is a convenient way of creating
         * an {@link Address} with a {@link SipURI} and is the same as:
         * 
         * <pre>
         * SipURI uri = SipURI.with().host(host).build();
         * Address.with(uri);
         * </pre>
         * 
         * NOTE: you cannot specify a host and also specify a URI since those would conflict and an
         * exception will occur at the time you try and {@link #build()} the address.
         * 
         * @param host
         * @return
         */
        public Builder withHost(final Buffer host) {
            ensureURIBuilder().withHost(host);
            return this;
        }

        public Builder withHost(final String host) {
            ensureURIBuilder().withHost(host);
            return this;
        }

        public Builder withDisplayName(final Buffer displayName) {
            this.displayName = ifNull(displayName, Buffers.EMPTY_BUFFER);
            return this;
        }

        public Builder withDisplayName(final String displayName) {
            this.displayName = Buffers.wrap(ifNull(displayName, ""));
            return this;
        }

        private SipURI.Builder ensureURIBuilder() {
            if (uriBuilder == null) {
                uriBuilder = SipURI.builder();
            }
            return uriBuilder;
        }

        public Builder withURI(final URI uri) {
            assertNotNull(uri, "URI cannot be null");
            assertArgument(uri.isSipURI(), "Can only do SIP URIs right now");
            uriBuilder = uri.toSipURI().copy();
            return this;
        }

        public Address build() throws SipParseException {
            final SipURI uri = uriBuilder.build();
            final Buffer uriBuffer = uri.toBuffer();
            int size = uriBuffer.capacity();
            boolean doubleQuoteIt = false;
            final boolean yesDisplayName = displayName != null && !displayName.isEmpty();
            if (yesDisplayName) {
                size += displayName.capacity() + 1; // +1 for space
                if (doubleQuoteIt(displayName)) {
                    doubleQuoteIt = true;
                    size += 2; // because we will surround the display name with double quotes.
                }
            }

            // we will only add the angle brackets if there is a display name and/or there
            // are parameters on the URI. See RFC3261 section
            final boolean yesAngleQuoteIt = yesDisplayName || uriBuilder.hasParameters();
            if (yesAngleQuoteIt) {
                size += 2;
            }

            final Buffer addressBuf = Buffers.createBuffer(size);
            if (yesDisplayName) {
                if (doubleQuoteIt) {
                    addressBuf.write(SipParser.DOUBLE_QOUTE);
                    displayName.getBytes(0, addressBuf);
                    addressBuf.write(SipParser.DOUBLE_QOUTE);
                } else {
                    displayName.getBytes(0, addressBuf);
                }
                addressBuf.write(SipParser.SP);
            }

            if (yesAngleQuoteIt) {
                addressBuf.write(SipParser.LAQUOT);
                uriBuffer.getBytes(0, addressBuf);
                addressBuf.write(SipParser.RAQUOT);
            } else {
                uriBuffer.getBytes(0, addressBuf);
            }

            return new AddressImpl(addressBuf, this.displayName, uri);
        }

        private boolean doubleQuoteIt(final Buffer buffer) {
            try {
                // Don't think a display name would be longer than 1k but if
                // it is and there is a space after that 1k then this fails...
                return buffer.indexOf(1024, SipParser.SP, SipParser.HTAB) != -1
                        && buffer.getByte(0) != SipParser.DOUBLE_QOUTE;
            } catch (final IOException e) {
                // ignore, can't happen
                return false;
            }
        }

    }

}
