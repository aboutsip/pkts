/**
 * 
 */
package com.aboutsip.yajpcap.packet.sip.header.impl;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.yajpcap.packet.sip.SipParseException;
import com.aboutsip.yajpcap.packet.sip.header.CallIdHeader;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public final class CallIdHeaderImpl extends SipHeaderImpl implements CallIdHeader {

    public CallIdHeaderImpl(final Buffer value) {
        super(CallIdHeader.NAME, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer getCallId() {
        return getValue();
    }

    public static CallIdHeader frame(final Buffer buffer) throws SipParseException {
        return new CallIdHeaderImpl(buffer);
    }

    @Override
    public CallIdHeader clone() {
        try {
            return CallIdHeaderImpl.frame(getValue().clone());
        } catch (final SipParseException e) {
            throw new RuntimeException("Unable to clone the CallId-header", e);
        }
    }

}
