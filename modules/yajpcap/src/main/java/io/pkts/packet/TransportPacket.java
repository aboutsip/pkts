/**
 * 
 */
package io.pkts.packet;

/**
 * Represents a Transport packet (Layer 4 in the OSI)
 * 
 * @author jonas@jonasborjesson.com
 * 
 */
public interface TransportPacket extends IPPacket, Cloneable {

    int getSourcePort();

    int getDestinationPort();

    @Override
    TransportPacket clone();

}
