/**
 * 
 */
package com.aboutsip.yajpcap.packet.sip.address.impl;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.yajpcap.packet.sip.address.AddressFactory;
import com.aboutsip.yajpcap.packet.sip.address.SipURI;

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
