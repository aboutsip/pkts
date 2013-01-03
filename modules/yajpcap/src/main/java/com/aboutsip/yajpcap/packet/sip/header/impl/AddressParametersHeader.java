/**
 * 
 */
package com.aboutsip.yajpcap.packet.sip.header.impl;

import java.io.IOException;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.buffer.Buffers;
import com.aboutsip.yajpcap.packet.sip.address.Address;
import com.aboutsip.yajpcap.packet.sip.address.impl.AddressImpl;
import com.aboutsip.yajpcap.packet.sip.header.FromHeader;
import com.aboutsip.yajpcap.packet.sip.header.HeaderAddress;
import com.aboutsip.yajpcap.packet.sip.header.Parameters;
import com.aboutsip.yajpcap.packet.sip.header.ToHeader;
import com.aboutsip.yajpcap.packet.sip.impl.SipParseException;

/**
 * A base class for all headers that implmenets both the {@link Address} and
 * {@link Parameters} interfaces, such as the {@link ToHeader} and
 * {@link FromHeader}.
 * 
 * @author jonas@jonasborjesson.com
 */
public abstract class AddressParametersHeader extends ParametersImpl implements HeaderAddress {

    public static final Buffer TAG = Buffers.wrap("tag");

    private final Address address;

    /**
     * @param name
     * @param params
     */
    public AddressParametersHeader(final Buffer name, final Address address, final Buffer params) {
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
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Frame the value as a {@link AddressParametersHeader}. This method assumes
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

}
