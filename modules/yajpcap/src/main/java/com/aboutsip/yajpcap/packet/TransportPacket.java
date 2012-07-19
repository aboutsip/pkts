/**
 * 
 */
package com.aboutsip.yajpcap.packet;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public interface TransportPacket extends Packet {

    boolean isUDP();

    boolean isTCP();

    int getSourcePort();

    int getDestinationPort();

}
