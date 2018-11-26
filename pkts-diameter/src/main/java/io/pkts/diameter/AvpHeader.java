package io.pkts.diameter;

import io.pkts.buffer.ReadOnlyBuffer;
import io.pkts.diameter.impl.DiameterParser;

import java.io.IOException;
import java.util.Optional;

/**
 * The {@link AvpHeader} contains the AVP code, length, flags and potentially
 * the vendor specific ID.
 *
 * @author jonas@jonasborjesson.com
 */
public interface AvpHeader {

    static AvpHeader frame(final ReadOnlyBuffer buffer) throws DiameterParseException, IOException {
        return DiameterParser.frameAvpHeader(buffer);
    }

    long getCode();

    int getLength();

    Optional<Long> getVendorId();

    boolean isVendorSpecific();

    boolean isMandatory();

    boolean isProtected();
}
