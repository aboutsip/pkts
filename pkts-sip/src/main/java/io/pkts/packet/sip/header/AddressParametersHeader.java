/**
 * 
 */
package io.pkts.packet.sip.header;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.address.Address;
import io.pkts.packet.sip.address.SipURI;
import io.pkts.packet.sip.header.impl.AddressParametersHeaderImpl;
import io.pkts.packet.sip.header.impl.ParametersSupport;
import io.pkts.packet.sip.impl.SipParser;

import java.io.IOException;
import java.util.UUID;

import static io.pkts.packet.sip.impl.PreConditions.assertNotEmpty;
import static io.pkts.packet.sip.impl.PreConditions.assertNotNull;

/**
 * @author jonas@jonasborjesson.com
 */
public interface AddressParametersHeader extends SipHeader, HeaderAddress, Parameters{

    /**
     * Frame the value as a {@link AddressParametersHeaderImpl}. This method assumes that you have
     * already parsed out the actual header name, e.g. "To: ". Also, this method assumes that a
     * message framer (or similar) has framed the buffer that is being passed in to us to only
     * contain this header and nothing else.
     * 
     * Note, as with all the frame-methods on all headers/messages/whatever, they do not do any
     * validation that the information is actually correct. This method will simply only try and
     * validate just enough to get the framing done.
     * 
     * @param value
     * @return an array where the first object is a {@link Address} object and the second is a
     *         {@link Buffer} with all the parameters.
     * @throws SipParseException in case anything goes wrong while parsing.
     */
    static Object[] frame(final Buffer buffer) throws SipParseException {
        try {
            final Address address = Address.frame(buffer);
            // we assume that the passed in buffer ONLY contains
            // this header and nothing else. Therefore, there are only
            // header parameters left after we have consumed the address
            // portion.
            Buffer params = null;
            if (buffer.hasReadableBytes()) {
                params = buffer.slice();
            }
            return new Object[] {address, params};
        } catch (final IndexOutOfBoundsException e) {
            throw new SipParseException(buffer.getReaderIndex(),
                    "Unable to process the value due to a IndexOutOfBoundsException", e);
        } catch (final IOException e) {
            throw new SipParseException(buffer.getReaderIndex(),
                    "Unable to process the To-header to due an IOException");
        }
    }

    static <T> Builder<AddressParametersHeader> with(final Buffer headerName)
            throws SipParseException {
        assertNotEmpty(headerName, "The name of the header cannot be null or the empty buffer");
        return new Builder<AddressParametersHeader>(headerName);
    }

    @Override
    Builder copy();

    default boolean isAddressParametersHeader() {
        return true;
    }

    default AddressParametersHeader toAddressParametersHeader() throws ClassCastException {
        return this;
    }

    class Builder<T extends AddressParametersHeader> implements SipHeader.Builder<T> {

        private final Buffer name;

        private Address.Builder addressBuilder;

        private Buffer value;

        /**
         * Note these are the header parameters and are not to be confused with any URI parameters
         * that are "attached" to the URI within the address object.
         */
        private ParametersSupport paramSupport;

        protected Builder(final Buffer name) {
            this(name, new ParametersSupport(null));
        }

        protected Builder(final Buffer name, final ParametersSupport params) {
            this.name = name;
            this.paramSupport = params;
        }


        public final Builder<T> withValue(final Buffer buffer) {
            throw new RuntimeException("Not implemented yet");
            // Address.frame(buffer).copy();
            // return this;
        }

        public final Builder<T> withPort(final int port) {
            ensureBuilder().withPort(port);
            return this;
        }

        /**
         * Set the user portion of the {@link ToHeader}. Since the user portion may in fact be null
         * (or empty), any value is accepted but of course, a value of null or empty will lead to no
         * user portion of the SIP-URI within the header.
         * 
         * @param user
         * @return
         */
        public final Builder<T> withUser(final Buffer user) {
            ensureBuilder().withUser(user);
            return this;
        }

        public final Builder<T> withUser(final String user) {
            ensureBuilder().withUser(user);
            return this;
        }

        public final Builder<T> withDisplayName(final String displayName) {
            ensureBuilder().withDisplayName(displayName);
            return this;
        }

        public final Builder<T> withDisplayName(final Buffer displayName) {
            ensureBuilder().withDisplayName(displayName);
            return this;
        }

        /**
         * Use this host for the ToHeader.
         * 
         * NOTE: you can only specify either an address or a host and user but not both. If you do,
         * an exception will occur at the time you try and {@link #build()} the header.
         * 
         * @param host
         * @return
         */
        public final Builder<T> withHost(final Buffer host) {
            ensureBuilder().withHost(host);
            return this;
        }

        public final Builder<T> withHost(final String host) {
            ensureBuilder().withHost(host);
            return this;
        }

        /**
         * Convenience method for removing the tag paramter.
         *
         * @return
         * @throws SipParseException
         * @throws IllegalArgumentException
         */
        public Builder<T> withNoTag() throws SipParseException,
                IllegalArgumentException {
            this.paramSupport.removeParameter(SipParser.TAG);
            return this;
        }

        /**
         * Convenience method for setting the tag parameter.
         *
         * @param value
         * @return
         * @throws SipParseException
         * @throws IllegalArgumentException
         */
        public Builder<T> withTag(final Buffer value) throws SipParseException,
                IllegalArgumentException {
            assertNotEmpty(value, "The value of the tag parameter cannot be null or the empty buffer");
            this.paramSupport.setParameter(SipParser.TAG, value);
            return this;
        }

        /**
         * Convenience method for setting the tag parameter.
         *
         * @param value
         * @return
         * @throws SipParseException
         * @throws IllegalArgumentException
         */
        public Builder<T> withTag(final String value) throws SipParseException,
                IllegalArgumentException {
            assertNotEmpty(value, "The value of the tag parameter cannot be null or the empty string");
            this.paramSupport.setParameter(SipParser.TAG, Buffers.wrap(value));
            return this;
        }

        /**
         * Convenience method for setting the value of the tag parameter to a default
         * generated value.
         *
         * @return
         * @throws SipParseException
         * @throws IllegalArgumentException
         */
        public Builder<T> withDefaultTag() throws SipParseException,
                IllegalArgumentException {
            this.paramSupport.setParameter(SipParser.TAG, Buffers.wrap(UUID.randomUUID().toString()));
            return this;
        }

        /**
         * Set a parameter on the header.
         * 
         * NOTE: if you want to set a parameter on the URI you need to use the method
         * {@link #withUriParameter(Buffer, Buffer)}.
         * 
         * @param name
         * @param value
         * @return
         * @throws SipParseException
         * @throws IllegalArgumentException
         */
        public Builder<T> withParameter(final Buffer name, final Buffer value) throws SipParseException,
        IllegalArgumentException {
            this.paramSupport.setParameter(name, value);
            return this;
        }

        public Builder<T> withParameter(final String name, final String value) throws SipParseException,
        IllegalArgumentException {
            this.paramSupport.setParameter(name, value);
            return this;
        }

        /**
         * Set a bunch of parameters at the same time.
         *
         * WARNING! This one will wipe out any previously set parameters so be sure to call
         * this one FIRST before any other calls to withParameter...
         *
         * @param params
         * @return
         */
        public Builder<T> withParameters(final Buffer params) {
            this.paramSupport = new ParametersSupport(params);
            return this;
        }

        /**
         * Remove all header parameters.
         *
         * @return
         */
        public Builder<T> withNoParameters() {
            this.paramSupport = new ParametersSupport();
            return this;
        }

        /**
         * Set a parameter on the underlying {@link SipURI}.
         * 
         * @param name
         * @param value
         * @return
         * @throws SipParseException
         * @throws IllegalArgumentException
         */
        public Builder<T> withUriParameter(final Buffer name, final Buffer value) throws SipParseException,
        IllegalArgumentException {
            ensureBuilder().withURIParameter(name, value);
            return this;
        }

        /**
         * Set a parameter on the underlying {@link SipURI}.
         * 
         * @param name
         * @param value
         * @return
         * @throws SipParseException
         * @throws IllegalArgumentException
         */
        public Builder<T> withUriParameter(final String name, final String value) throws SipParseException,
        IllegalArgumentException {
            ensureBuilder().withURIParameter(name, value);
            return this;
        }

        public Builder<T> withTransport(final Buffer transport) throws SipParseException {
            ensureBuilder().withTransport(transport);
            return this;
        }

        public Builder<T> withTransport(final String transport) throws SipParseException {
            ensureBuilder().withTransport(transport);
            return this;
        }

        /**
         * Set the transport parameter on the underlying {@link SipURI} to be "tcp".
         * 
         * @return
         * @throws SipParseException
         */
        public Builder<T> withTransportTCP() throws SipParseException {
            ensureBuilder().withTransportTCP();
            return this;
        }

        /**
         * Set the transport parameter on the underlying {@link SipURI} to be "udp".
         * 
         * @return
         * @throws SipParseException
         */
        public Builder<T> withTransportUDP() throws SipParseException {
            ensureBuilder().withTransportUDP();
            return this;
        }

        /**
         * Set the transport parameter on the underlying {@link SipURI} to be "tls".
         * 
         * @return
         * @throws SipParseException
         */
        public Builder<T> withTransportTLS() throws SipParseException {
            ensureBuilder().withTransportTLS();
            return this;
        }

        /**
         * Set the transport parameter on the underlying {@link SipURI} to be "sctp".
         * 
         * @return
         * @throws SipParseException
         */
        public Builder<T> withTransportSCTP() throws SipParseException {
            ensureBuilder().withTransportSCTP();
            return this;
        }

        /**
         * Set the transport parameter on the underlying {@link SipURI} to be "ws".
         * 
         * @return
         * @throws SipParseException
         */
        public Builder<T> withTransportWS() throws SipParseException {
            ensureBuilder().withTransportWS();
            return this;
        }

        public Builder<T> withTransportWSS() throws SipParseException {
            ensureBuilder().withTransportWSS();
            return this;
        }

        /**
         * Use this address for the ToHeader.
         *
         * Note, if you already have specified any part of the address, such as the host,
         * then this
         * 
         * @param address
         * @throws SipParseException in case an address already has been specified either by
         * a previous call to this method or by specifying parts of an address through e.g.
         * withHost etc.
         * @return
         */
        public final Builder<T> withAddress(final Address address) throws SipParseException {
            assertNotNull(address, "Address cannot be null");
            this.addressBuilder = address.copy();
            return this;
        }

        /**
         * Build a new ToHeader.
         * 
         * @return
         * @throws SipParseException in case anything goes wrong while constructing the
         *         {@link ToHeader}.
         */
        public final T build() throws SipParseException {
            if (addressBuilder == null) {
                throw new SipParseException("You must specify an address of some sort.");
            }

            Address addressToUse = this.addressBuilder.build();
            final Buffer addressBuffer = addressToUse.toBuffer();
            final Buffer paramsBuffer = this.paramSupport.toBuffer();
            final Buffer headerValue = Buffers.createBuffer(addressBuffer.capacity() + paramsBuffer.capacity());
            addressBuffer.getBytes(0, headerValue);
            paramsBuffer.getBytes(0, headerValue);

            return internalBuild(headerValue, addressToUse, paramsBuffer);
        }

        @SuppressWarnings("unchecked")
        protected T internalBuild(final Buffer rawValue, final Address address, final Buffer params) {
            return (T) new AddressParametersHeaderImpl(name, rawValue, address, params);
        }

        private Address.Builder ensureBuilder() {
            if (addressBuilder == null) {
                this.addressBuilder = Address.builder();
            }
            return this.addressBuilder;
        }

    }

}
