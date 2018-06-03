package io.pkts.packet.diameter;

import java.io.IOException;

import io.pkts.buffer.Buffer;
import io.pkts.packet.diameter.impl.DiameterParser;

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

    long getApplicationId();

    long getHopByHopId();

    long getEndToEndId();

    static DiameterHeader frame(final Buffer buffer) throws DiameterParseException, IOException {
        return DiameterParser.frameHeader(buffer);
    }

}
