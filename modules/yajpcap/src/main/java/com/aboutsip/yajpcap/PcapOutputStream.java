/**
 * 
 */
package com.aboutsip.yajpcap;

import java.io.IOException;
import java.io.OutputStream;

import com.aboutsip.yajpcap.frame.Frame;
import com.aboutsip.yajpcap.frame.PcapGlobalHeader;
import com.aboutsip.yajpcap.packet.Packet;

/**
 * @author jonas@jonasborjesson.com
 */
public class PcapOutputStream extends OutputStream {

    /**
     * The underlying {@link OutputStream} we will be using for writing the
     * actual data.
     */
    private final OutputStream out;

    private boolean isInitialized = false;


    /**
     * 
     */
    public PcapOutputStream(final OutputStream out) {
        this.out = out;
    }

    private void initialize() throws IOException {
        if (!this.isInitialized) {
            // TODO: if dumping frames, we need to know the byte order
            final PcapGlobalHeader header = PcapGlobalHeader.createDefaultHeader();
            header.write(this.out);
            this.isInitialized = true;
        }
    }

    /**
     * Write a {@link Frame} to the outputstream.
     * 
     * @param frame
     *            the frame to write. If null is passed in, it will silently be
     *            ignored.
     */
    public void write(final Frame frame) throws IOException {
        if (frame == null) {
            return;
        }

        initialize();
        frame.write(this);
    }

    /**
     * Write a {@link Packet} to the outputstream.
     * 
     * @param packet
     *            the packet to write. If null is passed in, it will silently be
     *            ignored.
     */
    public void write(final Packet packet) throws IOException {
        if (packet == null) {
            return;
        }

        initialize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(final int b) throws IOException {
        // TODO: should we allow this?
        this.out.write(b);
    }

}
