package io.pkts.diameter;

import io.pkts.buffer.ReadOnlyBuffer;
import io.pkts.diameter.impl.DiameterParser;

import java.io.IOException;

/**
 *
 * @author jonas@jonasborjesson.com
 */
public interface DiameterHeader {

    /**
     * The only valid version is 1 and when framing a {@link DiameterHeader},
     * the framer will ensure that this is indeed true or else you cannot construct
     * a new header. If/when this changes in the future, simply override this method.
     *
     * @return
     */
    default int getVersion() {
        return 1;
    }

    int getLength();

    boolean isRequest();

    default boolean isResponse() {
        return !isRequest();
    }

    boolean isProxiable();

    boolean isError();

    boolean isPossiblyRetransmission();

    int getCommandCode();

    long getApplicationId();

    long getHopByHopId();

    long getEndToEndId();

    /**
     * If you'd like to ensure that the header is indeed a proper header then there are a few
     * things we can check. E.g., the version must be set to one. The last 4 bits of the Command Flags
     * are reserved and not set per specification and the length must be a multiple of 4.
     *
     * @return
     */
    boolean validate();

    static DiameterHeader frame(final ReadOnlyBuffer buffer) throws DiameterParseException, IOException {
        return DiameterParser.frameHeader(buffer);
    }

}
