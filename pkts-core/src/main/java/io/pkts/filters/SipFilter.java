/**
 * 
 */
package io.pkts.filters;

import io.pkts.packet.Packet;
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
    public boolean accept(final Packet packet) throws FilterException {
        try {
            return packet.hasProtocol(Protocol.SIP);
        } catch (final IOException e) {
            throw new FilterException("Unable to process the frame due to IOException", e);
        }
    }
}
