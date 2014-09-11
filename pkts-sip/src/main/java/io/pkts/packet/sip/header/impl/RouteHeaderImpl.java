/**
 * 
 */
package io.pkts.packet.sip.header.impl;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.address.Address;
import io.pkts.packet.sip.header.RouteHeader;


/**
 * @author jonas@jonasborjesson.com
 */
public final class RouteHeaderImpl extends AddressParametersHeaderImpl implements RouteHeader {

    /**
     * @param name
     * @param address
     * @param params
     */
    public RouteHeaderImpl(final Address address, final Buffer params) {
        super(RouteHeader.NAME, address, params);
    }

    /**
     * Frame the value as a {@link RouteHeader}.
     * 
     * @param value
     * @return
     * @throws SipParseException
     *             in case anything goes wrong while parsing.
     */
    public static RouteHeader frame(final Buffer buffer) throws SipParseException {
        final Object[] result = AddressParametersHeaderImpl.frameAddressParameters(buffer);
        return new RouteHeaderImpl((Address) result[0], (Buffer) result[1]);
    }

    @Override
    public RouteHeader clone() {
        final Buffer buffer = Buffers.createBuffer(1024);
        transferValue(buffer);
        try {
            return RouteHeaderImpl.frame(buffer);
        } catch (final SipParseException e) {
            throw new RuntimeException("Unable to clone the Route-header", e);
        }
    }

}
