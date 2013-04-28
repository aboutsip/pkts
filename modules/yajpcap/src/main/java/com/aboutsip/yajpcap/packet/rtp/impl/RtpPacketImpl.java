/**
 * 
 */
package com.aboutsip.yajpcap.packet.rtp.impl;

import java.io.IOException;
import java.io.OutputStream;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.yajpcap.packet.TransportPacket;
import com.aboutsip.yajpcap.packet.rtp.RtpPacket;

/**
 * @author jonas@jonasborjesson.com
 */
public final class RtpPacketImpl implements RtpPacket {

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
        this.parent = parent;
        this.headers = headers;
        this.payload = payload;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getVersion() throws IOException {
        return (this.headers.getByte(0) & 0xC0) >> 6;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public int getSourcePort() {
        return this.parent.getSourcePort();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getDestinationPort() {
        return this.parent.getDestinationPort();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSourceIP() {
        return this.parent.getSourceIP();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDestinationIP() {
        return this.parent.getDestinationIP();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSourceMacAddress() {
        return this.parent.getSourceMacAddress();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDestinationMacAddress() {
        return this.parent.getDestinationMacAddress();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSourceMacAddress(final String macAddress) {
        this.parent.setSourceMacAddress(macAddress);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDestinationMacAddress(final String macAddress) {
        this.parent.setDestinationMacAddress(macAddress);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSourceIP(final int a, final int b, final int c, final int d) {
        this.parent.setSourceIP(a, b, c, d);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDestinationIP(final int a, final int b, final int c, final int d) {
        this.parent.setDestinationIP(a, b, c, d);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSourceIP(final String sourceIp) {
        this.parent.setSourceIP(sourceIp);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDestinationIP(final String destinationIP) {
        this.parent.setSourceIP(destinationIP);
    }

    @Override
    public String toString() {
        try {
            final StringBuilder sb = new StringBuilder();
            sb.append("Seq=").append(getSeqNumber());
            sb.append(" type=").append(getPayloadType());
            sb.append(" src=").append(getSourceIP()).append(":").append(getSourcePort());
            sb.append(" dst=").append(getDestinationIP()).append(":").append(getDestinationPort());
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
    public void write(final OutputStream out) throws IOException {
        throw new RuntimeException("Sorry, not implemented just yet.");
    }

    @Override
    public int getTotalLength() {
        return this.parent.getTotalLength();
    }

    @Override
    public int getIpChecksum() {
        return this.parent.getIpChecksum();
    }

    @Override
    public void setSourceIP(final byte a, final byte b, final byte c, final byte d) {
        this.parent.setSourceIP(a, b, c, d);
    }

    @Override
    public void setDestinationIP(final byte a, final byte b, final byte c, final byte d) {
        this.parent.setDestinationIP(a, b, c, d);
    }

    @Override
    public void reCalculateChecksum() {
        this.parent.reCalculateChecksum();
    }

    @Override
    public boolean verifyIpChecksum() {
        return this.parent.verifyIpChecksum();
    }

    @Override
    public RtpPacket clone() {
        throw new RuntimeException("Sorry, not implemented just yet");
    }

}
