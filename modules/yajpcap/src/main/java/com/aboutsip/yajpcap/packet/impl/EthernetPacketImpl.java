/**
 * 
 */
package com.aboutsip.yajpcap.packet.impl;

import com.aboutsip.yajpcap.packet.EthernetPacket;

/**
 * @author jonas@jonasborjesson.com
 */
public final class EthernetPacketImpl implements EthernetPacket {

    private final String sourceMacAddress;
    private final String destinationMacAddress;

    /**
     * 
     */
    public EthernetPacketImpl(final String sourceMacAddress, final String destinationMacAddress) {
        assert (sourceMacAddress != null) && !sourceMacAddress.isEmpty();
        assert (destinationMacAddress != null) && !destinationMacAddress.isEmpty();
        this.sourceMacAddress = sourceMacAddress;
        this.destinationMacAddress = destinationMacAddress;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getSourceMacAddress() {
        return this.sourceMacAddress;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getDestinationMacAddress() {
        return this.destinationMacAddress;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void verify() {
        // nothing to verify for an ethernet packet
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Destination Mac Address: ").append(this.destinationMacAddress);
        sb.append(" Source Mac Address: ").append(this.sourceMacAddress);
        return sb.toString();
    }

}
