/**
 * 
 */
package com.aboutsip.yajpcap.packet.layer7.sip;

import com.aboutsip.buffer.Buffer;


/**
 * @author jonas@jonasborjesson.com
 */
public interface SipRequest extends SipMessage {

    /**
     * Get the request uri of the sip request
     * 
     * @return
     */
    Buffer getRequestUri();


}
