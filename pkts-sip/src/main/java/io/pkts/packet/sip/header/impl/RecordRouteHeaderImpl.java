/**
 * 
 */
package io.pkts.packet.sip.header.impl;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.address.Address;
import io.pkts.packet.sip.header.RecordRouteHeader;


/**
 * @author jonas@jonasborjesson.com
 */
public class RecordRouteHeaderImpl extends AddressParametersHeaderImpl implements RecordRouteHeader {

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
        final Object[] result = AddressParametersHeaderImpl.frameAddressParameters(buffer);
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
