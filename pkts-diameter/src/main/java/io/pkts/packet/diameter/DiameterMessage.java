package io.pkts.packet.diameter;

import io.pkts.buffer.Buffer;
import io.pkts.packet.diameter.impl.DiameterParser;

import java.io.IOException;
import java.util.List;

/**
 * @author jonas@jonasborjesson.com
 */
public interface DiameterMessage {

    DiameterHeader getHeader();

    List<Avp> getAllAvps();

    static DiameterMessage frame(final Buffer buffer) throws IOException {
        return DiameterParser.frame(buffer);
    }
}
