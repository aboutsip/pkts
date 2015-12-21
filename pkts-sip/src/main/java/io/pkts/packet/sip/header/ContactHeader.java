package io.pkts.packet.sip.header;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.address.Address;
import io.pkts.packet.sip.address.SipURI;
import io.pkts.packet.sip.header.impl.ContactHeaderImpl;

/**
 * @author jonas@jonasborjesson.com
 */
public interface ContactHeader extends AddressParametersHeader {

    Buffer NAME = Buffers.wrap("Contact");

    Buffer COMPACT_NAME = Buffers.wrap("m");

    @Override
    ContactHeader clone();

    static Builder with() {
        return new Builder();
    }

    static Builder withAddress(final Address address) throws SipParseException {
        final Builder builder = new Builder();
        builder.withAddress(address);
        return builder;
    }

    static Builder withHost(final Buffer host) throws SipParseException {
        final Builder builder = new Builder();
        builder.withHost(host);
        return builder;
    }

    static Builder withHost(final String host) throws SipParseException {
        final Builder builder = new Builder();
        builder.withHost(host);
        return builder;
    }

    static Builder withSipURI(final SipURI uri) throws SipParseException {
        final Builder builder = new Builder();
        final Address address = Address.withURI(uri).build();
        builder.withAddress(address);
        return builder;
    }

    /**
     * Frame the value as a {@link ContactHeader}.
     * 
     * @param value
     * @return
     * @throws SipParseException in case anything goes wrong while parsing.
     */
    static ContactHeader frame(final Buffer buffer) throws SipParseException {
        final Buffer original = buffer.slice();
        final Object[] result = AddressParametersHeader.frame(buffer);
        return new ContactHeaderImpl(original, (Address) result[0], (Buffer) result[1]);
    }

    @Override
    Builder copy();

    @Override
    default boolean isContactHeader() {
        return true;
    }

    @Override
    default ContactHeader toContactHeader() {
        return this;
    }

    class Builder extends AddressParametersHeader.Builder<ContactHeader> {

        private Builder() {
            super(NAME);
        }

        @Override
        public ContactHeader internalBuild(final Buffer rawValue, final Address address, final Buffer params) throws SipParseException {
            return new ContactHeaderImpl(rawValue, address, params);
        }
    }

}
