/**
 * 
 */
package io.pkts.packet.sip.address.impl;

import io.pkts.buffer.Buffer;
import io.pkts.packet.sip.address.Address;
import io.pkts.packet.sip.address.AddressFactory;
import io.pkts.packet.sip.address.SipURI;
import io.pkts.packet.sip.address.URI;

/**
 * @author jonas@jonasborjesson.com
 */
public final class AddressFactoryImpl implements AddressFactory {

    /**
     * {@inheritDoc}
     */
    @Override
    public SipURI createSipURI(final Buffer user, final Buffer host) {
        return new SipURIImpl(false, user, host, null, null, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Address createAddress(final Buffer displayName, final URI uri) throws IllegalArgumentException {
        if (uri == null) {
            throw new IllegalArgumentException("The URI cannot be null");
        }

        return new AddressImpl(displayName, uri);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Address createAddress(final URI uri) throws IllegalArgumentException {
        return createAddress(null, uri);
    }

}
