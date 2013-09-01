/**
 * 
 */
package io.pkts.packet.sip.address;

import io.pkts.buffer.Buffer;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public interface URI {

    /**
     * Returns the scheme of this URI, which really can be anything (see RFC3261
     * section 25.1 and the definition of absoluteURI) but most commonly will be
     * "sip", "sips" or "tel".
     * 
     * @return
     */
    Buffer getScheme();

    /**
     * Check whether this {@link URI} is a "sip" or "sips" URI.
     * 
     * @return true if this {@link URI} is a SIP URI, false otherwise.
     */
    boolean isSipURI();

    /**
     * Write the bytes of this URI into the destination buffer
     * 
     * @param dst
     */
    void getBytes(Buffer dst);

}
