/**
 * 
 */
package com.aboutsip.yajpcap.frame.layer3;

import com.aboutsip.yajpcap.packet.PacketParseException;
import com.aboutsip.yajpcap.packet.layer3.IPPacket;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public interface IPFrame extends Layer3Frame {

    /**
     * The IP version (4 or 6)
     * 
     * @return
     */
    int getVersion();

    /**
     * Get the length of the IP headers (in bytes)
     * 
     * @return
     */
    int getHeaderLength();

    /**
     * {@inheritDoc}
     */
    @Override
    IPPacket parse() throws PacketParseException;

}
