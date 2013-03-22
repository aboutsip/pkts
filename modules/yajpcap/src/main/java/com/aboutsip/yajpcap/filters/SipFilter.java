/**
 * 
 */
package com.aboutsip.yajpcap.filters;

import java.io.IOException;

import com.aboutsip.yajpcap.frame.Frame;
import com.aboutsip.yajpcap.protocol.Protocol;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public class SipFilter implements Filter {

    /**
     * 
     */
    public SipFilter() {
        // left empty intentionally
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean accept(final Frame frame) throws FilterException {
        try {
            return frame.hasProtocol(Protocol.SIP);
        } catch (final IOException e) {
            throw new FilterException("Unable to process the frame due to IOException", e);
        }
    }

}
