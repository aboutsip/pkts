package io.pkts.packet.diameter;

import java.io.IOException;
import java.util.Optional;

import io.pkts.buffer.Buffer;
import io.pkts.packet.diameter.impl.DiameterParser;

/**
 * The {@link AvpHeader} contains the AVP code, length, flags and potentially
 * the vendor specific ID.
 *
 * @author jonas@jonasborjesson.com
 */
public interface AvpHeader {

    static AvpHeader frame(final Buffer buffer) throws DiameterParseException, IOException {
        return DiameterParser.frameAvpHeader(buffer);
    }

    int getCode();

    long getLength();

    Optional<Long> getVendorId();
}
