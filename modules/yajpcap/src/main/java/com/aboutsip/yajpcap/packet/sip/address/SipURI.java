/**
 * 
 */
package com.aboutsip.yajpcap.packet.sip.address;

import com.aboutsip.buffer.Buffer;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public interface SipURI extends URI {

    /**
     * Get the user portion of this URI.
     * 
     * @return the user portion of this URI or an empty buffer if there is no
     *         user portion
     */
    Buffer getUser();

    /**
     * Get the host portion of this URI.
     * 
     * @return
     */
    Buffer getHost();

    /**
     * Get the port. If the port isn't set the default port for the scheme will
     * be returned (i.e. 5060 for sip and 5061 for sips)
     * 
     * @return
     */
    int getPort();

    /**
     * Check whether this is a sips URI.
     * 
     * @return true if this indeed is a sips URI, false otherwise.
     */
    boolean isSecure();

    /**
     * Get the entire content of the {@link SipURI} as a {@link Buffer}.
     * 
     * @return
     */
    Buffer toBuffer();

}
