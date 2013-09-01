/**
 * 
 */
package io.pkts.packet.sip.header;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;

/**
 * @author jonas@jonasborjesson.com
 */
public interface CallIdHeader extends SipHeader {

    Buffer NAME = Buffers.wrap("Call-ID");

    /**
     * The compact name of the Call-ID header is 'i'
     */
    Buffer COMPACT_NAME = Buffers.wrap("i");

    Buffer getCallId();

    @Override
    CallIdHeader clone();

}
