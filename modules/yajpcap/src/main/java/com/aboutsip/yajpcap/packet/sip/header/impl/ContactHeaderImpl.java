/**
 * 
 */
package com.aboutsip.yajpcap.packet.sip.header.impl;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.yajpcap.packet.sip.SipParseException;
import com.aboutsip.yajpcap.packet.sip.address.Address;
import com.aboutsip.yajpcap.packet.sip.header.ContactHeader;
import com.aboutsip.yajpcap.packet.sip.header.RecordRouteHeader;

/**
 * @author jonas@jonasborjesson.com
 */
public class ContactHeaderImpl extends AddressParametersHeader implements ContactHeader {

    /**
     * @param name
     * @param address
     * @param params
     */
    public ContactHeaderImpl(final Address address, final Buffer params) {
        super(ContactHeader.NAME, address, params);
    }

    /**
     * Frame the value as a {@link RecordRouteHeader}.
     * 
     * @param value
     * @return
     * @throws SipParseException
     *             in case anything goes wrong while parsing.
     */
    public static ContactHeader frame(final Buffer buffer) throws SipParseException {
        final Object[] result = AddressParametersHeader.frameAddressParameters(buffer);
        return new ContactHeaderImpl((Address) result[0], (Buffer) result[1]);
    }

}
