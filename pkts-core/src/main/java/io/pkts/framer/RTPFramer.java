/**
 * 
 */
package io.pkts.framer;

import io.pkts.buffer.Buffer;
import io.pkts.packet.TransportPacket;
import io.pkts.packet.rtp.RtpPacket;
import io.pkts.packet.rtp.impl.RtpPacketImpl;
import io.pkts.protocol.Protocol;

import java.io.IOException;

/**
 * @author jonas@jonasborjesson.com
 */
public final class RTPFramer implements Framer<TransportPacket> {

    /**
     * 
     */
    public RTPFramer() {
        // left empty intentionally
    }

    @Override
    public Protocol getProtocol() {
        return Protocol.RTP;
    }

    /**
     * There is no real good test to make sure that the data indeed is an RTP
     * packet. Appendix 2 in RFC3550 describes one way of doing it but you
     * really need a sequence of packets in order to be able to determine if
     * this indeed is a RTP packet or not. The best is to analyze the session
     * negotiation but here we are just looking at a single packet so can't do
     * that.
     * 
     * {@inheritDoc}
     */
    @Override
    public boolean accept(final Buffer data) throws IOException {
        // a RTP packet has at least 12 bytes. Check that
        if (data.getReadableBytes() < 12) {
            // not enough bytes but see if we actually could
            // get another 12 bytes by forcing the underlying
            // implementation to read further ahead
            data.markReaderIndex();
            try {
                final Buffer b = data.readBytes(12);
                if (b.capacity() < 12) {
                    return false;
                }
            } catch (final IndexOutOfBoundsException e) {
                // guess not...
                // System.err.println("But still!!!");
                // e.printStackTrace();
            }
            data.resetReaderIndex();
        }

        // check the version. Currently we only check for version 2
        // and if this is true then we'll just return true.
        final byte b = data.getByte(0);
        return (b & 0xC0) >> 6 == 0x02;
    }

    @Override
    public RtpPacket frame(final TransportPacket parent, final Buffer buffer) throws IOException {
        if (parent == null) {
            throw new IllegalArgumentException("The parent frame cannot be null");
        }

        // An RTP packet has a least 12 bytes but can contain more depending on
        // extensions, padding etc. Figure that out.
        final Buffer headers = buffer.readBytes(12);
        final Byte b = headers.getByte(0);
        final boolean hasPadding = (b & 0x20) == 0x020;
        final boolean hasExtension = (b & 0x10) == 0x010;
        final int csrcCount = b & 0x0F;

        if (hasExtension) {
            final short extensionHeaders = buffer.readShort();
            final int length = buffer.readUnsignedShort();
            final Buffer extensionData = buffer.readBytes(length);
        }

        if (hasPadding || hasExtension || csrcCount > 0) {
            // throw new RuntimeException("TODO - have not implemented the case of handling padding, extensions etc");
        }

        final Buffer payload = buffer.slice();
        return new RtpPacketImpl(parent, headers, payload);
    }
}
