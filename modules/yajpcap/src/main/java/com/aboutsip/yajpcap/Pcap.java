package com.aboutsip.yajpcap;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.buffer.Buffers;
import com.aboutsip.yajpcap.frame.Frame;
import com.aboutsip.yajpcap.frame.layer1.PcapGlobalHeader;
import com.aboutsip.yajpcap.framer.FramerManager;
import com.aboutsip.yajpcap.framer.layer1.PcapFramer;

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
        while ((frame = framer.frame(null, this.buffer)) != null) {
            callback.nextFrame(frame);
        }

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
        // TODO
    }

}
