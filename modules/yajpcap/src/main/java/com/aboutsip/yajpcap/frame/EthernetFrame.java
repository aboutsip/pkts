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
 * @author jonas@jonasborjesson.com
 */
public final class EthernetFrame extends AbstractFrame implements Layer2Frame {

    private final Layer1Frame parentFrame;
    private final Buffer destMacAddress;
    private final Buffer srcMacAddress;
    private final EtherType type;

    private final Buffer headers;

    /**
     * 
     */
    public EthernetFrame(final FramerManager framerManager, final PcapGlobalHeader header,
            final Layer1Frame parentFrame, final Buffer destMacAddress,
            final Buffer srcMacAddress,
            final EtherType type,
            final Buffer payload) {
        super(framerManager, header, Protocol.ETHERNET_II, payload);
        assert parentFrame != null;

        this.parentFrame = parentFrame;
        this.destMacAddress = destMacAddress;
        this.srcMacAddress = srcMacAddress;
        this.type = type;
        this.headers = null;
    }

    public EthernetFrame(final FramerManager framerManager, final PcapGlobalHeader header,
            final Layer1Frame parentFrame, final Buffer headers,
            final Buffer payload) {
        super(framerManager, header, Protocol.ETHERNET_II, payload);
        assert parentFrame != null;
        this.headers = headers;
        this.destMacAddress = null;
        this.srcMacAddress = null;

        this.parentFrame = parentFrame;
        byte b1 = 0x00;
        byte b2 = 0x00;
        try {
            b1 = this.headers.getByte(12);
            b2 = this.headers.getByte(13);
        } catch (final IndexOutOfBoundsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        this.type = EthernetFramer.getEtherTypeSafe(b1, b2);
        if (this.type == null) {
            throw new RuntimeException("what???");
        }

        // this.destMacAddress = this.destMacAddress;
        // this.srcMacAddress = this.srcMacAddress;
        // this.type = this.type;
    }

    /**
     * Get the destination mac address as a raw byte buffer
     * 
     * @return
     */
    public Buffer getRawDestinationMacAddress() {
        if (this.destMacAddress != null) {
            return this.destMacAddress;
        }

        return this.headers.slice(0, 6);
    }

    /**
     * Get the source mac address as a raw byte buffer
     * 
     * @return
     */
    public Buffer getRawSourceMacAddress() {
        if (this.srcMacAddress != null) {
            return this.srcMacAddress;
        }
        return this.headers.slice(6, 12);
    }

    /**
     * Get the source mac address as a human friendly string
     * 
     * @return
     */
    public String getSourceMacAddress() throws IOException {
        /*
         * if (this.srcMacAddress.readableBytes() != 6) { // probably want to
         * throw some parse/frame exception // or something throw new
         * IllegalArgumentException
         * ("Not enough bytes in the source mac address"); }
         */
        return toHexString(getRawSourceMacAddress());
    }

    /**
     * Get the destination mac address as a human friendly string
     * 
     * @return
     */
    public String getDestinationMacAddress() throws IOException {
        /*
         * if (this.destMacAddress.readableBytes() != 6) { // probably want to
         * throw some parse/frame exception // or something throw new
         * IllegalArgumentException
         * ("Not enough bytes in the source mac address"); }
         */
        return toHexString(getRawDestinationMacAddress());
    }

    /**
     * Convert the buffer into a hex string
     * 
     * TODO: move somewhere else...
     * 
     * @param buffer
     * @return
     * @throws IOException
     */
    public static String toHexString(final Buffer buffer) throws IOException {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < buffer.capacity(); ++i) {
            final byte b = buffer.getByte(i);
            sb.append(String.format("%02X", b));
            if (i < buffer.capacity() - 1) {
                sb.append(":");
            }
        }
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MACPacket parse() throws PacketParseException {
        final Packet parentPacket = this.parentFrame.parse();

        // option 1, pass in the raw headers
        return new MACPacketImpl(parentPacket, this.headers);

        // option 2, parse out the mac addresses
        // This is AMAZING! Doing the below is 110%
        // slower compared to the above! Parsing a 100mb pcap
        // tool 6.44 seconds with option 1 and 13.99 seconds with
        // option 2! Gee, that is quite amazing. Another proof that
        // keeping as much as possible as pure buffers make a big
        // difference.
        /*
         * try { final String source = getSourceMacAddress(); final String dest
         * = getDestinationMacAddress(); return new MACPacketImpl(parentPacket,
         * source, dest); } catch (final IOException e) { throw new
         * RuntimeException("TODO: need to parse exception or something", e); }
         */
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        try {
            sb.append("Destination Mac Address: ").append(getDestinationMacAddress());
            sb.append("Source Mac Address: ").append(getSourceMacAddress());
            sb.append("EtherType: ").append(this.type);
        } catch (final IOException e) {
            e.printStackTrace();
        }

        return sb.toString();
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
    protected Frame framePayload(final FramerManager framerManager, final Buffer buffer) throws IOException {
        switch (this.type) {
        case IPv4:
            final Framer framer = framerManager.getFramer(Protocol.IPv4);
            return framer.frame(this, buffer);
        case IPv6:
            throw new RuntimeException("Cant do ipv6 right now");
        default:
            throw new RuntimeException("Uknown ether type");
        }
    }

    public static enum EtherType {
        IPv4((byte) 0x08, (byte) 0x00), IPv6((byte) 0x86, (byte) 0xdd);

        private final byte b1;
        private final byte b2;

        private EtherType(final byte b1, final byte b2) {
            this.b1 = b1;
            this.b2 = b2;
        }

        public void write(final OutputStream out) throws IOException {
            out.write(this.b1);
            out.write(this.b2);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(final OutputStream out) throws IOException {
        this.parentFrame.write(out);
        // out.write(this.destMacAddress.getArray());
        // out.write(this.srcMacAddress.getArray());
        // this.type.write(out);
    }

    @Override
    public long getArrivalTime() {
        return this.parentFrame.getArrivalTime();
    }

}
