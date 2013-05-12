/**
 * 
 */
package com.aboutsip.yajpcap.packet.sip.header.impl;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.buffer.Buffers;
import com.aboutsip.yajpcap.packet.sip.SipParseException;
import com.aboutsip.yajpcap.packet.sip.address.Address;
import com.aboutsip.yajpcap.packet.sip.header.ToHeader;

/**
 * @author jonas@jonasborjesson.com
 */
public final class ToHeaderImpl extends AddressParametersHeader implements ToHeader {

    /**
     * 
     */
    private ToHeaderImpl(final Address address, final Buffer parametersBuffer) {
        super(ToHeader.NAME, address, parametersBuffer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer getTag() throws SipParseException {
        return getParameter(TAG);
    }

    /**
     * Frame the value as a {@link ToHeader}. This method assumes that you have
     * already parsed out the actual header name "To: ". Also, this method
     * assumes that a message framer (or similar) has framed the buffer that is
     * being passed in to us to only contain this header and nothing else.
     * 
     * Note, as with all the frame-methods on all headers/messages/whatever,
     * they do not do any validation that the information is actually correct.
     * This method will simply only try and validate just enough to get the
     * framing done.
     * 
     * @param value
     * @return
     * @throws SipParseException
     *             in case anything goes wrong while parsing.
     */
    public static ToHeader frame(final Buffer buffer) throws SipParseException {
        final Object[] result = AddressParametersHeader.frameAddressParameters(buffer);
        return new ToHeaderImpl((Address) result[0], (Buffer) result[1]);
    }

    @Override
    public ToHeader clone() {
        final Buffer buffer = Buffers.createBuffer(1024);
        transferValue(buffer);
        try {
            return ToHeaderImpl.frame(buffer);
        } catch (final SipParseException e) {
            throw new RuntimeException("Unable to clone the To-header", e);
        }
    }

}
