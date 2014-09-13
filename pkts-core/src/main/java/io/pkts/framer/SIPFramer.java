/**
 * 
 */
package io.pkts.framer;

import io.pkts.buffer.Buffer;
import io.pkts.packet.TransportPacket;
import io.pkts.packet.sip.SipMessage;
import io.pkts.packet.sip.SipPacket;
import io.pkts.packet.sip.impl.SipParser;
import io.pkts.packet.sip.impl.SipRequestPacketImpl;
import io.pkts.packet.sip.impl.SipResponsePacketImpl;
import io.pkts.protocol.Protocol;

import java.io.IOException;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public final class SIPFramer implements Framer<TransportPacket> {

    public SIPFramer() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Protocol getProtocol() {
        return Protocol.SIP;
    }

    /**
     * {@inheritDoc}
     * 
     * Very basic way of framing a sip message. It makes a lot of assumption but
     * in the framing phase we are, well, just framing.
     */
    @Override
    public SipPacket frame(final TransportPacket parent, final Buffer buffer) throws IOException {

        if (parent == null) {
            throw new IllegalArgumentException("The parent frame cannot be null");
        }

        final SipMessage sip = SipParser.frame(buffer);
        if (sip.isRequest()) {
            return new SipRequestPacketImpl(parent, sip.toRequest());
        }

        return new SipResponsePacketImpl(parent, sip.toResponse());
    }

    @Override
    public boolean accept(final Buffer data) throws IOException {
        return SipParser.couldBeSipMessage(data);
    }

}
