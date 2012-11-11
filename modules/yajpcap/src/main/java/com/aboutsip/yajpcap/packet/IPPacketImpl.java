/**
 * 
 */
package com.aboutsip.yajpcap.packet;

import com.aboutsip.buffer.Buffer;

/**
 * @author jonas@jonasborjesson.com
 */
public final class IPPacketImpl implements IPPacket {

    private final MACPacket parent;

    private final Buffer headers;

    private final int options;

    // private final String sourceIp;
    // private final String destinationIp;

    /**
     * 
     */
    public IPPacketImpl(final MACPacket parent, final Buffer headers, final int options) {
        assert parent != null;
        assert headers != null;
        this.parent = parent;
        this.headers = headers;
        this.options = options;
        // assert (sourceIp != null) && !sourceIp.isEmpty();
        // assert (destinationIp != null) && !destinationIp.isEmpty();

        // this.sourceIp = sourceIp;
        // this.destinationIp = this.destinationIp;
    }

    /**
     * Get the raw source ip.
     * 
     * Note, these are the raw bits and should be treated as such. If you really
     * want to print it, then you should treat it as unsigned
     * 
     * @return
     */
    public int getRawSourceIp() {
        return this.headers.getInt(12);
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public String getSourceIP() {
        final short a = this.headers.getUnsignedByte(12);
        final short b = this.headers.getUnsignedByte(13);
        final short c = this.headers.getUnsignedByte(14);
        final short d = this.headers.getUnsignedByte(15);
        return a + "." + b + "." + c + "." + d;
    }

    /**
     * Get the raw destination ip.
     * 
     * Note, these are the raw bits and should be treated as such. If you really
     * want to print it, then you should treat it as unsigned
     * 
     * @return
     */
    public int getRawDestinationIp() {
        return this.headers.getInt(16);
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public String getDestinationIP() {
        final short a = this.headers.getUnsignedByte(16);
        final short b = this.headers.getUnsignedByte(17);
        final short c = this.headers.getUnsignedByte(18);
        final short d = this.headers.getUnsignedByte(19);
        return a + "." + b + "." + c + "." + d;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void verify() {
        // nothing to do for ip packets
    }

    @Override
    public long getArrivalTime() {
        return this.parent.getArrivalTime();
    }

    @Override
    public String getSourceMacAddress() {
        return this.parent.getSourceMacAddress();
    }

    @Override
    public String getDestinationMacAddress() {
        return this.parent.getDestinationMacAddress();
    }

}
