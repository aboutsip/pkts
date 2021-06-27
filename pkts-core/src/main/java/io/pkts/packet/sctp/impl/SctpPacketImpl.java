package io.pkts.packet.sctp.impl;

import io.pkts.buffer.Buffer;
import io.pkts.packet.IPPacket;
import io.pkts.packet.TransportPacket;
import io.pkts.packet.impl.TransportPacketImpl;
import io.pkts.packet.sctp.SctpChunk;
import io.pkts.packet.sctp.SctpPacket;
import io.pkts.packet.sctp.SctpParseException;
import io.pkts.protocol.Protocol;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static io.pkts.packet.sip.impl.PreConditions.assertArgument;
import static io.pkts.packet.sip.impl.PreConditions.assertNotEmpty;
import static io.pkts.packet.sip.impl.PreConditions.assertNotNull;

public class SctpPacketImpl extends TransportPacketImpl implements SctpPacket {

    private final List<SctpChunk> chunks;

    public static SctpPacket frame(final IPPacket ipPacket, final Buffer buffer) {
        try {
            assertNotNull(ipPacket, "The IP Packet cannot be null");
            assertNotEmpty(buffer, "The Buffer cannot be null or empty");

            // TODO: really need to move all of this to Snice.io Buffers instead.
            //
            // SCTP Common Header is always 12 bytes so we must have at least that.
            assertArgument(buffer.getReadableBytes() >= 12, "There must be at least 12 bytes to read");
            final Buffer header = buffer.readBytes(12);
            final int startChunks = buffer.getReaderIndex();
            final List<SctpChunk> chunks = new ArrayList<>();
            while (buffer.hasReadableBytes()) {
                chunks.add(SctpChunk.frame(buffer));
            }

            final Buffer chunksBuffer = buffer.slice(startChunks, buffer.getReaderIndex());
            return new SctpPacketImpl(ipPacket, header, chunks, chunksBuffer);

        } catch (final IOException e) {
            throw new SctpParseException(buffer.getReaderIndex(), "Unable to read from buffer. Message (if any) " + e.getMessage());
        }

    }

    private SctpPacketImpl(final IPPacket parent, final Buffer headers, final List<SctpChunk> chunks, final Buffer payload) {
        super(parent, Protocol.SCTP, headers, payload);
        this.chunks = chunks;
    }

    @Override
    public List<SctpChunk> getChunks() {
        return chunks;
    }

    @Override
    public int getHeaderLength() {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public void write(final OutputStream out, final Buffer payload) throws IOException {
        throw new RuntimeException("Not yet implemented");

    }

    @Override
    public TransportPacket clone() {
        throw new RuntimeException("Not yet implemented");
    }
}
