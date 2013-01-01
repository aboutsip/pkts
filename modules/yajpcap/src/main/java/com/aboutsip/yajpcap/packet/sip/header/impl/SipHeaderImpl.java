/**
 * 
 */
package com.aboutsip.yajpcap.packet.sip.header.impl;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.yajpcap.packet.sip.SipHeader;

/**
 * @author jonas@jonasborjesson.com
 */
public class SipHeaderImpl implements SipHeader {

    private final Buffer name;

    private final Buffer value;
    /**
     * 
     */
    public SipHeaderImpl(final Buffer name, final Buffer value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Subclasses may override this one and are in fact encourage to do so
     * 
     * {@inheritDoc}
     */
    @Override
    public Buffer getName() {
        return this.name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return this.name.toString() + ": " + this.value.toString();
    }

}
