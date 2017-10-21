/**
 * 
 */
package io.pkts.packet.impl;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.MACPacket;
import io.pkts.packet.TransportPacket;
import io.pkts.protocol.Protocol;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author jonas@jonasborjesson.com
 */
public abstract class AbstractApplicationPacket extends AbstractPacket implements ApplicationPacket {

    private final TransportPacket parent;

    private final Buffer payload;

    /**
     * @param p
     * @param parent
     * @param payload
     */
    public AbstractApplicationPacket(final Protocol p, final TransportPacket parent, final Buffer payload) {
        super(p, parent, payload);
        this.parent = parent;
        this.payload = payload;
    }

    protected TransportPacket getParent() {
        return this.parent;
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public final long getArrivalTime() {
        return this.parent.getArrivalTime();
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public void write(final OutputStream out, final Buffer payload) throws IOException {
        final Buffer buffer = this.payload != null ? Buffers.wrap(this.payload, payload) : payload;
        this.parent.write(out, buffer);
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public abstract ApplicationPacket clone();

}
