/**
 * 
 */
package io.pkts.framer;

import io.pkts.buffer.Buffer;
import io.pkts.frame.PcapGlobalHeader;
import io.pkts.frame.PcapRecordHeader;
import io.pkts.packet.PCapPacket;
import io.pkts.packet.impl.PCapPacketImpl;
import io.pkts.protocol.Protocol;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteOrder;

/**
 * @author jonas@jonasborjesson.com
 */
public final class PcapFramer implements Framer<PCapPacket> {

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
    public PCapPacket frame(final PCapPacket parent, final Buffer buffer) throws IOException {

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
        final int length = (int) header.getCapturedLength();
        final Buffer payload = buffer.readBytes(length);

        return new PCapPacketImpl(header, payload);
        // return new PcapFrame(framerManager, this.globalHeader, header, payload);
    }

    /**
     * Frame the pcap frame. This frame is the entry into the pcap and we will
     * always frame a pcap one before asking it to frame the rest
     * 
     * @param byteOrder
     * @param in
     * @return the framed PcapFrame or null if nothing left to frame in the
     *         stream
     * @throws IOException
     */
    public PCapPacket frame(final ByteOrder byteOrder, final BufferedInputStream in) throws IOException {
        if (true) {
            throw new RuntimeException("is anyone actually using this one???");
        }
        // not enough bytes in the stream
        final int l = in.available();
        if (l == -1) {
            return null;
        }

        if (l < 16) {
            return null;
        }

        final byte[] record = new byte[16];
        in.read(record);
        return null;
    }

    @Override
    public boolean accept(final Buffer data) {
        // TODO Auto-generated method stub
        return false;
    }

}
