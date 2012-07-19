/**
 * 
 */
package com.aboutsip.yajpcap.packet.impl;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.yajpcap.packet.SipMessage;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public abstract class SipMessageImpl implements SipMessage {

    /**
     * 
     */
    public SipMessageImpl() {
        // TODO Auto-generated constructor stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isResponse() {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRequest() {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer getMethod() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void verify() {
        // TODO Auto-generated method stub
    }

}
