package com.aboutsip.yajpcap;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.buffer.Buffers;
import com.aboutsip.yajpcap.frame.Frame;
import com.aboutsip.yajpcap.frame.PcapGlobalHeader;
import com.aboutsip.yajpcap.framer.FramerManager;
import com.aboutsip.yajpcap.framer.PcapFramer;

/**
 * 
 * @author jonas@jonasborjesson.com
 * 
 */
public class Pcap {

    private final PcapGlobalHeader header;
    private final Buffer buffer;

    private Pcap(final PcapGlobalHeader header, final Buffer buffer) {
        assert header != null;
        assert buffer != null;
        this.header = header;
        this.buffer = buffer;
    }

    public void loop(final FrameHandler callback) throws IOException {
        final FramerManager framerManager = FramerManager.getInstance();
        final ByteOrder byteOrder = this.header.getByteOrder();
        final PcapFramer framer = new PcapFramer(byteOrder, framerManager);

        Frame frame = null;
        while ((frame = framer.frame(this.buffer)) != null) {
            callback.nextFrame(frame);
        }

        // frame = framer.frame(this.header.getByteOrder(), this.in);
        // callback.nextFrame(frame);

        // final Frame etherFrame = frame.getNextFrame();
        // final Frame ipFrame = etherFrame.getNextFrame();
        // final UDPFrame udpFrame = (UDPFrame)ipFrame.getNextFrame();

        // System.out.println("Source Port: " + udpFrame.getSourcePort());
        // System.out.println("Destination Port: " +
        // udpFrame.getDestinationPort());

        /*
         * final byte[] p = frame.getPayload(); final byte[] sip = new
         * byte[p.length - 42]; System.arraycopy(p, 42, sip, 0, sip.length);
         * System.out.println(new String(sip));
         */

    }

    /**
     * Capture packets from the input stream
     * 
     * @param is
     * @return
     * @throws IOException
     */
    public static Pcap openStream(final InputStream is) throws IOException {
        final Buffer stream = Buffers.wrap(is);
        final PcapGlobalHeader header = PcapGlobalHeader.parse(stream);
        return new Pcap(header, stream);
    }

    public void close() {
        System.err.println("closing...");
    }

}
