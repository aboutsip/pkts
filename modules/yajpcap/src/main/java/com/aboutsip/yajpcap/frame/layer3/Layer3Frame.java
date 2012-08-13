/**
 * 
 */
package com.aboutsip.yajpcap.frame.layer3;

import com.aboutsip.yajpcap.frame.Frame;
import com.aboutsip.yajpcap.packet.PacketParseException;
import com.aboutsip.yajpcap.packet.layer3.IPPacket;

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
