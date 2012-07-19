/**
 * 
 */
package com.aboutsip.yajpcap.packet.impl;

import com.aboutsip.yajpcap.packet.IPPacket;

/**
 * @author jonas@jonasborjesson.com
 */
public final class IPPacketImpl implements IPPacket {

    private final String sourceIp;
    private final String destinationIp;

    /**
     * 
     */
    public IPPacketImpl(final String sourceIp, final String destinationIp) {
        assert (sourceIp != null) && !sourceIp.isEmpty();
        assert (destinationIp != null) && !destinationIp.isEmpty();
        this.sourceIp = sourceIp;
        this.destinationIp = destinationIp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void verify() {
        // nothing to do for ip packets
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getSourceIP() {
        return this.sourceIp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getDestinationIP() {
        return this.destinationIp;
    }

}
