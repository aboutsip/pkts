package io.pkts.packet.diameter;

import io.pkts.buffer.Buffer;
import io.pkts.packet.diameter.impl.DiameterParser;

import java.io.IOException;
import java.util.Optional;

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

    long getCode();

    int getLength();

    Optional<Long> getVendorId();

    boolean isVendorSpecific();

    boolean isMandatory();

    boolean isProtected();
}
