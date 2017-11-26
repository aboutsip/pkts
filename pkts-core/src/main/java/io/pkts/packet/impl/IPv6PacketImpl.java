/**
 *
 */
package io.pkts.packet.impl;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.framer.FramingException;
import io.pkts.framer.TCPFramer;
import io.pkts.framer.UDPFramer;
import io.pkts.packet.IPv6Packet;
import io.pkts.packet.Packet;
import io.pkts.packet.PacketParseException;
import io.pkts.protocol.Protocol;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author epall@google.com
 */
public final class IPv6PacketImpl extends AbstractPacket implements IPv6Packet {
    public static final int FIXED_HEADER_LENGTH = 40;

    private static final UDPFramer udpFramer = new UDPFramer();

    private static final TCPFramer tcpFramer = new TCPFramer();

    private final Buffer headers;

    private final int nextProtocol;

    public IPv6PacketImpl(final Packet parent, final Buffer headers, final int nextProtocol, final Buffer payload) {
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
    public byte[] getRawSourceIP() {
        final Buffer tmp = Buffers.createBuffer(128 / 8);
        this.headers.getBytes(8, tmp);
        return tmp.getArray();
    }

    @Override
    public void setSourceIP(final String sourceIp) {
        // TODO
        throw new RuntimeException("Not implemented");
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public String getSourceIP() {
        try {
            return InetAddress.getByAddress(getRawSourceIP()).getHostAddress();
        } catch (final UnknownHostException e) {
            return null;
        }
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
    public byte[] getRawDestinationIP() {
        final Buffer tmp = Buffers.createBuffer(128 / 8);
        this.headers.getBytes(24, tmp);
        return tmp.getArray();
    }

    @Override
    public void setDestinationIP(final String destinationIP) {
        // TODO
        throw new RuntimeException("Not implemented");
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public String getDestinationIP() {
        try {
            return InetAddress.getByAddress(getRawDestinationIP()).getHostAddress();
        } catch (final UnknownHostException e) {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void verify() {
        // nothing to do for ip packets
    }

    @Override
    public long getArrivalTime() {
        return getParentPacket().getArrivalTime();
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

    public void setRawSourceIP(final byte[] ip) {
        this.headers.setWriterIndex(8);
        this.headers.write(ip);
    }

    public void setRawDestinationIP(final byte[] ip) {
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
                throw new PacketParseException(0, "Unsupported inner protocol for IPv6");
            }
        } else {
            throw new PacketParseException(0, String.format("Unknown protocol %d inside IPv6 packet", nextProtocol));
        }

    }

    @Override
    public int getVersion() {
        return 6;
    }

    /**
     * The length of the IPv6 headers
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
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isFragmented() {
        return (getHeader(EXTENSION_FRAGMENT) != null);
    }

    @Override
    public short getFragmentOffset() {
        final Buffer fragmentHeader = getHeader(EXTENSION_FRAGMENT);
        if (fragmentHeader != null) {
            final int offset = (fragmentHeader.getShort(2) & 0xFFF8) >> 3;
            return (short) (offset * 8);
        }
        return -1;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("IPv6 ");
        sb.append(" Total Length: ").append(getTotalIPLength())
                .append(" ID: ").append(getIdentification())
                .append(" Fragment Offset: ").append(getFragmentOffset());
        return sb.toString();
    }

    private Buffer getHeader(final int extensionNumber) {
        try {
            int startOfHeader = IPv6PacketImpl.FIXED_HEADER_LENGTH;
            byte thisHeaderNumber = this.headers.getByte(6);
            byte nextHeaderNumber;
            int headerExtensionLen;
            // advance to next header
            while (startOfHeader < this.headers.capacity()) {
                switch (extensionNumber) {
                    case IPv6Packet.EXTENSION_HOP_BY_HOP:
                    case IPv6Packet.EXTENSION_ROUTING:
                    case IPv6Packet.EXTENSION_DESTINATION_OPTIONS:
                        nextHeaderNumber = this.headers.getByte(startOfHeader);
                        headerExtensionLen = 8 + this.headers.getByte(startOfHeader + 1) * 8;
                        break;
                    case IPv6Packet.EXTENSION_FRAGMENT:
                        nextHeaderNumber = this.headers.getByte(startOfHeader);
                        headerExtensionLen = 8;
                        break;
                    case IPv6Packet.EXTENSION_AH:
                        nextHeaderNumber = this.headers.getByte(startOfHeader);
                        headerExtensionLen = 4 * (this.headers.getByte(startOfHeader + 1) + 2);
                        break;
                    case IPv6Packet.EXTENSION_ESP:
                        // TODO figure out how length is even parsed...
                    default:
                        // out of headers to check
                        return null;
                }
                if (thisHeaderNumber == extensionNumber) {
                    // extract the header and return in a buffer
                    return this.headers.slice(startOfHeader, startOfHeader + headerExtensionLen);
                }
                // advance to next header
                startOfHeader += headerExtensionLen;
                thisHeaderNumber = nextHeaderNumber;
            }
        } catch (final IOException e) {
            throw new PacketParseException(0, String.format("Error extracting extension header %d", extensionNumber), e);
        }
        return null;
    }
}
