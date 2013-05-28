/**
 * 
 */
package com.aboutsip.yajpcap.frame;

import java.io.IOException;
import java.io.ObjectOutput;
import java.io.OutputStream;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.yajpcap.framer.EthernetFramer;
import com.aboutsip.yajpcap.framer.Framer;
import com.aboutsip.yajpcap.framer.FramerManager;
import com.aboutsip.yajpcap.packet.MACPacket;
import com.aboutsip.yajpcap.packet.MACPacketImpl;
import com.aboutsip.yajpcap.packet.Packet;
import com.aboutsip.yajpcap.packet.PacketParseException;
import com.aboutsip.yajpcap.protocol.Protocol;

/**
 * SLL is the linux cooked-mode capture.
 * 
 * http://wiki.wireshark.org/SLL
 * 
 * @author jonas@jonasborjesson.com
 */
public final class SllFrame extends AbstractFrame implements Layer2Frame {

    private final Layer1Frame parentFrame;
    private final Buffer headers;

    /**
     * @param framerManager
     * @param p
     * @param payload
     */
    public SllFrame(final FramerManager framerManager, final PcapGlobalHeader header, final Layer1Frame parentFrame,
            final Buffer headers,
            final Buffer payload) {
        super(framerManager, header, Protocol.SLL, payload);
        assert parentFrame != null;
        this.parentFrame = parentFrame;
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
    public MACPacket parse() throws PacketParseException {
        try {
            final Packet parentPacket = this.parentFrame.parse();
            final Buffer src = this.headers.slice(6, 12);
            final String source = EthernetFrame.toHexString(src);
            return new MACPacketImpl(parentPacket, source, source);
        } catch (final IOException e) {
            throw new RuntimeException("TODO: need to parse exception or something", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        this.parentFrame.writeExternal(out);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(final OutputStream out) throws IOException {
        this.parentFrame.write(out);
        // out.write(this.headers.getArray());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Frame framePayload(final FramerManager framerManager, final Buffer buffer) throws IOException {
        switch (getType()) {
        case IPv4:
            final Framer framer = framerManager.getFramer(Protocol.IPv4);
            return framer.frame(this, buffer);
        case IPv6:
            throw new RuntimeException("Cant do ipv6 right now");
        default:
            throw new RuntimeException("Uknown ether type");
        }
    }

    @Override
    public long getArrivalTime() {
        return this.parentFrame.getArrivalTime();
    }
}
