/**
 * 
 */
package io.pkts.packet.sip;

import io.pkts.packet.sip.address.URI;

/**
 * @author jonas@jonasborjesson.com
 */
public interface SipRequest extends SipMessage {

    /**
     * Get the request uri of the sip request
     * 
     * @return
     */
    URI getRequestUri() throws SipParseException;

    @Override
    SipRequest clone();

}
