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

}
