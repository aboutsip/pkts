/**
 * 
 */
package com.aboutsip.yajpcap.packet;


/**
 * Represents a Transport packet (Layer 4 in the OSI)
 * 
 * @author jonas@jonasborjesson.com
 * 
 */
public interface TransportPacket extends IPPacket {

    int getSourcePort();

    int getDestinationPort();

}
