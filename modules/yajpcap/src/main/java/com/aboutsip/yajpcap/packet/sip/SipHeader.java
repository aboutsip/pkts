/**
 * 
 */
package com.aboutsip.yajpcap.packet.sip;

import com.aboutsip.buffer.Buffer;

/**
 * @author jonas@jonasborjesson.com
 */
public interface SipHeader {

    /**
     * Get the name of the header
     * 
     * @return
     */
    Buffer getName();

    /**
     * Get the value of the buffer
     * 
     * @return
     */
    Buffer getValue();

}
