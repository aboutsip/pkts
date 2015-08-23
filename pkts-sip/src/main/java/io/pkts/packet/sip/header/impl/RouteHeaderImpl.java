/**
 * 
 */
package io.pkts.packet.sip.header.impl;

import io.pkts.buffer.Buffer;
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
    public RouteHeaderImpl(final Buffer value, final Address address, final Buffer params) {
        super(RouteHeader.NAME, value, address, params);
    }

    @Override
    public RouteHeader clone() {
        final Buffer value = getValue();
        final Address address = getAddress();
        final Buffer params = getRawParams();
        // TODO: once Buffer is truly immutable we don't actually have to clone, like we don't have to do for Address anymore
        return new RouteHeaderImpl(value.clone(), address, params.clone());
    }

    @Override
    public RouteHeader.Builder copy() {
        final RouteHeader.Builder builder = RouteHeader.withAddress(getAddress());
        builder.withParameters(getRawParams().slice());
        return builder;
    }

    @Override
    public RouteHeader ensure() {
        return this;
    }

}
