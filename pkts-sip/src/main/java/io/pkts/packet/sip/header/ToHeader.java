/**
 * 
 */
package io.pkts.packet.sip.header;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.address.Address;
import io.pkts.packet.sip.header.impl.ToHeaderImpl;


/**
 * @author jonas@jonasborjesson.com
 */
public interface ToHeader extends AddressParametersHeader {

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

    static Builder with() {
        return new Builder();
    }

    static Builder with(final Address address) throws SipParseException {
        final Builder builder = new Builder();
        builder.address(address);
        return builder;
    }

    static class Builder extends AddressParametersHeader.Builder<ToHeader> {

        private Builder() {
            super(NAME);
        }

        @Override
        public ToHeader internalBuild(final Address address, final Buffer params) throws SipParseException {
            return new ToHeaderImpl(address, params);
        }
    }

}
