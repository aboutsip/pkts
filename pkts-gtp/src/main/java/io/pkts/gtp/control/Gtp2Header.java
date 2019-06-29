package io.pkts.gtp.control;

import io.pkts.gtp.GtpHeader;
import io.pkts.gtp.GtpParseException;
import io.pkts.gtp.GtpVersionException;
import io.pkts.gtp.Teid;
import io.pkts.gtp.control.impl.Gtp2HeaderImpl;
import io.snice.buffer.Buffer;
import io.snice.buffer.ReadableBuffer;

import java.util.Optional;

import static io.snice.preconditions.PreConditions.assertNotNull;

public interface Gtp2Header extends GtpHeader {

    static Gtp2Header frame(final Buffer buffer) throws IllegalArgumentException, GtpParseException, GtpVersionException {
        assertNotNull(buffer, "The buffer cannot be null");
        return frame(buffer.toReadableBuffer());
    }

    static Gtp2Header frame(final ReadableBuffer buffer) throws IllegalArgumentException, GtpParseException, GtpVersionException {
        return Gtp2HeaderImpl.frame(buffer);
    }

    /**
     * The tunnel endpoint identifier is optional in GTPv2.
     */
    Optional<Teid> getTeid();

    @Override
    default int getVersion() {
        return 2;
    }

    @Override
    default Gtp2Header toGtp2Header() throws ClassCastException {
        return this;
    }
}
