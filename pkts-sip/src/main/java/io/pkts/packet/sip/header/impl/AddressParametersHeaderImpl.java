/**
 * 
 */
package io.pkts.packet.sip.header.impl;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.address.Address;
import io.pkts.packet.sip.address.impl.AddressImpl;
import io.pkts.packet.sip.header.AddressParametersHeader;
import io.pkts.packet.sip.header.FromHeader;
import io.pkts.packet.sip.header.Parameters;
import io.pkts.packet.sip.header.ToHeader;

import java.io.IOException;


/**
 * A base class for all headers that implmenets both the {@link Address} and {@link Parameters}
 * interfaces, such as the {@link ToHeader} and {@link FromHeader}. However, users must be able to
 * create to create other {@link AddressParametersHeader}s that are unknown to this implementation
 * so they can either extend this base class or simply just create a new
 * {@link AddressParametersHeader} by using the {@link Builder}.
 * 
 * @author jonas@jonasborjesson.com
 */
public class AddressParametersHeaderImpl extends ParametersImpl implements AddressParametersHeader {

    public static final Buffer TAG = Buffers.wrap("tag");

    private final Address address;

    /**
     * @param name
     * @param params
     */
    public AddressParametersHeaderImpl(final Buffer name, final Address address, final Buffer params) {
        super(name, params);
        this.address = address;
    }

    @Override
    public Address getAddress() {
        return this.address;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer getValue() {
        // TODO: create a composite buffer instead of this crap
        final StringBuilder sb = new StringBuilder();
        sb.append(this.address.toString());
        final Buffer superValue = super.getValue();
        if (superValue != null) {
            sb.append(superValue.toString());
        }
        return Buffers.wrap(sb.toString());
    }

    /**
     * Frame the value as a {@link AddressParametersHeaderImpl}. This method assumes
     * that you have already parsed out the actual header name, e.g. "To: ".
     * Also, this method assumes that a message framer (or similar) has framed
     * the buffer that is being passed in to us to only contain this header and
     * nothing else.
     * 
     * Note, as with all the frame-methods on all headers/messages/whatever,
     * they do not do any validation that the information is actually correct.
     * This method will simply only try and validate just enough to get the
     * framing done.
     * 
     * @param value
     * @return an array where the first object is a {@link Address} object and
     *         the second is a {@link Buffer} with all the parameters.
     * @throws SipParseException
     *             in case anything goes wrong while parsing.
     */
    protected static Object[] frameAddressParameters(final Buffer buffer) throws SipParseException {
        try {
            final Address address = AddressImpl.parse(buffer);
            // we assume that the passed in buffer ONLY contains
            // this header and nothing else. Therefore, there are only
            // header parameters left after we have consumed the address
            // portion.
            Buffer params = null;
            if (buffer.hasReadableBytes()) {
                params = buffer.slice();
            }
            return new Object[] {
                    address, params };
        } catch (final IndexOutOfBoundsException e) {
            throw new SipParseException(buffer.getReaderIndex(),
                    "Unable to process the value due to a IndexOutOfBoundsException", e);
        } catch (final IOException e) {
            throw new SipParseException(buffer.getReaderIndex(),
                    "Unable to process the To-header to due an IOException");
        }
    }

    @Override
    protected void transferValue(final Buffer dst) {
        this.address.getBytes(dst);
        super.transferValue(dst);
    }

}
