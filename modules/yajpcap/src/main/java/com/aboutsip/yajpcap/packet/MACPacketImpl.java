/**
 * 
 */
package com.aboutsip.yajpcap.packet;

import java.io.IOException;
import java.io.OutputStream;


/**
 * @author jonas@jonasborjesson.com
 */
public final class MACPacketImpl implements MACPacket {

    private final Packet parentPacket;
    private final String sourceMacAddress;
    private final String destinationMacAddress;

    /**
     * 
     */
    public MACPacketImpl(final Packet packet, final String sourceMacAddress, final String destinationMacAddress) {
        assert packet != null;
        assert (sourceMacAddress != null) && !sourceMacAddress.isEmpty();
        assert (destinationMacAddress != null) && !destinationMacAddress.isEmpty();
        this.parentPacket = packet;
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
        // nothing to verify
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Destination Mac Address: ").append(this.destinationMacAddress);
        sb.append(" Source Mac Address: ").append(this.sourceMacAddress);
        return sb.toString();
    }

    @Override
    public long getArrivalTime() {
        return this.parentPacket.getArrivalTime();
    }

    @Override
    public void write(final OutputStream out) throws IOException {
        throw new RuntimeException("Sorry, not implemented just yet.");
    }

}
