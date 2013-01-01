/**
 * 
 */
package com.aboutsip.yajpcap.packet.sip.address;

import com.aboutsip.buffer.Buffer;

/**
 * @author jonas@jonasborjesson.com
 */
public interface Address {

    /**
     * Get the display name of this {@link Address} or null if it is not set.
     * 
     * @return
     */
    Buffer getDisplayName();

    /**
     * Get the {@link URI} of this {@link Address}.
     * 
     * @return the {@link URI}
     */
    URI getURI();

}
