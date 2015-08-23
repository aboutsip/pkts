/**
 * 
 */
package io.pkts.packet.sip.header.impl;

import io.pkts.buffer.Buffer;
import io.pkts.packet.sip.address.Address;
import io.pkts.packet.sip.header.ContactHeader;


/**
 * @author jonas@jonasborjesson.com
 */
public class ContactHeaderImpl extends AddressParametersHeaderImpl implements ContactHeader {

    /**
     * @param name
     * @param address
     * @param params
     */
    public ContactHeaderImpl(final Buffer value, final Address address, final Buffer params) {
        super(ContactHeader.NAME, value, address, params);
    }

    @Override
    public ContactHeader clone() {
        final Buffer value = getValue();
        final Address address = getAddress();
        final Buffer params = getRawParams();
        // TODO: once Buffer is truly immutable we don't actually have to clone, like we don't have to do for Address anymore
        return new ContactHeaderImpl(value.clone(), address, params.clone());
    }

    @Override
    public ContactHeader.Builder copy() {
        final ContactHeader.Builder builder = ContactHeader.withAddress(getAddress());
        builder.withParameters(getRawParams().slice());
        return builder;
    }

    @Override
    public ContactHeader ensure() {
        return this;
    }

}
