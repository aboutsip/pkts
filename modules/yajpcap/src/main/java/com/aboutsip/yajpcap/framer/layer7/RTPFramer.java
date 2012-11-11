/**
 * 
 */
package com.aboutsip.yajpcap.framer.layer7;

import java.io.IOException;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.yajpcap.frame.layer4.Layer4Frame;
import com.aboutsip.yajpcap.frame.layer7.Layer7Frame;
import com.aboutsip.yajpcap.frame.layer7.RtpFrame;
import com.aboutsip.yajpcap.framer.FramerManager;
import com.aboutsip.yajpcap.protocol.Protocol;

/**
 * @author jonas@jonasborjesson.com
 */
public final class RTPFramer implements Layer7Framer {

    private final FramerManager framerManager;

    /**
     * 
     */
    public RTPFramer(final FramerManager framerManager) {
        this.framerManager = framerManager;
    }

    @Override
    public Protocol getProtocol() {
        return Protocol.RTP;
    }

    /**
     * There is no real good test to make sure that the data indeed is an RTP
     * packet. Appendix 2 in RFC3550 describes one way of doing it but you
     * really need a sequence of packets in order to be able to determine if
     * this indeed is a RTP packet or not. The best is to analyze the session
     * negotiation but here we are just looking at a single packet so can't do
     * that.
     * 
     * {@inheritDoc}
     */
    @Override
    public boolean accept(final Buffer data) throws IOException {

        // a RTP packet has at least 12 bytes. Check that
        if (data.readableBytes() < 12) {
            // not enough bytes but see if we actually could
            // get another 12 bytes by forcing the underlying
            // implementation to read further ahead
            data.markReaderIndex();
            final Buffer b = data.readBytes(12);
            if (b.capacity() < 12) {
                return false;
            }
            data.resetReaderIndex();
        }

        // check the version. Currently we only check for version 2
        // and if this is true then we'll just return true.
        final byte b = data.getByte(0);
        return ((b & 0xC0) >> 6) == 0x02;
    }

    @Override
    public Layer7Frame frame(final Layer4Frame parent, final Buffer buffer) throws IOException {
        if (parent == null) {
            throw new IllegalArgumentException("The parent frame cannot be null");
        }

        // TODO: can be more. Just testing right now
        final Buffer headers = buffer.readBytes(12);
        final Buffer payload = buffer.slice();

        return new RtpFrame(this.framerManager, parent, headers, payload);
    }

}
