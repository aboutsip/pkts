/**
 * 
 */
package com.aboutsip.yajpcap.packet.sip.address;

import com.aboutsip.buffer.Buffer;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public interface URI {

    /**
     * Returns the scheme of this URI.
     * 
     * @return
     */
    Buffer getScheme();

    /**
     * Check whether this {@link URI} is a "sip" or "sips" URI.
     * 
     * @return true if this {@link URI} is a SIP URI, false otherwise.
     */
    boolean isSipURI();

}
