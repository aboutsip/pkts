/**
 * 
 */
package io.pkts.packet.sip.header.impl;

import io.pkts.buffer.Buffer;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.address.Address;
import io.pkts.packet.sip.header.ToHeader;

/**
 * @author jonas@jonasborjesson.com
 */
public final class ToHeaderImpl extends AddressParametersHeaderImpl implements ToHeader {

    /**
     *
     * @param value since all headers are immutable, we will always supply the raw buffer
     *              making up the actual header, which then makes life easier when we
     *              "convert" headers to buffers etc.
     * @param address the parsed address
     * @param parametersBuffer the parsed parameters
     */
    public ToHeaderImpl(final Buffer value, final Address address, final Buffer parametersBuffer) {
        super(ToHeader.NAME, value, address, parametersBuffer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer getTag() throws SipParseException {
        return getParameter(TAG);
    }


    @Override
    public ToHeader clone() {
        final Buffer value = getValue();
        final Address address = getAddress();
        final Buffer params = getRawParams();
        // TODO: once Buffer is truly immutable we don't actually have to clone, like we don't have to do for Address anymore
        return new ToHeaderImpl(value.clone(), address, params.clone());
    }

    @Override
    public ToHeader ensure() {
        return this;
    }

    @Override
    public ToHeader.Builder copy() {
        final ToHeader.Builder builder = ToHeader.withAddress(getAddress());
        builder.withParameters(getRawParams().slice());
        return builder;
    }

}
