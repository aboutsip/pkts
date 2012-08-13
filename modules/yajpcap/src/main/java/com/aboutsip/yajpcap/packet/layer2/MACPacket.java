/**
 * 
 */
package com.aboutsip.yajpcap.packet.layer2;

import com.aboutsip.yajpcap.packet.Packet;

/**
 * Represents a packet from the Data Link Layer (DLL - Layer 2 in the OSI
 * model). Now, this is not 100% accurate since the MAC layer is really a sub
 * layer of DLL but whatever, it works for now.
 * 
 * @author jonas@jonasborjesson.com
 */
public interface MACPacket extends Packet {

    String getSourceMacAddress();

    String getDestinationMacAddress();

}
