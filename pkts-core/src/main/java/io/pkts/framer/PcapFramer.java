/**
 * 
 */
package io.pkts.framer;

import io.pkts.buffer.Buffer;
import io.pkts.frame.PcapGlobalHeader;
import io.pkts.frame.PcapRecordHeader;
import io.pkts.packet.PCapPacket;
import io.pkts.packet.Packet;
import io.pkts.packet.impl.PCapPacketImpl;
import io.pkts.protocol.Protocol;

import java.io.IOException;
import java.nio.ByteOrder;

/**
 * @author jonas@jonasborjesson.com
 */
public final class PcapFramer implements Framer<Packet, PCapPacket> {

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
    public PCapPacket frame(final Packet parent, final Buffer buffer) throws IOException {

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

        final PcapRecordHeader header = new PcapRecordHeader(this.byteOrder, record, this.globalHeader.timestampsInNs());
        final int length = (int) header.getCapturedLength();
        if (length < 0) {
            throw new FramingException(String.format("Invalid PCAP captured length of %d", length), Protocol.PCAP);
        }
        final int total = (int) header.getTotalLength();
        final Buffer payload = buffer.readBytes(Math.min(length, total));
        return new PCapPacketImpl(globalHeader, header, payload);
    }

    @Override
    public boolean accept(final Buffer data) {
        // TODO Auto-generated method stub
        return false;
    }

}
