/**
 * 
 */
package io.pkts.packet.impl;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.framer.TCPFramer;
import io.pkts.framer.UDPFramer;
import io.pkts.packet.IPv6Packet;
import io.pkts.packet.PCapPacket;
import io.pkts.packet.Packet;
import io.pkts.packet.PacketParseException;
import io.pkts.protocol.Protocol;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author epall@google.com
 */
public final class IPv6PacketImpl extends AbstractPacket implements IPv6Packet {
    public static final int FIXED_HEADER_LENGTH = 40;

    private static final UDPFramer udpFramer = new UDPFramer();

    private static final TCPFramer tcpFramer = new TCPFramer();

    private final Buffer headers;

    private final int nextProtocol;

    /**
     *
     */
    public IPv6PacketImpl(final Packet parent, final Buffer headers, int nextProtocol, final Buffer payload) {
        super(Protocol.IPv6, parent, payload);
        assert parent != null;
        assert headers != null;
        this.headers = headers;
        this.nextProtocol = nextProtocol;
    }

    /**
     * Get the raw source ip.
     * 
     * Note, these are the raw bits and should be treated as such. If you really
     * want to print it, then you should treat it as unsigned
     * 
     * @return
     */
    @Override
    public byte[] getRawSourceIp() {
        Buffer tmp = Buffers.createBuffer(128 / 8);
        this.headers.getBytes(8, tmp);
        return tmp.getArray();
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public String getSourceIP() {
        throw new RuntimeException("TODO");
    }

    /**
     * Get the raw destination ip.
     * 
     * Note, these are the raw bits and should be treated as such. If you really
     * want to print it, then you should treat it as unsigned
     * 
     * @return
     */
    @Override
    public byte[] getRawDestinationIp() {
        Buffer tmp = Buffers.createBuffer(128 / 8);
        this.headers.getBytes(24, tmp);
        return tmp.getArray();
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public String getDestinationIP() {
        throw new RuntimeException("TODO");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void verify() {
        // nothing to do for ip packets
    }

    @Override
    public void write(final OutputStream out, final Buffer payload) throws IOException {
        final Buffer pkt = Buffers.wrap(this.headers, payload);
        getParentPacket().write(out, pkt);
    }

    @Override
    public int getTotalIPLength() {
        return FIXED_HEADER_LENGTH + this.headers.getUnsignedShort(4);
    }

    public void setSourceIP(final byte[] ip) {
        this.headers.setWriterIndex(8);
        this.headers.write(ip);
    }

    public void setDestinationIP(final byte[] ip) {
        this.headers.setWriterIndex(24);
        this.headers.write(ip);
    }

    @Override
    public IPv6Packet clone() {
        final Packet parent = getParentPacket().clone();
        final IPv6Packet pkt = new IPv6PacketImpl(parent, this.headers.clone(), this.nextProtocol, getPayload().clone());
        return pkt;
    }

    @Override
    public Packet getNextPacket() throws IOException {
        final Buffer payload = getPayload();
        if (payload == null) {
            return null;
        }

        final Protocol protocol = Protocol.valueOf((byte) nextProtocol);
        if (protocol != null) {
            switch (protocol) {
            case UDP:
                return udpFramer.frame(this, payload);
            case TCP:
                return tcpFramer.frame(this, payload);
            default:
                throw new PacketParseException(0, "Unsupported inner protocol");
            }
        } else {
            throw new PacketParseException(0, "Unknown Protocol. Was this SCTP or something???");
        }

    }

    /**
     * The version of this ip frame, will always be 4
     * 
     * @return
     */
    @Override
    public int getVersion() {
        return 6;
    }

    /**
     * The length of the ipv4 headers
     * 
     * @return
     */
    @Override
    public int getHeaderLength() {
        return this.headers.capacity();
    }

    @Override
    public int getIdentification() {
        try {
            return (this.headers.getByte(1) & 0x0F) << 16 | this.headers.getUnsignedShort(2);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        // TODO
        return "IPv6";
    }

}
