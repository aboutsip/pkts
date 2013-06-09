/**
 * 
 */
package io.pkts.frame;

import io.pkts.buffer.Buffer;
import io.pkts.framer.FramerManager;
import io.pkts.packet.PacketParseException;
import io.pkts.packet.TransportPacket;
import io.pkts.packet.rtp.RtpPacket;
import io.pkts.packet.rtp.impl.RtpPacketImpl;
import io.pkts.protocol.Protocol;

import java.io.IOException;
import java.io.ObjectOutput;
import java.io.OutputStream;


/**
 * @author jonas@jonasborjesson.com
 * 
 */
public class RtpFrame extends AbstractFrame implements Layer7Frame {

    private final Layer4Frame parentFrame;

    private final Buffer headers;

    private final Buffer payload;

    public RtpFrame(final FramerManager framerManager, final PcapGlobalHeader header, final Layer4Frame parentFrame,
            final Buffer headers,
            final Buffer payload) {
        super(framerManager, header, Protocol.RTP, payload);
        assert parentFrame != null;
        this.parentFrame = parentFrame;
        this.headers = headers;
        this.payload = payload;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RtpPacket parse() throws PacketParseException {
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

    @Override
    public long getArrivalTime() {
        return this.parentFrame.getArrivalTime();
    }

}
