/**
 * 
 */
package io.pkts.packet.impl;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.TransportPacket;
import io.pkts.protocol.Protocol;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author jonas@jonasborjesson.com
 */
public abstract class AbstractApplicationPacket extends AbstractPacket implements ApplicationPacket {

    private final TransportPacket parent;

    private final Buffer payload;

    /**
     * @param p
     * @param parent
     * @param payload
     */
    public AbstractApplicationPacket(final Protocol p, final TransportPacket parent, final Buffer payload) {
        super(p, parent, payload);
        this.parent = parent;
        this.payload = payload;
    }

    protected TransportPacket getParent() {
        return this.parent;
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.TransportPacket#getSourcePort()
     */
    @Override
    public final int getSourcePort() {
        return this.parent.getSourcePort();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.TransportPacket#getDestinationPort()
     */
    @Override
    public final int getDestinationPort() {
        return this.parent.getDestinationPort();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.IPPacket#getSourceIP()
     */
    @Override
    public final String getSourceIP() {
        return this.parent.getSourceIP();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.IPPacket#setSourceIP(int, int, int, int)
     */
    @Override
    public final void setSourceIP(final int a, final int b, final int c, final int d) {
        this.parent.setSourceIP(a, b, c, d);
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.IPPacket#setSourceIP(byte, byte, byte, byte)
     */
    @Override
    public final void setSourceIP(final byte a, final byte b, final byte c, final byte d) {
        this.parent.setSourceIP(a, b, c, d);
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.IPPacket#setSourceIP(java.lang.String)
     */
    @Override
    public final void setSourceIP(final String sourceIp) {
        this.parent.setSourceIP(sourceIp);
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.IPPacket#getDestinationIP()
     */
    @Override
    public final String getDestinationIP() {
        return this.parent.getDestinationIP();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.IPPacket#setDestinationIP(int, int, int, int)
     */
    @Override
    public final void setDestinationIP(final int a, final int b, final int c, final int d) {
        this.parent.setDestinationIP(a, b, c, d);
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.IPPacket#setDestinationIP(byte, byte, byte, byte)
     */
    @Override
    public final void setDestinationIP(final byte a, final byte b, final byte c, final byte d) {
        this.parent.setDestinationIP(a, b, c, d);
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.IPPacket#setDestinationIP(java.lang.String)
     */
    @Override
    public final void setDestinationIP(final String destinationIP) {
        this.parent.setDestinationIP(destinationIP);
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.IPPacket#getTotalLength()
     */
    @Override
    public final long getTotalLength() {
        return this.parent.getTotalLength();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.IPPacket#getIpChecksum()
     */
    @Override
    public final int getIpChecksum() {
        return this.parent.getIpChecksum();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.IPPacket#reCalculateChecksum()
     */
    @Override
    public final void reCalculateChecksum() {
        this.parent.reCalculateChecksum();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.IPPacket#verifyIpChecksum()
     */
    @Override
    public final boolean verifyIpChecksum() {
        return this.parent.verifyIpChecksum();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.MACPacket#getSourceMacAddress()
     */
    @Override
    public final String getSourceMacAddress() {
        return this.parent.getSourceMacAddress();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.MACPacket#setSourceMacAddress(java.lang.String)
     */
    @Override
    public final void setSourceMacAddress(final String macAddress) throws IllegalArgumentException {
        this.parent.setSourceMacAddress(macAddress);
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.MACPacket#getDestinationMacAddress()
     */
    @Override
    public final String getDestinationMacAddress() {
        return this.parent.getDestinationMacAddress();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.MACPacket#setDestinationMacAddress(java.lang.String)
     */
    @Override
    public final void setDestinationMacAddress(final String macAddress) throws IllegalArgumentException {
        this.parent.setDestinationMacAddress(macAddress);
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.Packet#getArrivalTime()
     */
    @Override
    public final long getArrivalTime() {
        return this.parent.getArrivalTime();
    }

    @Override
    public final void setSourcePort(final int port) {
        this.parent.setSourcePort(port);
    }

    @Override
    public final void setDestinationPort(final int port) {
        this.parent.setDestinationPort(port);
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.IPPacket#getTotalIPLength()
     */
    @Override
    public final int getTotalIPLength() {
        return this.parent.getTotalIPLength();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.IPPacket#getVersion()
     */
    @Override
    public final int getVersion() {
        return this.parent.getVersion();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.IPPacket#getHeaderLength()
     */
    @Override
    public final int getHeaderLength() {
        return this.parent.getHeaderLength();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.IPPacket#getIdentification()
     */
    @Override
    public final int getIdentification() {
        return this.parent.getIdentification();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.IPPacket#isFragmented()
     */
    @Override
    public final boolean isFragmented() {
        return this.parent.isFragmented();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.IPPacket#isReservedFlagSet()
     */
    @Override
    public final boolean isReservedFlagSet() {
        return this.parent.isReservedFlagSet();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.IPPacket#isDontFragmentSet()
     */
    @Override
    public final boolean isDontFragmentSet() {
        return this.parent.isDontFragmentSet();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.IPPacket#isMoreFragmentsSet()
     */
    @Override
    public final boolean isMoreFragmentsSet() {
        return this.parent.isMoreFragmentsSet();
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.pkts.packet.IPPacket#getFragmentOffset()
     */
    @Override
    public final short getFragmentOffset() {
        return this.parent.getFragmentOffset();
    }

    @Override
    public void write(final OutputStream out, final Buffer payload) throws IOException {
        final Buffer buffer = this.payload != null ? Buffers.wrap(this.payload, payload) : payload;
        this.parent.write(out, buffer);
    }

    @Override
    public final long getCapturedLength() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public abstract ApplicationPacket clone();

}
