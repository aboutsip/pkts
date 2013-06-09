/**
 * 
 */
package io.pkts.packet.sip.address.impl;

import io.pkts.buffer.Buffer;
import io.pkts.packet.sip.address.AddressFactory;
import io.pkts.packet.sip.address.SipURI;


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

}
