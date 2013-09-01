/**
 * 
 */
package io.pkts.filters;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.Packet;
import io.pkts.packet.PacketParseException;
import io.pkts.packet.sip.SipPacket;
import io.pkts.packet.sip.header.SipHeader;
import io.pkts.protocol.Protocol;

import java.io.IOException;

/**
 * @author jonas@jonasborjesson.com
 */
public class SipHeaderFilter extends SipFilter {
    private final Buffer name;
    private final Buffer value;

    public SipHeaderFilter(final String name, final String value) {
        this.name = Buffers.wrap(name);
        this.value = Buffers.wrap(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean accept(final Packet packet) throws FilterException {
        try {
            if (super.accept(packet)) {
                final SipPacket msg = (SipPacket) packet.getPacket(Protocol.SIP);
                final SipHeader header = msg.getHeader(this.name);
                if (header == null) {
                    return false;
                }

                return header.getValue().equals(this.value);
            }
        } catch (final IOException e) {
            throw new FilterException("Unable to process the frame due to IOException", e);
        } catch (final PacketParseException e) {
            throw new FilterException("Unable to process the frame due to parse issue of the SIP Message", e);
        }
        return false;
    }

}
