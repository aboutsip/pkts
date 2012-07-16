/**
 * 
 */
package com.aboutsip.yajpcap.packet;

/**
 * @author jonas@jonasborjesson.com
 */
public interface IPPacket extends Packet {

    String getSourceIP();

    String getDestinationIP();

}
