/**
 * 
 */
package com.aboutsip.yajpcap.frame;

import java.io.IOException;
import java.io.ObjectOutput;
import java.io.OutputStream;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.yajpcap.framer.FramerManager;
import com.aboutsip.yajpcap.packet.PacketParseException;
import com.aboutsip.yajpcap.packet.impl.ApplicationPacket;
import com.aboutsip.yajpcap.protocol.Protocol;

/**
 * @author jonas@jonasborjesson.com
 */
public final class SDPFrame extends AbstractFrame implements Layer7Frame {

    private final Frame parentFrame;

    /**
     * The raw SDP
     */
    private final Buffer sdp;

    /**
     * @param framerManager
     * @param sdp
     *            the raw SDP
     */
    public SDPFrame(final FramerManager framerManager, final PcapGlobalHeader header, final Frame parentFrame,
            final Buffer sdp) {
        super(framerManager, header, Protocol.SDP, null);
        assert parentFrame != null;
        this.parentFrame = parentFrame;
        this.sdp = sdp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Frame framePayload(final FramerManager framerManager, final Buffer payload) throws IOException {
        // SDP's doesn't have payloads so just return null
        return null;
    }

    /**
     * Get the raw SDP buffer.
     * 
     * @return
     */
    public Buffer getRawSDP() {
        return this.sdp.slice();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        throw new RuntimeException("Not yet implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApplicationPacket parse() throws PacketParseException {
        throw new RuntimeException("Not yet implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(final OutputStream out) {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public long getArrivalTime() {
        return this.parentFrame.getArrivalTime();
    }

}
