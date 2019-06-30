package io.pkts.gtp;

import io.pkts.gtp.control.Gtp2Message;
import io.pkts.gtp.control.InfoElement;
import io.snice.buffer.Buffer;
import io.snice.buffer.ReadableBuffer;

import java.util.List;

import static io.snice.preconditions.PreConditions.assertNotNull;

/**
 *
 */
public interface GtpMessage {

    static GtpMessage frame(final Buffer buffer) throws GtpParseException, IllegalArgumentException {
        assertNotNull(buffer, "The buffer cannot be null");
        return frame(buffer.toReadableBuffer());
    }

    static GtpMessage frame(final ReadableBuffer buffer) throws GtpParseException, IllegalArgumentException {
        final GtpHeader header = GtpHeader.frame(buffer);
        switch (header.getVersion()) {
            case 1:
                throw new RuntimeException("Not implemented yet");
            case 2:
                return Gtp2Message.frame(header.toGtp2Header(), buffer);
            default:
                // should not happen since the GTP Header should have complained but,
                // it is good practice to have a default path defined
                throw new GtpParseException(buffer.getReaderIndex(), "Unknown GTP protocol version");
        }
    }


    GtpHeader getHeader();

    List<? extends InfoElement> getInfoElements();

    default int getVersion() {
        return getHeader().getVersion();
    }

}
