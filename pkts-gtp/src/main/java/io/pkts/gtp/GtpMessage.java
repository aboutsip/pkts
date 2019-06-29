package io.pkts.gtp;

import io.snice.buffer.Buffer;
import io.snice.buffer.ReadableBuffer;

/**
 *
 */
public interface GtpMessage {

    static GtpMessage frame(Buffer buffer) throws GtpParseException, IllegalArgumentException {
        return null;
    }

    static GtpMessage frame(ReadableBuffer buffer) throws GtpParseException, IllegalArgumentException {
        return null;
    }

}
