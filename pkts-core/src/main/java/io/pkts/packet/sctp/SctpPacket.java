package io.pkts.packet.sctp;

import io.pkts.buffer.Buffer;
import io.pkts.packet.IPPacket;
import io.pkts.packet.TransportPacket;
import io.pkts.packet.sctp.impl.SctpPacketImpl;

import java.util.List;

public interface SctpPacket extends TransportPacket {

    static SctpPacket frame(final IPPacket ipPacket, final Buffer buffer) {
        return SctpPacketImpl.frame(ipPacket, buffer);
    }

    List<SctpChunk> getChunks();
}
