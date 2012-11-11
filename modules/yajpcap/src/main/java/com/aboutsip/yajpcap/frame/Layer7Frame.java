package com.aboutsip.yajpcap.frame;

import com.aboutsip.yajpcap.packet.PacketParseException;
import com.aboutsip.yajpcap.packet.impl.ApplicationPacket;

public interface Layer7Frame extends Frame {

    /**
     * Layer 7 frames parse their content into Application packets
     * 
     * {@inheritDoc}
     */
    @Override
    ApplicationPacket parse() throws PacketParseException;

}
