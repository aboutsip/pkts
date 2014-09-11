/**
 * 
 */
package io.pkts.packet.sip.header;

import static io.pkts.packet.sip.impl.PreConditions.assertNotEmpty;
import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.header.impl.CallIdHeaderImpl;

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

    static CallIdHeader create(final Buffer callId) {
        assertNotEmpty(callId, "The value of the Call-ID cannot be null or empty");
        return new CallIdHeaderImpl(callId);
    }

    static CallIdHeader create() {
        return new CallIdHeaderImpl();
    }

}
