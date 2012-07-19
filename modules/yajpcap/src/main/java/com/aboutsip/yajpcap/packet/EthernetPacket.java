/**
 * 
 */
package com.aboutsip.yajpcap.packet;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public interface EthernetPacket extends Packet {

    String getSourceMacAddress();

    String getDestinationMacAddress();

}
