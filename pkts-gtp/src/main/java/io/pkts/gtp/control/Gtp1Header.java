package io.pkts.gtp.control;

import io.pkts.gtp.GtpHeader;
import io.pkts.gtp.GtpParseException;
import io.pkts.gtp.GtpVersionException;
import io.pkts.gtp.Teid;
import io.pkts.gtp.control.impl.Gtp1HeaderImpl;
import io.snice.buffer.Buffer;
import io.snice.buffer.ReadableBuffer;

import java.util.Optional;

import static io.snice.preconditions.PreConditions.assertNotNull;

public interface Gtp1Header extends GtpHeader {

    static Gtp1Header frame(final Buffer buffer) throws IllegalArgumentException, GtpParseException, GtpVersionException {
        assertNotNull(buffer, "The buffer cannot be null");
        return frame(buffer.toReadableBuffer());
    }

    static Gtp1Header frame(final ReadableBuffer buffer) throws IllegalArgumentException, GtpParseException, GtpVersionException {
        return Gtp1HeaderImpl.frame(buffer);
    }

    Teid getTeid();

    @Override
    default int getVersion() {
        return 1;
    }

    @Override
    default Gtp1Header toGtp1Header() throws ClassCastException {
        return this;
    }
}
