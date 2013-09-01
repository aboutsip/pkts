/**
 * 
 */
package io.pkts.packet.sip.header;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipParseException;


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

    @Override
    ToHeader clone();

}
