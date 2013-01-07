/**
 * 
 */
package com.aboutsip.yajpcap.frame;

import java.io.IOException;
import java.io.ObjectOutput;
import java.io.OutputStream;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.yajpcap.framer.Framer;
import com.aboutsip.yajpcap.framer.FramerManager;
import com.aboutsip.yajpcap.packet.IPPacket;
import com.aboutsip.yajpcap.packet.IPPacketImpl;
import com.aboutsip.yajpcap.packet.MACPacket;
import com.aboutsip.yajpcap.packet.PacketParseException;
import com.aboutsip.yajpcap.protocol.Protocol;

/**
 * An IP Frame
 * 
 * @author jonas@jonasborjesson.com
 * 
 */
public final class IPv4Frame extends AbstractFrame implements IPFrame {

    /**
     * The parent frame
     */
    private final Layer2Frame parentFrame;

    /**
     * Internet Header Length (IHL), which is the number of 32-bit words in the
     * header.
     */
    private final int length;

    /**
     * All the headers of this ip packet
     */
    private final Buffer headers;

    /**
     * If the {@code #length} is greater than 5, then this header carries some
     * extra options
     */
    private final int options;

    /**
     * The protocol contained in this ip packet. I.e., the
     */
    private final Protocol protocol;

    /**
     * 
     * @param length the header length
     * @param headers all the ipv4 headers in a buffer.
     * @param options if header length > 5, then we have a set of options as
     *            well
     * @param data the payload of the ip4v frame
     */
    public IPv4Frame(final FramerManager framerManager, final Layer2Frame parent, final int length,
            final Buffer headers, final int options,
            final Buffer payload) throws IOException {
        super(framerManager, Protocol.IPv4, payload);
        assert parent != null;

        this.parentFrame = parent;
        this.length = length;
        this.headers = headers;
        this.options = options;

        // the protocol is in byte 10
        final byte code = headers.getByte(9);
        this.protocol = Protocol.valueOf(code);
    }

    /**
     * Check out http://en.wikipedia.org/wiki/IPv4 for a good explanation of the
     * IPv4 header frame
     * 
     * @param payload the total payload of the previous frame, which contains
     *            the ipv4 headers and its payload
     * @return
     */
    // public static IPv4Frame frame(final Buffer payload) {

    // }

    /**
     * The version of this ip frame, will always be 4
     * 
     * @return
     */
    @Override
    public int getVersion() {
        return 4;
    }

    /**
     * The length of the ipv4 headers
     * 
     * @return
     */
    @Override
    public int getHeaderLength() {
        return this.length;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        // frames are easy to write out, just ask the top level
        // frame to persist all its payload since that will
        // capture everything...
        this.parentFrame.writeExternal(out);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IPPacket parse() throws PacketParseException {
        final MACPacket packet = this.parentFrame.parse();
        return new IPPacketImpl(packet, this.headers, this.options);
    }

    @Override
    protected Frame framePayload(final FramerManager framerManager, final Buffer payload) throws IOException {
        final Framer framer = framerManager.getFramer(this.protocol);
        if (framer != null) {
            return framer.frame(this, payload);
        }

        throw new RuntimeException("Unknown protocol " + this.protocol);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(final OutputStream out) throws IOException {
        this.parentFrame.write(out);
        // out.write(this.headers.getArray());
        // out.write(getPayload().getArray());
    }

    @Override
    public long getArrivalTime() {
        return this.parentFrame.getArrivalTime();
    }

}
