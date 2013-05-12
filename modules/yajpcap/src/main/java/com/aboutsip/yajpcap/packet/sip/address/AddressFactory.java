/**
 * 
 */
package com.aboutsip.yajpcap.packet.sip.address;

import com.aboutsip.buffer.Buffer;

/**
 * @author jonas@jonasborjesson.com
 */
public interface AddressFactory {

    /**
     * Create a new {@link SipURI}.
     * 
     * @param user
     *            the user portion of the {@link SipURI}, may be null.
     * @param host
     *            the host portion of the {@link SipURI}.
     * @return
     */
    SipURI createSipURI(Buffer user, Buffer host);

}
