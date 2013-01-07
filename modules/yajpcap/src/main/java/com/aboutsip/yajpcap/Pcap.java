package com.aboutsip.yajpcap;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
    private final FramerManager framerManager;

    private Pcap(final PcapGlobalHeader header, final Buffer buffer) {
        assert header != null;
        assert buffer != null;
        this.header = header;
        this.buffer = buffer;
        this.framerManager = FramerManager.getInstance();
    }

    public void loop(final FrameHandler callback) throws IOException {
        final ByteOrder byteOrder = this.header.getByteOrder();
        final PcapFramer framer = new PcapFramer(byteOrder, this.framerManager);

        Frame frame = null;
        while ((frame = framer.frame(null, this.buffer)) != null) {
            final long time = frame.getArrivalTime();
            this.framerManager.tick(time);
            callback.nextFrame(frame);
        }

    }

    /**
     * Create an {@link PcapOutputStream} based on this {@link Pcap}. The new
     * {@link PcapOutputStream} is configured to use the same
     * {@link PcapGlobalHeader} as the {@link Pcap} is using which means that
     * you can just safely write frames back out to this
     * {@link PcapOutputStream}. Good for those applications that needs to
     * filter a {@link Pcap} and write out new files.
     * 
     * @param out
     * @return
     * @throws IllegalArgumentException
     */
    public PcapOutputStream createOutputStream(final OutputStream out) throws IllegalArgumentException {
        return PcapOutputStream.create(this.header, out);
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
