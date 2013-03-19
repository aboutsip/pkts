/**
 * 
 */
package com.aboutsip.yajpcap.packet.sip.header.impl;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.yajpcap.packet.sip.SipParseException;
import com.aboutsip.yajpcap.packet.sip.address.Address;
import com.aboutsip.yajpcap.packet.sip.header.FromHeader;

/**
 * @author jonas@jonasborjesson.com
 */
public class FromHeaderImpl extends AddressParametersHeader implements FromHeader {

    /**
     * @param name
     * @param address
     * @param params
     */
    public FromHeaderImpl(final Address address, final Buffer params) {
        super(FromHeader.NAME, address, params);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer getTag() throws SipParseException {
        return getParameter(TAG);
    }

    /**
     * Frame the value as a {@link FromHeader}.
     * 
     * @param value
     * @return
     * @throws SipParseException
     *             in case anything goes wrong while parsing.
     */
    public static FromHeader frame(final Buffer buffer) throws SipParseException {
        final Object[] result = AddressParametersHeader.frameAddressParameters(buffer);
        return new FromHeaderImpl((Address) result[0], (Buffer) result[1]);
    }


}
