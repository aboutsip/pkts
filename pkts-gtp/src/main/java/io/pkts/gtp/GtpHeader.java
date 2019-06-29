package io.pkts.gtp;

import io.pkts.gtp.control.Gtp1Header;
import io.pkts.gtp.control.Gtp2Header;
import io.snice.buffer.Buffer;
import io.snice.buffer.ReadableBuffer;

import static io.snice.preconditions.PreConditions.assertNotNull;

public interface GtpHeader {

    static GtpHeader frame(final Buffer buffer) throws IllegalArgumentException, GtpParseException {
        assertNotNull(buffer, "The buffer cannot be null");
        return frame(buffer.toReadableBuffer());
    }

    static GtpHeader frame(final ReadableBuffer buffer) throws IllegalArgumentException, GtpParseException {
        assertNotNull(buffer, "The buffer cannot be null");
        final int version = (buffer.getByte(buffer.getReaderIndex()) & 11100000) >> 5;
        switch (version) {
            case 1:
                return Gtp1Header.frame(buffer);
            case 2:
                return Gtp2Header.frame(buffer);
            default:
                throw new GtpParseException(buffer.getReaderIndex(), "Unknown (" + version + ") GTP protocol version");
        }
    }

    default Gtp1Header toGtp1Header() throws ClassCastException {
        throw new ClassCastException("Unable to cast a " + getClass().getName() + " into a " + Gtp1Header.class.getName());
    }

    default Gtp2Header toGtp2Header() throws ClassCastException {
        throw new ClassCastException("Unable to cast a " + getClass().getName() + " into a " + Gtp2Header.class.getName());
    }

    int getVersion();

    int getLength();

    /**
     *
     */
    int getMessageTypeDecimal();

}
