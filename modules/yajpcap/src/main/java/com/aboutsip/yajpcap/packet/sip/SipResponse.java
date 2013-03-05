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

    /**
     * Convenience method for checking whether this is a 1xx response or not.
     * 
     * @return
     */
    boolean isProvisional();

    /**
     * Convenience method for checking whether this is a 2xx response or not.
     * 
     * @return
     */
    boolean isSuccess();

    /**
     * Convenience method for checking whether this is a 3xx response or not.
     * 
     * @return
     */
    boolean isRedirect();

    /**
     * Convenience method for checking whether this is a 4xx response or not.
     * 
     * @return
     */
    boolean isClientError();

    /**
     * Convenience method for checking whether this is a 5xx response or not.
     * 
     * @return
     */
    boolean isServerError();

    /**
     * Convenience method for checking whether this is a 6xx response or not.
     * 
     * @return
     */
    boolean isGlobalError();

    /**
     * Convenience method for checking whether this is a 100 Trying response or
     * not.
     * 
     * @return
     */
    boolean is100Trying();

    /**
     * Convenience method for checking whether this is a 180 Ringing response or
     * not.
     * 
     * @return
     */
    boolean isRinging();

    /**
     * Convenience method for checking whether this is a 480 Timeout response or
     * not.
     * 
     * @return
     */
    boolean isTimeout();

}
