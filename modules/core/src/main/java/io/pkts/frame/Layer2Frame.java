/**
 * 
 */
package io.pkts.frame;

import io.pkts.packet.MACPacket;
import io.pkts.packet.PacketParseException;

/**
 * A frame representing the Data Link Layer in the OSI model
 * 
 * @author jonas@jonasborjesson.com
 */
public interface Layer2Frame extends Frame {

    /**
     * Layer 2 frames parse their content into {@link MACPacket}.
     * 
     * This is not 100% true since MAC is really a sub-layer of the Data Link
     * Layer (layer 2) and there is also another sub-layer (LLC - Logical Link
     * Control) but for now we will ignore this.
     * 
     * {@inheritDoc}
     */
    @Override
    MACPacket parse() throws PacketParseException;

}
