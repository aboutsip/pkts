/**
 * 
 */
package com.aboutsip.yajpcap.packet.sip;

import com.aboutsip.yajpcap.packet.SipMessageFactory;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public class SipMessageFactoryImpl implements SipMessageFactory {

    /**
     * 
     */
    public SipMessageFactoryImpl() {
        // TODO Auto-generated constructor stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SipResponse createResponse(final int statusCode, final SipRequest request) {
        return null;
    }

}
