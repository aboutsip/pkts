/**
 * 
 */
package io.pkts.packet.sip.header;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.header.impl.CallIdHeaderImpl;

import static io.pkts.packet.sip.impl.PreConditions.assertNotEmpty;

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

    @Override
    default boolean isCallIdHeader() {
        return true;
    }

    @Override
    default CallIdHeader toCallIdHeader() {
        return this;
    }

    static CallIdHeader frame(final Buffer buffer) {
        assertNotEmpty(buffer, "The value of the Call-ID cannot be null or empty");
        return new CallIdHeaderImpl(buffer);
    }

    /**
     * Frame the {@link CallIdHeader} using its compact name.
     * 
     * @param compactForm
     * @param buffer
     * @return
     * @throws SipParseException
     */
    static CallIdHeader frameCompact(final Buffer buffer) throws SipParseException {
        assertNotEmpty(buffer, "The value of the Call-ID cannot be null or empty");
        return new CallIdHeaderImpl(true, buffer);
    }

    static CallIdHeader create() {
        return new CallIdHeaderImpl();
    }

}
