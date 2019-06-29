package io.pkts.gtp.impl;

import io.pkts.gtp.GtpHeader;
import io.pkts.gtp.GtpMessage;
import io.snice.buffer.Buffer;
import io.snice.buffer.ReadableBuffer;

import static io.snice.preconditions.PreConditions.assertNotNull;

/**
 * Base class for all things related to framing GTP messages.
 *
 * @author jonas@jonasborjesson.com
 */
public final class GtpFramer {

    public static GtpMessage frameGtpMessage(final Buffer buffer) {
        assertNotNull(buffer, "The buffer cannot be null");
        final ReadableBuffer readable = buffer.toReadableBuffer();
        final GtpHeader header = frameGtpHeader(readable);
        return null;
    }

    public static GtpHeader frameGtpHeader(final Buffer buffer) {
        assertNotNull(buffer, "The buffer cannot be null");
        return frameGtpHeader(buffer.toReadableBuffer());
    }

}
