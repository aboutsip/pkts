/**
 * 
 */
package com.aboutsip.yajpcap.packet.impl.sip;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.yajpcap.packet.SipResponse;

/**
 * @author jonas@jonasborjesson.com
 */
public final class SipResponseImpl extends SipMessageImpl implements SipResponse {

    /**
     * @param initialLine
     * @param headers
     * @param payload
     */
    public SipResponseImpl(final SipInitialLine initialLine, final Buffer headers, final Buffer payload) {
        super(initialLine, headers, payload);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer getMethod() {
        // TODO Auto-generated method stub
        return null;
    }

}
