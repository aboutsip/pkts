/**
 * 
 */
package com.aboutsip.yajpcap.packet;

import java.io.IOException;
import java.io.OutputStream;


/**
 * @author jonas@jonasborjesson.com
 */
public final class TransportPacketImpl implements TransportPacket {

    private final IPPacket parent;

    private final boolean isUdp;
    private final int sourcePort;
    private final int destinationPort;

    public TransportPacketImpl(final IPPacket parent, final boolean isUdp, final int sourcePort,
            final int destinationPort) {
        assert parent != null;
        this.parent = parent;
        this.isUdp = isUdp;
        this.sourcePort = sourcePort;
        this.destinationPort = destinationPort;
    }

    /**
     * {@inheritDoc}
     */
    public final boolean isUDP() {
        return this.isUdp;
    }

    /**
     * {@inheritDoc}
     */
    public final boolean isTCP() {
        return !this.isUdp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int getSourcePort() {
        return this.sourcePort;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int getDestinationPort() {
        return this.destinationPort;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void verify() {
        // TODO - verify checksum etc?
    }

    @Override
    public long getArrivalTime() {
        return this.parent.getArrivalTime();
    }

    @Override
    public String getSourceIP() {
        return this.parent.getSourceIP();
    }

    @Override
    public String getDestinationIP() {
        return this.parent.getDestinationIP();
    }

    @Override
    public String getSourceMacAddress() {
        return this.parent.getSourceMacAddress();
    }

    @Override
    public String getDestinationMacAddress() {
        return this.parent.getDestinationMacAddress();
    }

    @Override
    public void write(final OutputStream out) throws IOException {
        throw new RuntimeException("Sorry, not implemented just yet.");
    }

    @Override
    public int getTotalLength() {
        return this.parent.getTotalLength();
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

    @Override
    public void setSourceIP(final int a, final int b, final int c, final int d) {
        this.parent.setSourceIP(a, b, c, d);
    }

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
        this.parent.setDestinationIP(destinationIP);
    }

}
