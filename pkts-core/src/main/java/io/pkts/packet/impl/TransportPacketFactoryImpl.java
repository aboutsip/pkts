/**
 * 
 */
package io.pkts.packet.impl;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.frame.PcapRecordHeader;
import io.pkts.packet.IPv4Packet;
import io.pkts.packet.MACPacket;
import io.pkts.packet.PCapPacket;
import io.pkts.packet.PacketFactory;
import io.pkts.packet.TransportPacket;
import io.pkts.packet.TransportPacketFactory;
import io.pkts.packet.UDPPacket;
import io.pkts.protocol.IllegalProtocolException;
import io.pkts.protocol.Protocol;

/**
 * @author jonas@jonasborjesson.com
 */
public final class TransportPacketFactoryImpl implements TransportPacketFactory {

    /**
     * Raw Ethernet II frame with a source and destination mac address of
     * 00:00:00:00:00:00 and the type is set to IP (0800 - the last two bytes).
     */
    private static final byte[] ehternetII = new byte[] {
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x08, (byte) 0x00 };

    /**
     * Raw IPv4 frame with source and destination IP:s set to 127.0.0.1 and a
     * protocol for UDP. The length and checksums must be corrected when
     * generating a new packet based on this template.
     */
    private static final byte[] ipv4 = new byte[] {
            (byte) 0x45, (byte) 0x00, (byte) 0x01, (byte) 0xed, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x40, (byte) 0x11, (byte) 0x3a, (byte) 0xfe, (byte) 0x7f, (byte) 0x00, (byte) 0x00, (byte) 0x01,
            (byte) 0x7f, (byte) 0x00, (byte) 0x00, (byte) 0x01 };

    /**
     * Raw UDP frame where the source port is 5090 and the destination port
     * 5060. You will certainly have to change the length of the UDP frame based
     * on your payload but you also need to re-calculate the checksum. And you
     * probably want to
     */
    private static final byte[] udp = new byte[] {
            (byte) 0x13, (byte) 0xe2, (byte) 0x13, (byte) 0xc4, (byte) 0x00, (byte) 0x00, (byte) 0xff, (byte) 0xec };

    /**
     * The total size of an empty UDP packet.
     */
    private static final int udpLength = ehternetII.length + ipv4.length + udp.length;

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
    public TransportPacket create(final Protocol protocol, final String srcAddress, final int srcPort,
            final String destAddress, final int destPort,
            final Buffer payload) throws IllegalArgumentException, IllegalProtocolException {

        final TransportPacket pkt = createUdpInternal(payload);
        final IPv4Packet ipPkt = (IPv4Packet) pkt.getParentPacket();
        ipPkt.setDestinationIP(destAddress);
        ipPkt.setSourceIP(srcAddress);
        pkt.setDestinationPort(destPort);
        pkt.setSourcePort(srcPort);
        ipPkt.reCalculateChecksum();
        return pkt;
    }

    private UDPPacket createUdpInternal(final long ts, final Buffer payload) {
        final int payloadSize = payload != null ? payload.getReadableBytes() : 0;
        final Buffer ethernet = Buffers.wrapAndClone(this.ehternetII);
        final Buffer ipv4 = Buffers.wrapAndClone(this.ipv4);

        final PcapRecordHeader pcapRecordHeader = PcapRecordHeader.createDefaultHeader(ts);
        pcapRecordHeader.setCapturedLength(this.udpLength + payloadSize);
        pcapRecordHeader.setTotalLength(this.udpLength + payloadSize);

        final PCapPacket pkt = new PCapPacketImpl(pcapRecordHeader, null);
        final MACPacket mac = MACPacketImpl.create(pkt, ethernet);

        final IPv4PacketImpl ipPacket = new IPv4PacketImpl(mac, ipv4, 0, null);
        ipPacket.setTotalLength(this.ipv4.length + this.udp.length + payloadSize);

        final UdpPacketImpl udp = new UdpPacketImpl(ipPacket, Buffers.wrap(new byte[8]), payload);
        udp.setLength(8 + payloadSize);
        return udp;
    }

    private UDPPacket createUdpInternal(final Buffer payload) {
        final long ts = System.currentTimeMillis();
        return createUdpInternal(ts, payload);
    }

    @Override
    public TransportPacket create(final Protocol protocol, final byte[] srcAddress, final int srcPort,
            final byte[] destAddress, final int destPort, final Buffer payload) throws IllegalArgumentException,
            IllegalProtocolException {
        final TransportPacket pkt = createUdpInternal(payload);
        final IPv4Packet ipPkt = (IPv4Packet) pkt.getParentPacket();
        ipPkt.setSourceIP(srcAddress[0], srcAddress[1], srcAddress[2], srcAddress[3]);
        ipPkt.setDestinationIP(destAddress[0], destAddress[1], destAddress[2], destAddress[3]);
        pkt.setDestinationPort(destPort);
        pkt.setSourcePort(srcPort);
        ipPkt.reCalculateChecksum();
        return pkt;
    }

    @Override
    public UDPPacket createUDP(final long ts, final Buffer payload) throws IllegalArgumentException,
            IllegalProtocolException {
        return createUdpInternal(ts, payload);
    }

    @Override
    public UDPPacket createUDP(final Buffer payload) throws IllegalArgumentException, IllegalProtocolException {
        return createUdpInternal(payload);
    }

}
