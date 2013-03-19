/**
 * 
 */
package com.aboutsip.yajpcap.packet.sip.header.impl;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.yajpcap.packet.sip.SipHeader;
import com.aboutsip.yajpcap.packet.sip.SipParseException;

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
        return getName().toString() + ": " + getValue();
    }

    @Override
    public void verify() throws SipParseException {
        // by default, everything is assumed to be correct.
        // Subclasses should override this method and
        // check that everything is ok...

    }

}
