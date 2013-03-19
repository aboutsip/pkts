/**
 * 
 */
package com.aboutsip.yajpcap.packet.sip.header;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.buffer.Buffers;
import com.aboutsip.yajpcap.packet.sip.SipHeader;
import com.aboutsip.yajpcap.packet.sip.SipParseException;

/**
 * @author jonas@jonasborjesson.com
 */
public interface ToHeader extends SipHeader, HeaderAddress, Parameters {

    Buffer NAME = Buffers.wrap("To");

    /**
     * Get the tag parameter.
     * 
     * @return the tag or null if it hasn't been set.
     * @throws SipParseException
     *             in case anything goes wrong while extracting tag.
     */
    Buffer getTag() throws SipParseException;

}
