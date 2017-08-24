/**
 * 
 */
package io.pkts.framer;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.frame.PcapGlobalHeader;
import io.pkts.frame.PcapRecordHeader;
import io.pkts.packet.PCapPacket;
import io.pkts.packet.impl.PCapPacketImpl;
import io.pkts.protocol.Protocol;

import java.io.IOException;
import java.nio.ByteOrder;

/**
 * @author jonas@jonasborjesson.com
 */
public final class PcapFramer implements Framer<PCapPacket> {
    // Nobody uses packets bigger than 9k in practice, so add 1k of overhead and we have 10k.
    private static final int MAX_FRAME_LENGTH = 10000;

    private final PcapGlobalHeader globalHeader;
    private final FramerManager framerManager;
    private final ByteOrder byteOrder;

    /**
     * 
     */
    public PcapFramer(final PcapGlobalHeader globalHeader, final FramerManager framerManager) {
        assert globalHeader != null;
        assert framerManager != null;

        this.globalHeader = globalHeader;
        this.byteOrder = this.globalHeader.getByteOrder();
        this.framerManager = framerManager;
    }

    @Override
    public Protocol getProtocol() {
        return Protocol.PCAP;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PCapPacket frame(final PCapPacket parent, final Buffer buffer) throws IOException, FramingException {

        // note that for the PcapPacket the parent will always be null
        // so we are simply ignoring it.
        Buffer record = null;
        try {
            record = buffer.readBytes(16);
        } catch (final IndexOutOfBoundsException e) {
            // we def want to do something nicer than exit
            // on an exception like this. For now, good enough
            return null;
        }

        final PcapRecordHeader header = new PcapRecordHeader(this.byteOrder, record);
        if (header.getTimeStampMicroSeconds() < 0) {
            throw new FramingException("pcap timestamp is negative", getProtocol());
        }
        if (header.getTimeStampMicroSeconds() > 1000000) {
            throw new FramingException("pcap timestamp is invalid", getProtocol());
        }
        final int length = (int) header.getCapturedLength();
        final int total = (int) header.getTotalLength();
        if (length > total) {
            throw new FramingException(String.format("captured length %d is greater than total length %d", length, total), getProtocol());
        }
        if (total == 0) {
            throw new FramingException("empty packet", getProtocol());
        }
        if (length > MAX_FRAME_LENGTH) {
            throw new FramingException(String.format("Invalid frame length %d", length), getProtocol());
        }
        try {
            int len = Math.min(length, total);
            Buffer payload;
            if (len == 0) {
                payload = Buffers.EMPTY_BUFFER;
            } else {
                payload = buffer.readBytes(len);
            }
            return new PCapPacketImpl(globalHeader, header, payload);
        } catch (IndexOutOfBoundsException e) {
            throw new FramingException("payload is missing", getProtocol());
        }
    }

    @Override
    public boolean accept(final Buffer data) {
        try {
            PCapPacket pkt = frame(null, data);
            if (pkt.getCapturedLength() < 0 || pkt.getTotalLength() < 0) {
                return false;
            }
        } catch (NegativeArraySizeException | IOException | IndexOutOfBoundsException | FramingException e) {
            return false;
        }
        return true;
    }
}
