/**
 * 
 */
package io.pkts.framer;

import io.pkts.buffer.Buffer;
import io.pkts.packet.IPPacket;
import io.pkts.packet.sctp.SctpPacket;
import io.pkts.protocol.Protocol;

import java.io.IOException;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public final class SctpFramer implements Framer<IPPacket, SctpPacket> {

    public SctpFramer() {
        // left empty intentionally
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Protocol getProtocol() {
        return Protocol.SCTP;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SctpPacket frame(final IPPacket parent, final Buffer buffer) throws IOException {
        return SctpPacket.frame(parent, buffer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean accept(final Buffer data) throws IOException {
        // TODO Auto-generated method stub
        return false;
    }

}
