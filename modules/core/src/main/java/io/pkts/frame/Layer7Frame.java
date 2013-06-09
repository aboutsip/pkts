package io.pkts.frame;

import io.pkts.packet.PacketParseException;
import io.pkts.packet.impl.ApplicationPacket;

public interface Layer7Frame extends Frame {

    /**
     * Layer 7 frames parse their content into Application packets
     * 
     * {@inheritDoc}
     */
    @Override
    ApplicationPacket parse() throws PacketParseException;

}
