/**
 *
 */
package io.pkts.framer;

import io.pkts.buffer.Buffer;
import io.pkts.diameter.DiameterMessage;
import io.pkts.packet.TransportPacket;
import io.pkts.packet.diameter.DiameterPacket;
import io.pkts.packet.diameter.impl.DiameterPacketImpl;
import io.pkts.protocol.Protocol;

import java.io.IOException;

import static io.pkts.diameter.impl.DiameterParser.couldBeDiameterMessage;

/**
 * @author jonas@jonasborjesson.com
 */
public final class DiameterFramer implements Framer<TransportPacket, DiameterPacket> {

    public DiameterFramer() {
        // left empty intentionally
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Protocol getProtocol() {
        return Protocol.DIAMETER;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Very basic way of framing a sip message. It makes a lot of assumption but
     * in the framing phase we are, well, just framing.
     */
    @Override
    public DiameterPacket frame(final TransportPacket parent, final Buffer buffer) throws IOException {
        if (parent == null) {
            throw new IllegalArgumentException("The parent frame cannot be null");
        }

        final DiameterMessage msg = DiameterMessage.frame(buffer.toReadOnly());
        return new DiameterPacketImpl(parent, msg);
    }

    @Override
    public boolean accept(final Buffer data) throws IOException {
        return couldBeDiameterMessage(data.toReadOnly());
    }

}
