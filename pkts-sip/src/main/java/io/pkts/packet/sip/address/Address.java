/**
 * 
 */
package io.pkts.packet.sip.address;

import io.pkts.buffer.Buffer;
import io.pkts.packet.sip.SipParseException;


/**
 * @author jonas@jonasborjesson.com
 */
public interface Address {

    /**
     * Get the display name of this {@link Address} or an empty buffer if it is
     * not set.
     * 
     * @return
     */
    Buffer getDisplayName();

    /**
     * Get the {@link URI} of this {@link Address}.
     * 
     * @return the {@link URI}
     * @throws SipParseException
     */
    URI getURI() throws SipParseException;

    /**
     * Get the {@link Address} as a raw buffer.
     * 
     * @return
     */
    Buffer toBuffer();

    void getBytes(Buffer dst);

}
