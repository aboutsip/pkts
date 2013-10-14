package io.pkts;

import io.pkts.packet.Packet;

import java.io.IOException;

/**
 * 
 * Whenever there is a new packet being read off of the stream, the registered
 * {@link PacketHandler} will be called.
 * 
 * @author jonas@jonasborjesson.com
 */
public interface PacketHandler {

    /**
     * Will be called by the {@link Pcap} class as soon as it detects a new
     * {@link Packet} in the pcap stream.
     * 
     * @param packet
     *            the new {@link Packet} as read off of the pcap stream.
     * @throws IOException
     */
    void nextPacket(Packet packet) throws IOException;

}
