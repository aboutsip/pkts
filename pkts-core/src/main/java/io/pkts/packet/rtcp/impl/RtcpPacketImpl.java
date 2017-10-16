/**
 * 
 */
package io.pkts.packet.rtcp.impl;

import io.pkts.buffer.Buffer;
import io.pkts.packet.Packet;
import io.pkts.packet.TransportPacket;
import io.pkts.packet.impl.AbstractPacket;
import io.pkts.packet.rtcp.RtcpPacket;
import io.pkts.protocol.Protocol;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author jonas@jonasborjesson.com
 */
public class RtcpPacketImpl extends AbstractPacket implements RtcpPacket {

    private final TransportPacket parent;

    public RtcpPacketImpl(final TransportPacket parent, final Buffer headers, final Buffer payload) {
        super(Protocol.RTCP, parent, payload);
        this.parent = parent;
        // this.headers = headers;
        // this.payload = payload;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(final OutputStream out, final Buffer payload) throws IOException {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Packet getNextPacket() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RtcpPacket clone() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getArrivalTime() {
        return this.parent.getArrivalTime();
    }

}
