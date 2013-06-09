package io.pkts.frame;

import io.pkts.packet.PacketParseException;
import io.pkts.packet.TransportPacket;

/**
 * A frame representing the Transport Layer in the OSI model
 * 
 * @author jonas@jonasborjesson.com
 */
public interface Layer4Frame extends Frame {

    /**
     * Layer 4 frames parse their content into transport packets
     * 
     * {@inheritDoc}
     */
    @Override
    TransportPacket parse() throws PacketParseException;

}
