/**
 * 
 */
package io.pkts.packet.impl;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.IPPacket;
import io.pkts.packet.IPPacketImpl;
import io.pkts.packet.MACPacket;
import io.pkts.packet.MACPacketImpl;
import io.pkts.packet.Packet;
import io.pkts.packet.PacketFactory;
import io.pkts.packet.TransportPacket;
import io.pkts.packet.TransportPacketFactory;
import io.pkts.packet.TransportPacketImpl;
import io.pkts.protocol.IllegalProtocolException;
import io.pkts.protocol.Protocol;

import java.io.IOException;
import java.io.OutputStream;


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
    public TransportPacket create(final Protocol protocol, final String srcAddress, final int srcPort,
            final String destAddress, final int destPort,
            final Buffer payload) throws IllegalArgumentException, IllegalProtocolException {

        final byte[] ethernet = new byte[this.ehternetII.length];
        System.arraycopy(this.ehternetII, 0, ethernet, 0, this.ehternetII.length);
        final long ts = System.currentTimeMillis() * 1000;
        final Packet pkt = new SimplePacket(ts);
        final MACPacket mac = MACPacketImpl.create(pkt, Buffers.wrap(ethernet));

        final byte[] rawIpv4 = new byte[this.ipv4.length];
        System.arraycopy(this.ipv4, 0, rawIpv4, 0, this.ipv4.length);
        final Buffer ipHeaders = Buffers.wrap(rawIpv4);
        final IPPacket ipPacket = new IPPacketImpl(mac, ipHeaders, 0);
        ipPacket.setSourceIP(srcAddress);
        ipPacket.setDestinationIP(destAddress);

        return new TransportPacketImpl(ipPacket, true, srcPort, destPort);
    }

    @Override
    public TransportPacket create(final Protocol protocol, final byte[] srcAddress, final int srcPort,
            final byte[] destAddress, final int destPort, final Buffer payload) throws IllegalArgumentException,
            IllegalProtocolException {

        final byte[] ethernet = new byte[this.ehternetII.length];
        System.arraycopy(this.ehternetII, 0, ethernet, 0, this.ehternetII.length);
        final long ts = System.currentTimeMillis() * 1000;
        final Packet pkt = new SimplePacket(ts);
        final MACPacket mac = MACPacketImpl.create(pkt, Buffers.wrap(ethernet));

        final byte[] rawIpv4 = new byte[this.ipv4.length];
        System.arraycopy(this.ipv4, 0, rawIpv4, 0, this.ipv4.length);
        final Buffer ipHeaders = Buffers.wrap(rawIpv4);
        final IPPacket ipPacket = new IPPacketImpl(mac, ipHeaders, 0);
        try {
            ipPacket.setSourceIP(srcAddress[0], srcAddress[1], srcAddress[2], srcAddress[3]);
            ipPacket.setDestinationIP(destAddress[0], destAddress[1], destAddress[2], destAddress[3]);
        } catch (final IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Not enough bytes for setting an IPv4 address");
        }

        return new TransportPacketImpl(ipPacket, true, srcPort, destPort);
    }

    private static class SimplePacket implements Packet {
        private final long arrivalTime;

        public SimplePacket(final long arrivalTime) {
            this.arrivalTime = arrivalTime;
        }

        @Override
        public long getArrivalTime() {
            return this.arrivalTime;
        }

        @Override
        public void verify() {
            // TODO Auto-generated method stub

        }

        @Override
        public void write(final OutputStream out) throws IOException {
            throw new RuntimeException("Sorry, not implemented");
        }

        @Override
        public SimplePacket clone() {
            return new SimplePacket(this.arrivalTime);
        }

    }

}
