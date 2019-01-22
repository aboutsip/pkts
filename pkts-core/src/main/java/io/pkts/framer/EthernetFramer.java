/**
 * 
 */
package io.pkts.framer;

import java.io.IOException;
import java.io.OutputStream;

import io.pkts.buffer.Buffer;
import io.pkts.frame.UnknownEtherType;
import io.pkts.packet.MACPacket;
import io.pkts.packet.PCapPacket;
import io.pkts.packet.impl.MACPacketImpl;
import io.pkts.protocol.Protocol;

/**
 * Simple framer for framing Ethernet frames
 * 
 * @author jonas@jonasborjesson.com
 */
public class EthernetFramer implements Framer<PCapPacket, MACPacket> {

    public EthernetFramer() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Protocol getProtocol() {
        return Protocol.ETHERNET_II;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MACPacket frame(final PCapPacket parent, final Buffer buffer) throws IOException, FramingException {
        if (parent == null) {
            throw new IllegalArgumentException("The parent frame cannot be null");
        }

        // final Buffer destMacAddress = buffer.readBytes(6);
        // final Buffer srcMacAddress = buffer.readBytes(6);
        // final byte b1 = buffer.readByte();
        // final byte b2 = buffer.readByte();

        if (buffer.getReadableBytes() < 14) {
            throw new FramingException("not enough bytes for header", getProtocol());
        }
        Buffer headers;
        try {
            EtherType etherType = getEtherType(buffer.getByte(12), buffer.getByte(13));
            if (etherType == EtherType.Dot1Q) {
                getEtherType(buffer.getByte(16), buffer.getByte(17));
                headers = buffer.readBytes(18);
            } else {
                headers = buffer.readBytes(14);
            }
        } catch (IndexOutOfBoundsException e) {
            throw new FramingException("not enough bytes for header", getProtocol());
        } catch (final UnknownEtherType e) {
            throw new FramingException(String.format("unknown ether type: 0x%02x%02x", e.getB1(), e.getB2()), getProtocol());
        }

        final Buffer payload = buffer.slice(buffer.capacity());
        return new MACPacketImpl(Protocol.ETHERNET_II, parent, headers, payload);
    }

    public static EtherType getEtherType(final byte b1, final byte b2) throws UnknownEtherType {
        final EtherType type = getEtherTypeSafe(b1, b2);
        if (type != null) {
            return type;
        }

        // will implement as we need to
        throw new UnknownEtherType(b1, b2);
    }

    public static EtherType getEtherTypeSafe(final byte b1, final byte b2) {
        int type = ((b1 << 8) & 0xFF00) | (b2 & 0xFF);
        if (type < 1536) {
            return EtherType.None;
        }
        for (EtherType t: EtherType.values()) {
          if (b1 == t.b1 && b2 == t.b2) {
              return t;
          }
        }

        return null;
    }

    @Override
    public boolean accept(final Buffer data) {
        return false;
    }

    public enum EtherType {
        IPv4((byte) 0x08, (byte) 0x00),
        ARP((byte) 0x08, (byte) 0x06),
        IPv6((byte) 0x86, (byte) 0xdd),
        LLDP((byte) 0x88, (byte) 0xcc),
        EAPOL((byte) 0x88, (byte) 0x8e),
        // Representing EtherType < 1536, which is actually a length of the frame and not a meaningful type
        None((byte) 0x00, (byte) 0x00),
        Dot1Q((byte) 0x81, (byte) 0x00)
        ;

        private final byte b1;
        private final byte b2;

        EtherType(final byte b1, final byte b2) {
            this.b1 = b1;
            this.b2 = b2;
        }

        public byte getB1() {
            return b1;
        }

        public byte getB2() {
            return b2;
        }

        public void write(final OutputStream out) throws IOException {
            out.write(this.b1);
            out.write(this.b2);
        }
    }

}
