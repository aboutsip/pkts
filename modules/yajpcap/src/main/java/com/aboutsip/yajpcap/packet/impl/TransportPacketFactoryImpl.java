/**
 * 
 */
package com.aboutsip.yajpcap.packet.impl;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.buffer.Buffers;
import com.aboutsip.yajpcap.packet.IPPacket;
import com.aboutsip.yajpcap.packet.IPPacketImpl;
import com.aboutsip.yajpcap.packet.MACPacket;
import com.aboutsip.yajpcap.packet.MACPacketImpl;
import com.aboutsip.yajpcap.packet.PacketFactory;
import com.aboutsip.yajpcap.packet.TransportPacket;
import com.aboutsip.yajpcap.packet.TransportPacketFactory;
import com.aboutsip.yajpcap.packet.TransportPacketImpl;
import com.aboutsip.yajpcap.protocol.IllegalProtocolException;
import com.aboutsip.yajpcap.protocol.Protocol;

/**
 * @author jonas@jonasborjesson.com
 */
public final class TransportPacketFactoryImpl implements TransportPacketFactory {

    /**
     * Raw Ethernet II frame with a source and destination mac address of
     * 00:00:00:00:00:00 and the type is set to IP (0800 - the last two bytes).
     */
    private final byte[] ehternetII = new byte[] {
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x08, (byte) 0x00 };

    /**
     * Raw IPv4 frame with source and destination IP:s set to 127.0.0.1 and a
     * protocol for UDP. The length and checksums must be corrected when
     * generating a new packet based on this template.
     */
    private final byte[] ipv4 = new byte[] {
            (byte) 0x45, (byte) 0x00, (byte) 0x01, (byte) 0xed, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x40, (byte) 0x11, (byte) 0x3a, (byte) 0xfe, (byte) 0x7f, (byte) 0x00, (byte) 0x00, (byte) 0x01,
            (byte) 0x7f, (byte) 0x00, (byte) 0x00, (byte) 0x01 };

    /**
     * Raw UDP frame where the source port is 5090 and the destination port
     * 5060. You will certainly have to change the length of the UDP frame based
     * on your payload but you also need to re-calculate the checksum. And you
     * probably want to
     */
    private final byte[] udp = new byte[] {
            (byte) 0x13, (byte) 0xe2, (byte) 0x13, (byte) 0xc4, (byte) 0x01, (byte) 0xd9, (byte) 0xff, (byte) 0xec };

    /**
     * A reference to the main {@link PacketFactory}
     */
    private final PacketFactory packetFactory;

    /**
     * 
     */
    public TransportPacketFactoryImpl(final PacketFactory factory) {
        this.packetFactory = factory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TransportPacket create(final Protocol protocol, final String srcAddress, final int srcPort, final String destAddress, final int destPort,
            final Buffer payload) throws IllegalArgumentException, IllegalProtocolException {

        final byte[] ethernet = new byte[this.ehternetII.length];
        System.arraycopy(this.ehternetII, 0, ethernet, 0, this.ehternetII.length);
        final MACPacket mac = MACPacketImpl.create(null, Buffers.wrap(ethernet));

        final byte[] rawIpv4 = new byte[this.ipv4.length];
        System.arraycopy(this.ipv4, 0, rawIpv4, 0, this.ipv4.length);
        final Buffer ipHeaders = Buffers.wrap(rawIpv4);
        final IPPacket ipPacket = new IPPacketImpl(mac, ipHeaders, 0);
        ipPacket.setSourceIP(srcAddress);
        ipPacket.setDestinationIP(destAddress);

        return new TransportPacketImpl(ipPacket, true, srcPort, destPort);
    }


}
