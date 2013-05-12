/**
 * 
 */
package com.aboutsip.yajpcap.packet.sip.header.impl;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.buffer.Buffers;
import com.aboutsip.yajpcap.packet.sip.SipParseException;
import com.aboutsip.yajpcap.packet.sip.address.Address;
import com.aboutsip.yajpcap.packet.sip.header.RecordRouteHeader;

/**
 * @author jonas@jonasborjesson.com
 */
public class RecordRouteHeaderImpl extends AddressParametersHeader implements RecordRouteHeader {

    /**
     * @param name
     * @param address
     * @param params
     */
    public RecordRouteHeaderImpl(final Address address, final Buffer params) {
        super(RecordRouteHeader.NAME, address, params);
    }

    /**
     * Frame the value as a {@link RecordRouteHeader}.
     * 
     * @param value
     * @return
     * @throws SipParseException
     *             in case anything goes wrong while parsing.
     */
    public static RecordRouteHeader frame(final Buffer buffer) throws SipParseException {
        final Object[] result = AddressParametersHeader.frameAddressParameters(buffer);
        return new RecordRouteHeaderImpl((Address) result[0], (Buffer) result[1]);
    }

    @Override
    public RecordRouteHeader clone() {
        final Buffer buffer = Buffers.createBuffer(1024);
        transferValue(buffer);
        try {
            return RecordRouteHeaderImpl.frame(buffer);
        } catch (final SipParseException e) {
            throw new RuntimeException("Unable to clone the RecordRoute-header", e);
        }
    }

}
