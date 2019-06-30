package io.pkts.gtp.control;

import io.pkts.gtp.GtpMessage;
import io.pkts.gtp.control.impl.Gtp2MessageImpl;
import io.snice.buffer.ReadableBuffer;

public interface Gtp2Message extends GtpMessage {

    static Gtp2Message frame(final Gtp2Header header, final ReadableBuffer buffer) {
        return Gtp2MessageImpl.frame(header, buffer);
    }


}
