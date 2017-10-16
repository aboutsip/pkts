/**
 * 
 */
package io.pkts.packet.rtp.impl;

import io.pkts.buffer.Buffer;
import io.pkts.packet.IPPacket;
import io.pkts.packet.Packet;
import io.pkts.packet.TransportPacket;
import io.pkts.packet.impl.AbstractPacket;
import io.pkts.packet.rtp.RtpPacket;
import io.pkts.protocol.Protocol;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author jonas@jonasborjesson.com
 */
public final class RtpPacketImpl extends AbstractPacket implements RtpPacket {

    private final TransportPacket parent;

    /**
     * All the RTP headers as one buffer.
     */
    private final Buffer headers;

    /**
     * The raw payload of the RTP packet. Is most likely audio or video.
     */
    private final Buffer payload;

    /**
     * 
     */
    public RtpPacketImpl(final TransportPacket parent, final Buffer headers, final Buffer payload) {
        super(Protocol.RTP, parent, payload);
        this.parent = parent;
        this.headers = headers;
        this.payload = payload;
    }

    @Override
    public int getVersion() {
        try {
            return (this.headers.getByte(0) & 0xC0) >> 6;
        } catch (final IndexOutOfBoundsException e) {
            throw new RuntimeException("Unable to parse out the RTP version, not enough data", e);
        } catch (final IOException e) {
            throw new RuntimeException("Unable to parse out the RTP version, IOException when trying.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasPadding() throws IOException {
        return (this.headers.getByte(0) & 0x20) == 0x020;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasExtensions() throws IOException {
        return (this.headers.getByte(0) & 0x10) == 0x010;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasMarker() throws IOException {
        return (this.headers.getByte(1) & 0xff & 0x80) == 0x80;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPayloadType() throws IOException {
        return this.headers.getByte(1) & 0xff & 0x7f;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getSeqNumber() throws IOException {
        // TODO: this is not quite right...
        return this.headers.getShort(2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getTimestamp() throws IOException {
        return (long) (this.headers.getByte(4) & 0xff) << 24 | (long) (this.headers.getByte(5) & 0xff) << 16
                | (long) (this.headers.getByte(6) & 0xff) << 8 | this.headers.getByte(7) & 0xff;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getSyncronizationSource() throws IOException {
        return (long) (this.headers.getByte(8) & 0xff) << 24 | (long) (this.headers.getByte(9) & 0xff) << 16
                | (long) (this.headers.getByte(10) & 0xff) << 8 | this.headers.getByte(11) & 0xff;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getContributingSource() throws IOException {
        return this.headers.getByte(0) & 0x0F;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void verify() {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getArrivalTime() {
        return this.parent.getArrivalTime();
    }

    @Override
    public String toString() {
        try {
            final StringBuilder sb = new StringBuilder();
            final TransportPacket transportPacket = getParentPacket();
            final IPPacket ipPacket = transportPacket.getParentPacket();
            sb.append("Seq=").append(getSeqNumber())
              .append(" type=").append(getPayloadType())
              .append(" src=").append(ipPacket.getSourceIP()).append(":").append(transportPacket.getSourcePort())
              .append(" dst=").append(ipPacket.getDestinationIP()).append(":").append(transportPacket.getDestinationPort());
            return sb.toString();
        } catch (final IOException e) {
            return super.toString();
        }

    }

    @Override
    public byte[] dumpPacket() {
        final int headerLength = this.headers.capacity();
        final int payloadLength = this.payload.capacity();

        final byte[] dump = new byte[headerLength + payloadLength];
        System.arraycopy(this.headers.getArray(), 0, dump, 0, headerLength);
        System.arraycopy(this.payload.getArray(), 0, dump, headerLength, payloadLength);
        return dump;
    }

    @Override
    public void write(final OutputStream out, final Buffer payload) throws IOException {
        throw new RuntimeException("Sorry, not implemented just yet.");
    }

    @Override
    public RtpPacket clone() {
        throw new RuntimeException("Sorry, not implemented just yet");
    }

    @Override
    public Packet getNextPacket() throws IOException {
        // no more packets for RTP
        return null;
    }

    @Override
    public TransportPacket getParentPacket() {
        return (TransportPacket) super.getParentPacket();
    }
}
