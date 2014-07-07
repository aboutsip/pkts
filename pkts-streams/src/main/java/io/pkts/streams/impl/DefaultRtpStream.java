/**
 * 
 */
package io.pkts.streams.impl;

import io.pkts.frame.PcapGlobalHeader;
import io.pkts.packet.rtp.RtpPacket;
import io.pkts.streams.RtpStream;
import io.pkts.streams.StreamId;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public final class DefaultRtpStream implements RtpStream {

    private static final Logger logger = LoggerFactory.getLogger(DefaultRtpStream.class);

    private final PcapGlobalHeader globalHeader;

    private final LongStreamId ssrc;

    private final NavigableSet<RtpPacket> packets;

    /**
     * 
     */
    public DefaultRtpStream(final PcapGlobalHeader globalHeader, final long ssrc) {
        this.globalHeader = globalHeader;
        this.ssrc = new LongStreamId(ssrc);
        this.packets = new TreeSet<RtpPacket>(new PacketComparator());
    }

    public void onPacket(final RtpPacket msg) {

        if (msg == null) {
            return;
        }

        final RtpPacket previousMsg = this.packets.isEmpty() ? null : this.packets.last();
        this.packets.add(msg);

        if (previousMsg != null && msg.getArrivalTime() < previousMsg.getArrivalTime()) {
            redrive();
            return;
        }

    }

    /**
     * If we merge multiple pcaps we may get packets "arriving" out-of-order. For now, this is a
     * very simple approach where we just redrive everything, which won't be the most efficient if
     * you merge a large pcap that was captured before the pcap you already loaded but the merge
     * command should take care of that and make sure that we merge things in the correct order so
     * therefore we dont care here...
     */
    private void redrive() {
        // TODO
        if (logger.isInfoEnabled()) {
            logger.info("Out-of-sequence event detected. Redriving all traffic.");
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RtpPacket> getPackets() {
        return new ArrayList<RtpPacket>(this.packets);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getDuration() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getTimeOfFirstPacket() {
        if (this.packets.isEmpty()) {
            return -1;
        }

        return this.packets.first().getArrivalTime();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getTimeOfLastPacket() {
        if (this.packets.isEmpty()) {
            return -1;
        }

        return this.packets.last().getArrivalTime();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StreamId getStreamIdentifier() {
        return this.ssrc;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(final OutputStream out) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public long getMaxJitter() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long getMeanJitter() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long getMaxDelta() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long getLostPackets() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long getSequenceErrors() {
        // TODO Auto-generated method stub
        return 0;
    }

}
