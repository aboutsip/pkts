/**
 * 
 */
package io.pkts.filters;

import io.pkts.frame.Frame;
import io.pkts.protocol.Protocol;

import java.io.IOException;


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
