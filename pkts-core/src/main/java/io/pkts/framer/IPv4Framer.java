/**
 * 
 */
package io.pkts.framer;

import io.pkts.buffer.Buffer;
import io.pkts.packet.IPv4Packet;
import io.pkts.packet.Packet;
import io.pkts.packet.impl.IPv4PacketImpl;
import io.pkts.protocol.Protocol;

import java.io.IOException;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public class IPv4Framer implements Framer<Packet, IPv4Packet> {
    public IPv4Framer() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Protocol getProtocol() {
        return Protocol.IPv4;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IPv4Packet frame(final Packet parent, final Buffer payload) throws IOException {

        if (parent == null) {
            throw new IllegalArgumentException("The parent frame cannot be null");
        }

        // the ipv4 headers are always 20 bytes unless
        // the length is greater than 5
        final Buffer headers = payload.readBytes(20);

        // byte 1, contains the version and the length
        final byte b = headers.getByte(0);
        // final int version = ((i >>> 28) & 0x0F);
        // final int length = ((i >>> 24) & 0x0F);

        final int version = b >>> 5 & 0x0F;
        final int headerLength = b & 0x0F;

        // byte 2 - dscp and ecn
        final byte tos = headers.getByte(1);

        // final int dscp = ((tos >>> 6) & 0x3B);
        // final int ecn = (tos & 0x03);

        // byte 3 - 4
        final int totalLength = headers.getUnsignedShort(2);

        // byte 5 - 6
        // final short id = headers.readShort();

        // this one contains flags + fragment offset

        // byte 7 - 8
        // final short flagsAndFragement = headers.readShort();

        // byte 9
        // final byte ttl = headers.readByte();

        // byte 10
        // final byte protocol = headers.getByte(9);

        // byte 11 - 12
        // final int checkSum = headers.readUnsignedShort();

        // byte 13 - 16
        // final int sourceIp = headers.readInt();

        // byte 17 - 20
        // final int destIp = headers.readInt();

        // if the length is greater than 5, then the frame
        // contains extra options so read those as well
        int options = 0;
        if (headerLength > 5) {
            // remember, this may have to be treated as unsigned
            // final int options = headers.readInt();
            options = payload.readInt();
        }

        //Trim off any padding from the upper layer, e.g. Ethernet padding for small packets.
        // If the captured frame was truncated, then use the truncated size for the data buffer, instead of what the
        // IPv4 header says its length should be.
        final int tcpLength = payload.getReaderIndex() + totalLength - (headerLength * 4);
        final Buffer data = payload.slice(Math.min(tcpLength, payload.capacity()));
        return new IPv4PacketImpl(parent, headers, options, data);
    }

    @Override
    public boolean accept(final Buffer data) {
        // TODO Auto-generated method stub
        return false;
    }

}
