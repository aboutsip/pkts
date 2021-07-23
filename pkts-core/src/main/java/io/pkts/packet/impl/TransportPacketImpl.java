/**
 * 
 */
package io.pkts.packet.impl;

import io.pkts.buffer.Buffer;
import io.pkts.framer.RTPFramer;
import io.pkts.framer.SIPFramer;
import io.pkts.packet.IPPacket;
import io.pkts.packet.Packet;
import io.pkts.packet.TransportPacket;
import io.pkts.packet.rtp.RtpPacket;
import io.pkts.protocol.Protocol;

import java.io.IOException;

/**
 * @author jonas@jonasborjesson.com
 */
public abstract class TransportPacketImpl extends AbstractPacket implements TransportPacket {

    private static final SIPFramer sipFramer = new SIPFramer();
    private static final RTPFramer rtpFramer = new RTPFramer();

    private final IPPacket parent;

    private final Buffer headers;

    protected TransportPacketImpl(final IPPacket parent, final Protocol protocol, final Buffer headers,
                                  final Buffer payload) {
        super(protocol, parent, payload);
        assert parent != null;
        this.parent = parent;
        this.headers = headers;
    }

    @Override
    public boolean isUDP() {
        return false;
    }

    @Override
    public boolean isTCP() {
        return false;
    }

    @Override
    public boolean isSCTP() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int getSourcePort() {
        return this.headers.getUnsignedShort(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setSourcePort(final int port) {
        this.headers.setUnsignedShort(0, port);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int getDestinationPort() {
        return this.headers.getUnsignedShort(2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setDestinationPort(final int port) {
        this.headers.setUnsignedShort(2, port);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void verify() {
        // TODO - verify checksum etc?
    }

    @Override
    public final long getArrivalTime() {
        return this.parent.getArrivalTime();
    }


    @Override
    public abstract TransportPacket clone();

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.Packet#getNextPacket()
     */
    @Override
    public Packet getNextPacket() throws IOException {
        final Buffer payload = getPayload();
        if (payload == null || payload.isEmpty()) {
            return null;
        }

        if (sipFramer.accept(payload)) {
            return sipFramer.frame(this, payload);
        } else if (rtpFramer.accept(payload)) {
            // RTP is tricky to parse so if we return
            // null then it wasn't an RTP packet afterall
            // so fall through...
            final RtpPacket rtp = frameRtp(payload);
            if (rtp != null) {
                return rtp;
            }
        }

        return new UnknownApplicationPacketImpl(this, payload);
    }

    private RtpPacket frameRtp(final Buffer payload) throws IOException {
        try {
            return rtpFramer.frame(this, payload);
        } catch (final IndexOutOfBoundsException e) {
            return null;
        }
    }

    @Override
    public IPPacket getParentPacket() {
        return (IPPacket) super.getParentPacket();
    }
}
