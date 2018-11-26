package io.pkts.packet.diameter.impl;

import io.pkts.buffer.Buffer;
import io.pkts.diameter.DiameterMessage;
import io.pkts.packet.Packet;
import io.pkts.packet.PacketParseException;
import io.pkts.packet.TransportPacket;
import io.pkts.packet.diameter.DiameterPacket;
import io.pkts.packet.impl.AbstractPacket;
import io.pkts.protocol.Protocol;

import java.io.IOException;
import java.io.OutputStream;

public class DiameterPacketImpl extends AbstractPacket implements DiameterPacket {

    /**
     * The actual Diameter message. The {@link DiameterPacket} is merely a thin wrapper
     * around this object in order to make if fit the pcap model whereas the
     * actual {@link DiameterMessage} is a pure Diameter object only.
     */
    private final DiameterMessage msg;

    public DiameterPacketImpl(final TransportPacket parent, final DiameterMessage msg) {
        super(Protocol.DIAMETER, parent, null);
        this.msg = msg;
    }

    @Override
    public long getArrivalTime() {
        return getTransportPacket().getArrivalTime();
    }

    protected TransportPacket getTransportPacket() {
        return (TransportPacket) getParentPacket();
    }

    @Override
    public void write(final OutputStream out, final Buffer payload) throws IOException {

    }

    @Override
    public Packet clone() {
        // now, the only implementaiton of the  diameter message is an immutable one
        // but I guess we shouldn't assume that so let's clone it all.
        final TransportPacket transport = getTransportPacket().clone();
        final DiameterMessage clone = msg.clone();
        return new DiameterPacketImpl(transport, clone);
    }

    @Override
    public Packet getNextPacket() throws IOException, PacketParseException {
        // No next packet for Diameter
        return null;
    }

    @Override
    public String toString() {
        return msg.toString();
    }
}
