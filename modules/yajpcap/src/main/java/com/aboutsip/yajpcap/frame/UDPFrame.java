/**
 * 
 */
package com.aboutsip.yajpcap.frame;

import java.io.IOException;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.yajpcap.framer.Framer;
import com.aboutsip.yajpcap.framer.FramerManager;
import com.aboutsip.yajpcap.packet.TransportPacket;
import com.aboutsip.yajpcap.packet.impl.TransportPacketImpl;
import com.aboutsip.yajpcap.protocol.Protocol;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public final class UDPFrame extends AbstractFrame {

    private final Buffer headers;

    /**
     * 
     */
    public UDPFrame(final FramerManager framerManager, final Buffer headers, final Buffer payload) {
        super(framerManager, Protocol.UDP, payload);
        assert headers != null;
        assert payload != null;

        this.headers = headers;
    }


    public int getSourcePort() {
        return this.headers.getUnsignedShort(0);
    }

    public int getDestinationPort() {
        return this.headers.getUnsignedShort(2);
    }

    public int getLength() {
        return this.headers.getUnsignedShort(4);
    }

    public int getChecksum() {
        return this.headers.getUnsignedShort(6);
    }

    @Override
    protected Frame framePayload(final FramerManager framerManager, final Buffer payload) throws IOException {

        final Framer framer = framerManager.getFramer(payload);
        if (framer != null) {
            return framer.frame(payload);
        }

        // unknown payload
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TransportPacket parse() {
        // TODO: perhaps do a UDPPacket
        return new TransportPacketImpl(true, getSourcePort(), getDestinationPort());
    }

}
