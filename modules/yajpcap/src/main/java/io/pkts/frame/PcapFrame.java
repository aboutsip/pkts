/**
 * 
 */
package io.pkts.frame;

import io.pkts.buffer.Buffer;
import io.pkts.framer.Framer;
import io.pkts.framer.FramerManager;
import io.pkts.packet.PCapPacket;
import io.pkts.packet.PacketParseException;
import io.pkts.packet.impl.PCapPacketImpl;
import io.pkts.protocol.Protocol;

import java.io.IOException;
import java.io.ObjectOutput;
import java.io.OutputStream;


/**
 * The Pcap frame is where it all begins.
 * 
 * @author jonas@jonasborjesson.com
 * 
 */
public final class PcapFrame extends AbstractFrame implements Layer1Frame {

    /**
     * The pcap record header that tells us at what time the packet was
     * captured, the length of the payload etc
     */
    private final PcapRecordHeader header;

    /**
     * 
     */
    public PcapFrame(final FramerManager framerManager, final PcapGlobalHeader globalHeader,
            final PcapRecordHeader header, final Buffer payload) {
        super(framerManager, globalHeader, Protocol.PCAP, payload);
        assert framerManager != null;
        assert header != null;
        this.header = header;
    }

    @Override
    protected Frame framePayload(final FramerManager framerManager, final Buffer payload) throws IOException {
        final Framer sllFramer = framerManager.getFramer(Protocol.SLL);

        if (sllFramer.accept(payload)) {
            return sllFramer.frame(this, payload);
        }

        final Framer ethernetFramer = framerManager.getFramer(Protocol.ETHERNET_II);
        return ethernetFramer.frame(this, payload);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        // out.write(this.header);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PCapPacket parse() throws PacketParseException {
        return new PCapPacketImpl(this.header);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getArrivalTime() {
        return this.header.getTimeStampSeconds() * 1000000 + this.header.getTimeStampMicroSeconds();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(final OutputStream out) throws IOException {
        this.header.write(out);
        out.write(super.getPayload().getArray());
    }

}
