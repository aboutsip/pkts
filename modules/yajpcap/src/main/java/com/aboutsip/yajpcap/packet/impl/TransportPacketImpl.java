/**
 * 
 */
package com.aboutsip.yajpcap.packet.impl;

import com.aboutsip.yajpcap.packet.TransportPacket;

/**
 * @author jonas@jonasborjesson.com
 */
public final class TransportPacketImpl implements TransportPacket {

    private final boolean isUdp;
    private final int sourcePort;
    private final int destinationPort;

    public TransportPacketImpl(final boolean isUdp, final int sourcePort, final int destinationPort) {
        this.isUdp = isUdp;
        this.sourcePort = sourcePort;
        this.destinationPort = destinationPort;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isUDP() {
        return this.isUdp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
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

}
