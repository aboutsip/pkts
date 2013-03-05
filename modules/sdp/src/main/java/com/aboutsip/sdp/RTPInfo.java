/**
 * 
 */
package com.aboutsip.sdp;

/**
 * @author jonas@jonasborjesson.com
 */
public interface RTPInfo {

    /**
     * Get the address to where we should be sending. Typically this is the
     * IP-address of the receiver.
     * 
     * @return
     */
    String getAddress();

    /**
     * Get the media port where we are expected to send media. Typical
     * 
     * @return
     */
    int getMediaPort();

}
