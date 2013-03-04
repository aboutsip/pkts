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
import com.aboutsip.yajpcap.packet.PacketParseException;
import com.aboutsip.yajpcap.packet.TransportPacket;
import com.aboutsip.yajpcap.packet.TransportPacketImpl;
import com.aboutsip.yajpcap.protocol.Protocol;
import com.aboutsip.yajpcap.protocol.Protocol.Layer;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public final class UDPFrame extends AbstractFrame implements Layer4Frame {

    private final Layer3Frame parentFrame;

    private final Buffer headers;

    /**
     * 
     */
    public UDPFrame(final FramerManager framerManager, final Layer3Frame parent, final Buffer headers,
            final Buffer payload) {
        super(framerManager, Protocol.UDP, payload);
        assert parent != null;
        assert headers != null;
        assert payload != null;
        this.parentFrame = parent;
        this.headers = headers;
    }


    // TODO: move all of this stuff into the packet itself
    public int getSourcePort() {
        return this.headers.getUnsignedShort(0);
    }

    public int getDestinationPort() {
        return this.headers.getUnsignedShort(2);
    }

    public int getLength() {
        return this.headers.getUnsignedShort(4);
    }

    public int getChecksum() {
        return this.headers.getUnsignedShort(6);
    }

    @Override
    protected Frame framePayload(final FramerManager framerManager, final Buffer payload) throws IOException {
        if (payload == null) {
            return null;
        }

        final Framer framer = framerManager.getFramer(Layer.LAYER_7, payload);
        if (framer != null) {
            try {
                return framer.frame(this, payload);
            } catch (final RuntimeException e) {
                e.printStackTrace();
                return null;
            }
        }

        // unknown payload
        return null;
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
    public TransportPacket parse() throws PacketParseException {
        // TODO: perhaps do a UDPPacket
        final IPPacket packet = this.parentFrame.parse();
        return new TransportPacketImpl(packet, true, getSourcePort(), getDestinationPort());
    }

    @Override
    public long getArrivalTime() {
        return this.parentFrame.getArrivalTime();
    }

}
