/**
 * 
 */
package io.pkts.packet;

/**
 * @author jonas@jonasborjesson.com
 */
public interface UDPPacket extends TransportPacket {
    @Override
    IPPacket getParentPacket();

    int getChecksum();

    int getLength();
}
