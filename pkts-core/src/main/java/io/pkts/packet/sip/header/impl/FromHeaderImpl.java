/**
 * 
 */
package io.pkts.packet.sip.header.impl;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.address.Address;
import io.pkts.packet.sip.header.FromHeader;


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

    @Override
    public FromHeader clone() {
        final Buffer buffer = Buffers.createBuffer(1024);
        transferValue(buffer);
        try {
            return FromHeaderImpl.frame(buffer);
        } catch (final SipParseException e) {
            throw new RuntimeException("Unable to clone the From-header", e);
        }
    }

}
