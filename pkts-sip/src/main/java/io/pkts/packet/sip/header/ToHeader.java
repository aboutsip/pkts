/**
 * 
 */
package io.pkts.packet.sip.header;

import static io.pkts.packet.sip.impl.PreConditions.assertNotEmpty;
import static io.pkts.packet.sip.impl.PreConditions.assertNotNull;
import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.address.Address;
import io.pkts.packet.sip.header.impl.ToHeaderImpl;


/**
 * @author jonas@jonasborjesson.com
 */
public interface ToHeader extends SipHeader, HeaderAddress, Parameters {

    Buffer NAME = Buffers.wrap("To");

    /**
     * Get the tag parameter.
     * 
     * @return the tag or null if it hasn't been set.
     * @throws SipParseException
     *             in case anything goes wrong while extracting tag.
     */
    Buffer getTag() throws SipParseException;

    @Override
    ToHeader clone();

    static ToHeader create(final Buffer header) throws SipParseException {
        return ToHeaderImpl.frame(header);
    }

    static ToHeaderBuilder with() {
        return new ToHeaderBuilder();
    }

    static ToHeaderBuilder with(final Address address) throws SipParseException {
        final ToHeaderBuilder builder = new ToHeaderBuilder();
        builder.address(address);
        return builder;
    }

    static class ToHeaderBuilder {

        private Buffer user;
        private Buffer host;

        private Address address;

        private ToHeaderBuilder() {}

        /**
         * Set the user portion of the {@link ToHeader}. Since the user portion may in fact be null
         * (or empty), any value is accepted but of course, a value of null or empty will lead to no
         * user portion of the SIP-URI within the header.
         * 
         * @param user
         * @return
         */
        public ToHeaderBuilder user(final Buffer user) {
            this.user = user;
            return this;
        }

        /**
         * use this host for the ToHeader.
         * 
         * NOTE: you can only specify either an address or a host and user but not both. If you do,
         * an exception will occur at the time you try and {@link #build()} the header.
         * 
         * @param host
         * @return
         */
        public ToHeaderBuilder host(final Buffer host) {
            this.host = assertNotEmpty(host, "Host cannot be null or empty");
            return this;
        }

        /**
         * Use this address for the ToHeader.
         * 
         * NOTE: you can only specify either an address or a host and user but not both. If you do,
         * an exception will occur at the time you try and {@link #build()} the header.
         * 
         * @param address
         * @return
         */
        public ToHeaderBuilder address(final Address address) {
            this.address = assertNotNull(address, "Address cannot be null");
            return this;
        }

        /**
         * Build a new ToHeader.
         * 
         * @return
         * @throws SipParseException in case anything goes wrong while constructing the
         *         {@link ToHeader}.
         */
        public ToHeader build() throws SipParseException {
            if (host != null && address != null) {
                throw new SipParseException("Both host and address was specified. Not sure which to pick");
            }

            Address addressToUse = this.address;
            if (host != null) {
                addressToUse = Address.with().user(user).host(host).build();
            }

            return new ToHeaderImpl(addressToUse, null);
        }

    }

}
