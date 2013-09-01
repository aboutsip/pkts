package io.pkts;

import io.pkts.packet.Packet;

import java.io.IOException;

/**
 * 
 * @author jonas@jonasborjesson.com
 */
public interface FrameHandler {

    void nextFrame(Packet packet) throws IOException;

}
