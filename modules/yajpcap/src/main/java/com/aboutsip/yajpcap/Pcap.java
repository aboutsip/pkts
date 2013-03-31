package com.aboutsip.yajpcap;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteOrder;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.buffer.Buffers;
import com.aboutsip.yajpcap.filters.Filter;
import com.aboutsip.yajpcap.filters.FilterFactory;
import com.aboutsip.yajpcap.filters.FilterParseException;
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

    /**
     * If the filter is set then only frames that are accepted by the filter
     * will be further processed.
     */
    private Filter filter = null;

    private final FilterFactory filterFactory = FilterFactory.getInstance();

    private Pcap(final PcapGlobalHeader header, final Buffer buffer) {
        assert header != null;
        assert buffer != null;
        this.header = header;
        this.buffer = buffer;
        this.framerManager = FramerManager.getInstance();
    }

    /**
     * It is possible to specify a filter so that only packets that matches the
     * filter will be passed onto the registered {@link FrameHandler}.
     * 
     * E.g., to only accept packets of type sip with the Call-ID of "123" you
     * could pass in the following filter:
     * 
     * "sip.Call-ID == 123"
     * 
     * @param expression
     *            the expression. If the expression is null or the empty string,
     *            it will silently be ignored.
     * @throws FilterParseException
     *             in case the expression is not a valid filter expression.
     */
    public void setFilter(final String expression) throws FilterParseException {
        if (expression != null && !expression.isEmpty()) {
            this.filter = this.filterFactory.createFilter(expression);
        }
    }

    public void loop(final FrameHandler callback) throws IOException {
        final ByteOrder byteOrder = this.header.getByteOrder();
        final PcapFramer framer = new PcapFramer(byteOrder, this.framerManager);

        Frame frame = null;
        while ((frame = framer.frame(null, this.buffer)) != null) {
            final long time = frame.getArrivalTime();
            this.framerManager.tick(time);
            if (this.filter == null) {
                callback.nextFrame(frame);
            } else if (this.filter != null && this.filter.accept(frame)) {
                callback.nextFrame(frame);
            }
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
