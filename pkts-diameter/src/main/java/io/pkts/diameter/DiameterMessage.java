package io.pkts.diameter;

import io.pkts.buffer.ReadOnlyBuffer;
import io.pkts.diameter.impl.DiameterParser;

import java.io.IOException;
import java.util.List;

/**
 * @author jonas@jonasborjesson.com
 */
public interface DiameterMessage extends Cloneable {

    DiameterHeader getHeader();

    List<Avp> getAllAvps();

    DiameterMessage clone();

    static DiameterMessage frame(final ReadOnlyBuffer buffer) throws IOException {
        return DiameterParser.frame(buffer);
    }
}
