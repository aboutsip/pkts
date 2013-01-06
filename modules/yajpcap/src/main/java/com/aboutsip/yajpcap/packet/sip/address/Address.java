/**
 * 
 */
package com.aboutsip.yajpcap.packet.sip.address;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.yajpcap.packet.sip.impl.SipParseException;

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

}
