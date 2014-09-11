/**
 * 
 */
package io.pkts.packet.sip.header.impl;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.address.Address;
import io.pkts.packet.sip.header.ContactHeader;
import io.pkts.packet.sip.header.RecordRouteHeader;


/**
 * @author jonas@jonasborjesson.com
 */
public class ContactHeaderImpl extends AddressParametersHeaderImpl implements ContactHeader {

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
        final Object[] result = AddressParametersHeaderImpl.frameAddressParameters(buffer);
        return new ContactHeaderImpl((Address) result[0], (Buffer) result[1]);
    }

    @Override
    public ContactHeader clone() {
        final Buffer buffer = Buffers.createBuffer(1024);
        transferValue(buffer);
        try {
            return ContactHeaderImpl.frame(buffer);
        } catch (final SipParseException e) {
            throw new RuntimeException("Unable to clone the Contact-header", e);
        }
    }

}
