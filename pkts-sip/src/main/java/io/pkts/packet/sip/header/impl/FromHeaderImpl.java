/**
 * 
 */
package io.pkts.packet.sip.header.impl;

import io.pkts.buffer.Buffer;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.address.Address;
import io.pkts.packet.sip.header.AddressParametersHeader;
import io.pkts.packet.sip.header.FromHeader;


/**
 * @author jonas@jonasborjesson.com
 */
public class FromHeaderImpl extends AddressParametersHeaderImpl implements FromHeader {

    /**
     * @param name
     * @param address
     * @param params
     */
    public FromHeaderImpl(final Buffer value, final Address address, final Buffer params) {
        super(FromHeader.NAME, value, address, params);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer getTag() throws SipParseException {
        return getParameter(TAG);
    }

    @Override
    public FromHeader clone() {
        final Buffer value = getValue();
        final Address address = getAddress();
        final Buffer params = getRawParams();
        // TODO: once Buffer is truly immutable we don't actually have to clone, like we don't have to do for Address anymore
        return new FromHeaderImpl(value.clone(), address, params.clone());
    }

    @Override
    public AddressParametersHeader.Builder<FromHeader> copy() {
        return FromHeader.withAddress(getAddress()).withParameters(getRawParams().slice());
    }

    @Override
    public FromHeader ensure() {
        return this;
    }

}
