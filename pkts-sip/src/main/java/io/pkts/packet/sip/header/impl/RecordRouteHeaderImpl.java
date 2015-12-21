/**
 * 
 */
package io.pkts.packet.sip.header.impl;

import io.pkts.buffer.Buffer;
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
    public RecordRouteHeaderImpl(final Buffer value, final Address address, final Buffer params) {
        super(RecordRouteHeader.NAME, value, address, params);
    }

    @Override
    public RecordRouteHeader ensure() {
        return this;
    }

    @Override
    public RecordRouteHeader clone() {
        final Buffer value = getValue();
        final Address address = getAddress();
        final Buffer params = getRawParams();
        // TODO: once Buffer is truly immutable we don't actually have to clone, like we don't have to do for Address anymore
        return new RecordRouteHeaderImpl(value.clone(), address, params.clone());
    }

    @Override
    public RecordRouteHeader.Builder copy() {
        final RecordRouteHeader.Builder builder = RecordRouteHeader.withAddress(getAddress());
        builder.withParameters(getRawParams().slice());
        return builder;
    }
}
