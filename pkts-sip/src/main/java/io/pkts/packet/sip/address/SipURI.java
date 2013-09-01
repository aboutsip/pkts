/**
 * 
 */
package io.pkts.packet.sip.address;

import io.pkts.buffer.Buffer;

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
     * Get the port. If the port isn't set then -1 (negative one) will be
     * returned.
     * 
     * @return
     */
    int getPort();

    /**
     * Set the port.
     * 
     * @param port
     */
    void setPort(int port);

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
