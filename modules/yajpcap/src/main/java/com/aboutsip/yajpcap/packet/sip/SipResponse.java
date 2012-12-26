/**
 * 
 */
package com.aboutsip.yajpcap.packet.sip;


/**
 * @author jonas@jonasborjesson.com
 */
public interface SipResponse extends SipMessage {

    /**
     * Get the status code of this SIP response
     * 
     * @return
     */
    int getStatus();

}
