/**
 * 
 */
package com.aboutsip.yajpcap.frame;

import java.io.IOException;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.yajpcap.framer.FramerManager;
import com.aboutsip.yajpcap.packet.Packet;
import com.aboutsip.yajpcap.packet.PacketParseException;
import com.aboutsip.yajpcap.protocol.Protocol;

/**
 * @author jonas@jonasborjesson.com
 */
public final class SDPFrame extends AbstractFrame {

    private final Buffer sdp;

    /**
     * @param framerManager
     * @param sdp the raw SDP
     */
    public SDPFrame(final FramerManager framerManager, final Buffer sdp) {
        super(framerManager, Protocol.SDP, null);
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
    public Packet parse() throws PacketParseException {
        // TODO Auto-generated method stub
        return null;
    }

}
