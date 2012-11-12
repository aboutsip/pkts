/**
 * 
 */
package com.aboutsip.yajpcap.frame;

import java.io.IOException;
import java.io.ObjectOutput;
import java.io.OutputStream;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.yajpcap.framer.FramerManager;
import com.aboutsip.yajpcap.packet.PacketParseException;
import com.aboutsip.yajpcap.packet.TransportPacket;
import com.aboutsip.yajpcap.packet.impl.ApplicationPacket;
import com.aboutsip.yajpcap.packet.rtp.impl.RtpPacketImpl;
import com.aboutsip.yajpcap.protocol.Protocol;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public class RtpFrame extends AbstractFrame implements Layer7Frame {

    private final Layer4Frame parentFrame;

    private final Buffer headers;

    private final Buffer payload;

    public RtpFrame(final FramerManager framerManager, final Layer4Frame parentFrame, final Buffer headers,
            final Buffer payload) {
        super(framerManager, Protocol.RTP, payload);
        assert parentFrame != null;
        this.parentFrame = parentFrame;
        this.headers = headers;
        this.payload = payload;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApplicationPacket parse() throws PacketParseException {
        final TransportPacket pkt = this.parentFrame.parse();
        return new RtpPacketImpl(pkt, this.headers, this.payload);
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
    protected Frame framePayload(final FramerManager framerManager, final Buffer payload) throws IOException {
        // Not sure how to frame the payload of an RTP packet, which
        // will typically be audio
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(final OutputStream out) throws IOException {
        this.parentFrame.write(out);
    }

}
