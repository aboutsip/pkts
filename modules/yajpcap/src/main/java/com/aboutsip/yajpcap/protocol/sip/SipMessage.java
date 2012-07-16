package com.aboutsip.yajpcap.protocol.sip;

public abstract class SipMessage {

    public SipMessage() {

    }

    /**
     * Check whether this sip message is a response or not
     * 
     * @return
     */
    public boolean isResponse() {
        return false;
    }

    /**
     * Check whether this sip message is a request or not
     * 
     * @return
     */
    public boolean isRequest() {
        return false;
    }

}
