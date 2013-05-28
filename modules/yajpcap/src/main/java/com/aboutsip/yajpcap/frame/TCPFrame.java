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
public final class TCPFrame extends AbstractFrame implements Layer4Frame {

    private final Layer3Frame parentFrame;

    /**
     * The raw tcp headers
     */
    private final Buffer headers;

    /**
     * Options, which may be null
     */
    private final Buffer options;

    /**
     * @param framerManager
     * @param p
     * @param payload
     */
    public TCPFrame(final FramerManager framerManager, final PcapGlobalHeader header, final Layer3Frame parent,
            final Buffer headers,
            final Buffer options, final Buffer payload) {
        super(framerManager, header, Protocol.TCP, payload);
        assert parent != null;
        assert headers != null;
        this.parentFrame = parent;
        this.headers = headers;
        this.options = options;
    }

    /**
     * Get the header length in bytes
     * 
     * @return
     */
    public int getHeaderLength() {
        // 20 because the minimum TCP header length is 20 - ALWAYS
        return 20 + (this.options != null ? this.options.capacity() : 0);
    }

    public boolean isFIN() {
        try {
            final byte b = this.headers.getByte(13);
            return (b & 0x01) == 0x01;
        } catch (final Exception e) {
            // ignore, Shouldn't happen since we have already
            // framed all the bytes
            return false;
        }
    }

    public boolean isSYN() {
        try {
            final byte b = this.headers.getByte(13);
            return (b & 0x02) == 0x02;
        } catch (final Exception e) {
            return false;
        }
    }

    public boolean isRST() {
        try {
            final byte b = this.headers.getByte(13);
            return (b & 0x04) == 0x04;
        } catch (final Exception e) {
            return false;
        }
    }

    /**
     * Check whether the psh (push) flag is turned on
     * 
     * @return
     */
    public boolean isPSH() {
        try {
            final byte b = this.headers.getByte(13);
            return (b & 0x08) == 0x08;
        } catch (final Exception e) {
            return false;
        }
    }

    public boolean isACK() {
        try {
            final byte b = this.headers.getByte(13);
            return (b & 0x10) == 0x10;
        } catch (final Exception e) {
            return false;
        }
    }

    public boolean isURG() {
        try {
            final byte b = this.headers.getByte(13);
            return (b & 0x20) == 0x20;
        } catch (final Exception e) {
            return false;
        }
    }

    public boolean isECE() {
        try {
            final byte b = this.headers.getByte(13);
            return (b & 0x40) == 0x40;
        } catch (final Exception e) {
            return false;
        }
    }

    public boolean isCWR() {
        try {
            final byte b = this.headers.getByte(13);
            return (b & 0x80) == 0x80;
        } catch (final Exception e) {
            return false;
        }
    }

    public int getSourcePort() {
        return this.headers.getUnsignedShort(0);
    }

    public int getDestinationPort() {
        return this.headers.getUnsignedShort(2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TransportPacket parse() throws PacketParseException {
        final IPPacket packet = this.parentFrame.parse();
        return new TransportPacketImpl(packet, false, getSourcePort(), getDestinationPort());
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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Frame framePayload(final FramerManager framerManager, final Buffer payload) throws IOException {
        if (payload == null) {
            return null;
        }

        final Framer framer = framerManager.getFramer(Layer.LAYER_7, payload);
        if (framer != null) {
            return framer.frame(this, payload);
        }

        // unknown payload
        return null;
    }

    @Override
    public long getArrivalTime() {
        return this.parentFrame.getArrivalTime();
    }

}
