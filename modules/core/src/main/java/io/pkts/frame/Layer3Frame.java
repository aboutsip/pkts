/**
 * 
 */
package io.pkts.frame;

import io.pkts.packet.IPPacket;
import io.pkts.packet.PacketParseException;

/**
 * A frame representing the Network Layer in the OSI model
 * 
 * @author jonas@jonasborjesson.com
 */
public interface Layer3Frame extends Frame {

    /**
     * Layer 3 frames parse their content into {@link IPPacket}.
     * 
     * {@inheritDoc}
     */
    @Override
    IPPacket parse() throws PacketParseException;

}
