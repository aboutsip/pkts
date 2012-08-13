/**
 * 
 */
package com.aboutsip.yajpcap.packet.layer3;

import com.aboutsip.yajpcap.packet.layer2.MACPacket;

/**
 * Represents a packet from the Network Layer (layer 3). Actually, to be
 * completely honest, the model implemented (at least so far) is more geared
 * towards what is commonly referred to as the Internet Layer and is strictly
 * speaking not quite the same as the Network Layer as specified by the OSI
 * model. However, until it becomes an issue this little "issue" is going to be
 * ignored and for now the Network Layer is equal to the Internet Layer.
 * 
 * The current version of YAJPcap is focused on IP anyway so...
 * 
 * @author jonas@jonasborjesson.com
 */
public interface IPPacket extends MACPacket {

    String getSourceIP();

    String getDestinationIP();
}
