/**
 * 
 */
package com.aboutsip.yajpcap.packet.rtp.impl;

import java.io.IOException;

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
        return (this.headers.getByte(1) & 0xff & 0x7f);
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
        return ((long) (this.headers.getByte(4) & 0xff) << 24) | ((long) (this.headers.getByte(5) & 0xff) << 16)
                | ((long) (this.headers.getByte(6) & 0xff) << 8) | ((this.headers.getByte(7) & 0xff));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getSyncronizationSource() throws IOException {
        return ((long) (this.headers.getByte(8) & 0xff) << 24) | ((long) (this.headers.getByte(9) & 0xff) << 16)
                | ((long) (this.headers.getByte(10) & 0xff) << 8) | ((this.headers.getByte(11) & 0xff));
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

}
