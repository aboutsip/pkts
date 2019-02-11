/**
 * 
 */
package io.pkts.packet.impl;

import io.pkts.buffer.Buffer;
import io.pkts.frame.PcapGlobalHeader;
import io.pkts.frame.PcapRecordHeader;
import io.pkts.framer.EthernetFramer;
import io.pkts.framer.FramingException;
import io.pkts.framer.IPv4Framer;
import io.pkts.framer.SllFramer;
import io.pkts.packet.PCapPacket;
import io.pkts.packet.PacketParseException;
import io.pkts.packet.Packet;
import io.pkts.protocol.Protocol;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * TODO: may rename this to a frame instead since this is a little different
 * than a "real" protocol packet.
 * 
 * @author jonas@jonasborjesson.com
 */
public final class PCapPacketImpl extends AbstractPacket implements PCapPacket {

    private final PcapRecordHeader pcapHeader;

    private static final SllFramer sllFramer = new SllFramer();
    private static final EthernetFramer ethernetFramer = new EthernetFramer();
    private static final IPv4Framer ipFramer = new IPv4Framer();
    private final PcapGlobalHeader pcapGlobalHeader;

    /**
     * Constructor which assumes an Ethernet link layer.
     */
    public PCapPacketImpl(final PcapRecordHeader header, final Buffer payload) {
        super(Protocol.PCAP, null, payload);
        this.pcapGlobalHeader = PcapGlobalHeader.createDefaultHeader();
        this.pcapHeader = header;
    }

    /**
     * Constructor which uses the PCAP file's global header to support more than just Ethernet link layers
     */
    public PCapPacketImpl(PcapGlobalHeader pcapGlobalHeader, final PcapRecordHeader header, final Buffer payload) {
        super(Protocol.PCAP, null, payload);
        this.pcapGlobalHeader = pcapGlobalHeader;
        this.pcapHeader = header;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getArrivalTime() {
        final long multiplier = pcapGlobalHeader.timestampsInNs() ? 1000000000 : 1000000;

        return this.pcapHeader.getTimeStampSeconds() * multiplier + this.pcapHeader.getTimeStampMicroOrNanoSeconds();
    }

    @Override
    public long getTotalLength() {
        return this.pcapHeader.getTotalLength();
    }

    @Override
    public long getCapturedLength() {
        return this.pcapHeader.getCapturedLength();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void verify() {
        // nothing to verify for the pcap packet since that would
        // have been detected when we framed the pcap packet.
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        final SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS");
        final Date date = new Date(getArrivalTime() / 1000);
        sb.append("Arrival Time: ").append(formatter.format(date))
          .append(" Epoch Time: ").append(this.pcapHeader.getTimeStampSeconds()).append(".")
          .append(String.format("%09d", this.pcapHeader.getTimeStampMicroOrNanoSeconds()))
          .append(" Frame Length: ").append(getTotalLength())
          .append(" Capture Length: ").append(getCapturedLength());

        return sb.toString();
    }

    @Override
    public void write(final OutputStream out, final Buffer payload) throws IOException {
        final int size = payload.getReadableBytes();
        this.pcapHeader.setCapturedLength(size);
        this.pcapHeader.setTotalLength(size);
        this.pcapHeader.write(out);
        out.write(payload.getArray());
    }

    @Override
    public PCapPacket clone() {
        throw new RuntimeException("not implemented yet");
    }

    @Override
    public Packet getNextPacket() throws IOException, PacketParseException {
        final Buffer payload = getPayload();
        if (payload == null) {
            return null;
        }

        switch(pcapGlobalHeader.getDataLinkType())
        {
            case 1:
            default:
                try {
                    return ethernetFramer.frame(this, payload);
                } catch (FramingException e) {
                    throw new PacketParseException(16, "Ethernet parsing failed", e);
                }
            case 113:
                return sllFramer.frame(this, payload);
            case 101:
                return ipFramer.frame(this, payload);
        }
    }

}
