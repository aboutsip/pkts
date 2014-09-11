package io.pkts.packet.sip.header;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.address.Address;
import io.pkts.packet.sip.header.impl.ContactHeaderImpl;

/**
 * @author jonas@jonasborjesson.com
 */
public interface ContactHeader extends AddressParametersHeader {

    Buffer NAME = Buffers.wrap("Contact");

    @Override
    ContactHeader clone();

    static Builder with() {
        return new Builder();
    }

    static Builder with(final Address address) throws SipParseException {
        final Builder builder = new Builder();
        builder.address(address);
        return builder;
    }

    static class Builder extends AddressParametersHeader.Builder<ContactHeader> {

        private Builder() {
            super(NAME);
        }

        @Override
        public ContactHeader internalBuild(final Address address, final Buffer params) throws SipParseException {
            return new ContactHeaderImpl(address, params);
        }
    }

}
