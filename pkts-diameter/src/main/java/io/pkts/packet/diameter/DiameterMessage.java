package io.pkts.packet.diameter;

import io.pkts.buffer.Buffer;

/**
 * @author jonas@jonasborjesson.com
 */
public interface DiameterMessage {

    DiameterHeader getHeader();

    static DiameterMessage frame(Buffer buffer) {
        return null;
    }
}
