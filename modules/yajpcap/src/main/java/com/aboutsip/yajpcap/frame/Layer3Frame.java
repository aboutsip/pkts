/**
 * 
 */
package com.aboutsip.yajpcap.frame;

import com.aboutsip.yajpcap.packet.IPPacket;
import com.aboutsip.yajpcap.packet.PacketParseException;

/**
 * A frame representing the Network Layer in the OSI model
 * 
 * @author jonas@jonasborjesson.com
 */
public interface Layer3Frame extends Frame {

    /**
     * Layer 3 frames parse their content into {@link IPPacket}.
     * 
     * {@inheritDoc}
     */
    @Override
    IPPacket parse() throws PacketParseException;

}
