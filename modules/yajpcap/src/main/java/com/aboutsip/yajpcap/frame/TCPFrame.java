/**
 * 
 */
package com.aboutsip.yajpcap.frame;

import java.io.IOException;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.yajpcap.framer.Framer;
import com.aboutsip.yajpcap.framer.FramerManager;
import com.aboutsip.yajpcap.packet.PacketParseException;
import com.aboutsip.yajpcap.packet.TransportPacket;
import com.aboutsip.yajpcap.packet.impl.TransportPacketImpl;
import com.aboutsip.yajpcap.protocol.Protocol;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public final class TCPFrame extends AbstractFrame {

    /**
     * The raw tcp headers
     */
    private final Buffer headers;

    /**
     * Options, which may be null
     */
    private final Buffer options;

    /**
     * @param framerManager
     * @param p
     * @param payload
     */
    public TCPFrame(final FramerManager framerManager, final Buffer headers, final Buffer options, final Buffer payload) {
        super(framerManager, Protocol.TCP, payload);
        assert headers != null;
        this.headers = headers;
        this.options = options;
    }

    /**
     * Get the header length in bytes
     * 
     * @return
     */
    public int getHeaderLength() {
        // 20 because the minimum TCP header length is 20 - ALWAYS
        return 20 + (this.options != null ? this.options.capacity() : 0);
    }

    public int getSourcePort() {
        return this.headers.getUnsignedShort(0);
    }

    public int getDestinationPort() {
        return this.headers.getUnsignedShort(2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TransportPacket parse() throws PacketParseException {
        return new TransportPacketImpl(false, getSourcePort(), getDestinationPort());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Frame framePayload(final FramerManager framerManager, final Buffer payload) throws IOException {
        if (payload == null) {
            return null;
        }

        final Framer framer = framerManager.getFramer(payload);
        if (framer != null) {
            return framer.frame(payload);
        }

        // unknown payload
        return null;
    }

}
