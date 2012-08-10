/**
 * 
 */
package com.aboutsip.yajpcap.frame;

import java.io.IOException;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.yajpcap.framer.EthernetFramer;
import com.aboutsip.yajpcap.framer.Framer;
import com.aboutsip.yajpcap.framer.FramerManager;
import com.aboutsip.yajpcap.packet.Packet;
import com.aboutsip.yajpcap.packet.PacketParseException;
import com.aboutsip.yajpcap.packet.impl.EthernetPacketImpl;
import com.aboutsip.yajpcap.protocol.Protocol;

/**
 * SLL is the linux cooked-mode capture.
 * 
 * http://wiki.wireshark.org/SLL
 * 
 * @author jonas@jonasborjesson.com
 */
public final class SllFrame extends AbstractFrame {

    private final Buffer headers;

    /**
     * @param framerManager
     * @param p
     * @param payload
     */
    public SllFrame(final FramerManager framerManager, final Buffer headers, final Buffer payload) {
        super(framerManager, Protocol.SLL, payload);
        this.headers = headers;
    }

    public EthernetFrame.EtherType getType() throws IndexOutOfBoundsException, IOException {
        final byte b1 = this.headers.getByte(14);
        final byte b2 = this.headers.getByte(15);

        try {
            return EthernetFramer.getEtherType(b1, b2);
        } catch (final UnknownEtherType e) {
            throw new RuntimeException("uknown ether type");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Packet parse() throws PacketParseException {
        try {
            final Buffer src = this.headers.slice(6, 12);
            final String source = EthernetFrame.toHexString(src);
            return new EthernetPacketImpl(source, source);
        } catch (final IOException e) {
            throw new RuntimeException("TODO: need to parse exception or something", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Frame framePayload(final FramerManager framerManager, final Buffer buffer) throws IOException {
        switch (getType()) {
        case IPv4:
            final Framer framer = framerManager.getFramer(Protocol.IPv4);
            return framer.frame(buffer);
        case IPv6:
            throw new RuntimeException("Cant do ipv6 right now");
        default:
            throw new RuntimeException("Uknown ether type");
        }
    }
}
