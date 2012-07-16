/**
 * 
 */
package com.aboutsip.yajpcap.framer;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteOrder;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.yajpcap.frame.Frame;
import com.aboutsip.yajpcap.frame.PcapFrame;
import com.aboutsip.yajpcap.frame.PcapRecordHeader;
import com.aboutsip.yajpcap.protocol.Protocol;

/**
 * @author jonas@jonasborjesson.com
 */
public final class PcapFramer implements Framer {

    private final FramerManager framerManager;
    private final ByteOrder byteOrder;

    /**
     * 
     */
    public PcapFramer(final ByteOrder byteOrder, final FramerManager framerManager) {
        assert byteOrder != null;
        assert framerManager != null;

        this.byteOrder = byteOrder;
        this.framerManager = framerManager;
    }

    @Override
    public Protocol getProtocol() {
        return Protocol.PCAP;
    }

    @Override
    public Frame frame(final Buffer buffer) throws IOException {

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

        final FramerManager framerManager = FramerManager.getInstance();
        return new PcapFrame(framerManager, header, payload);
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
    public PcapFrame frame(final ByteOrder byteOrder, final BufferedInputStream in) throws IOException {
        // not enough bytes in the stream
        final int l = in.available();
        if (l == -1) {
            System.err.println("end-of-stream");
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
