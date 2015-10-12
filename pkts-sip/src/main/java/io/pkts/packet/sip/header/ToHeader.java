/**
 * 
 */
package io.pkts.packet.sip.header;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.address.Address;
import io.pkts.packet.sip.header.impl.ParametersSupport;
import io.pkts.packet.sip.header.impl.ToHeaderImpl;

import java.util.Random;


/**
 * @author jonas@jonasborjesson.com
 */
public interface ToHeader extends AddressParametersHeader {

    Buffer NAME = Buffers.wrap("To");

    Buffer COMPACT_NAME = Buffers.wrap("t");

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


    @Override
    default ToHeader toToHeader() {
        return this;
    }

    /**
     * Frame the value as a {@link ToHeader}. This method assumes that you have already parsed out
     * the actual header name "To: ". Also, this method assumes that a message framer (or similar)
     * has framed the buffer that is being passed in to us to only contain this header and nothing
     * else.
     * 
     * Note, as with all the frame-methods on all headers/messages/whatever, they do not do any
     * validation that the information is actually correct. This method will simply only try and
     * validate just enough to get the framing done.
     * 
     * @param value
     * @return
     * @throws SipParseException in case anything goes wrong while parsing.
     */
    static ToHeader frame(final Buffer buffer) throws SipParseException {
        final Buffer original = buffer.slice();
        final Object[] result = AddressParametersHeader.frame(buffer);
        return new ToHeaderImpl(original, (Address) result[0], (Buffer) result[1]);
    }

    /**
     * Generate a new tag that can be used as a tag parameter for the {@link ToHeader}. A
     * tag-parameter only has to be unique within the same Call-ID space so therefore it doesn't
     * have to be cryptographically strong etc.
     * 
     * @return
     */
    static Buffer generateTag() {
        // TODO: fix this and move it to a better place.
        return Buffers.wrap(Integer.toHexString(new Random().nextInt()));
    }

    @Override
    default boolean isToHeader() {
        return true;
    }

    static Builder withHost(final Buffer host) {
        final Builder b = new Builder();
        b.withHost(host);
        return b;
    }

    static Builder withHost(final String host) {
        return withHost(Buffers.wrap(host));
    }

    static Builder withAddress(final Address address) throws SipParseException {
        final Builder builder = new Builder();
        builder.withAddress(address);
        return builder;
    }

    @Override
    Builder copy();

    class Builder extends AddressParametersHeader.Builder<ToHeader> {

        private Builder(ParametersSupport params) {
            super(NAME, params);
        }

        private Builder() {
            super(NAME);
        }

        @Override
        public ToHeader internalBuild(final Buffer rawValue, final Address address, final Buffer params) throws SipParseException {
            return new ToHeaderImpl(rawValue, address, params);
        }
    }

}
