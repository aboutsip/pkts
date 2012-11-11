package com.aboutsip.yajpcap.frame;

import com.aboutsip.yajpcap.packet.PacketParseException;
import com.aboutsip.yajpcap.packet.TransportPacket;

/**
 * A frame representing the Transport Layer in the OSI model
 * 
 * @author jonas@jonasborjesson.com
 */
public interface Layer4Frame extends Frame {

    /**
     * Layer 4 frames parse their content into transport packets
     * 
     * {@inheritDoc}
     */
    @Override
    TransportPacket parse() throws PacketParseException;

}
