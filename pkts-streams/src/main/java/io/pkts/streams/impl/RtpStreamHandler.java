/**
 * 
 */
package io.pkts.streams.impl;

import io.pkts.frame.PcapGlobalHeader;
import io.pkts.framer.Framer;
import io.pkts.framer.FramerManager;
import io.pkts.packet.Packet;
import io.pkts.packet.PacketParseException;
import io.pkts.packet.rtp.RtpPacket;
import io.pkts.protocol.Protocol;
import io.pkts.streams.RtpStream;
import io.pkts.streams.StreamListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jonas@jonasborjesson.com
 */
public class RtpStreamHandler {

    private static final Logger logger = LoggerFactory.getLogger(RtpStreamHandler.class);

    private StreamListener<RtpPacket> rtpListener;

    private final Map<Long, DefaultRtpStream> streams = new HashMap<Long, DefaultRtpStream>();

    /**
     * We use the framer manager to update hints when it comes to what protocols the
     * {@link FramerManager} can expect to see from certain ports etc. This will help it figure out
     * if e.g. there is RTP being sent to/from a particular port pair and as such it is more likely
     * that it will pick the correct {@link Framer}.
     */
    private final FramerManager framerManager;

    /**
     * 
     */
    public RtpStreamHandler(final FramerManager framerManager) {
        this.framerManager = framerManager;
    }

    public void processFrame(final Packet frame) throws PacketParseException {
        try {
            final RtpPacket rtp = (RtpPacket) frame.getPacket(Protocol.RTP);
            final long ssrc = rtp.getSyncronizationSource();
            DefaultRtpStream stream = this.streams.get(ssrc);
            if (stream == null) {
                // TODO: need to fix this.
                PcapGlobalHeader header = null;
                if (frame.hasProtocol(Protocol.SLL)) {
                    header = PcapGlobalHeader.createDefaultHeader(Protocol.SLL);
                } else if (frame.hasProtocol(Protocol.ETHERNET_II)) {
                    header = PcapGlobalHeader.createDefaultHeader(Protocol.ETHERNET_II);
                } else {
                    throw new PacketParseException(0, "Unable to create the PcapGlobalHeader because the "
                            + "link type isn't recognized. Currently only Ethernet II "
                            + "and Linux SLL (linux cooked capture) are implemented");
                }
                stream = new DefaultRtpStream(header, ssrc);
                this.streams.put(ssrc, stream);
                notifyStartStream(stream, rtp);
            } else {
                notifyPacketReceived(stream, rtp);
            }
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void addListener(final StreamListener<RtpPacket> listener) {
        this.rtpListener = listener;
    }

    private void notifyStartStream(final RtpStream stream, final RtpPacket pkt) {
        if (this.rtpListener != null) {
            this.rtpListener.startStream(stream, pkt);
        }
    }

    private void notifyPacketReceived(final RtpStream stream, final RtpPacket pkt) {
        if (this.rtpListener != null) {
            this.rtpListener.packetReceived(stream, pkt);
        }
    }

    private void notifyEndStream(final RtpStream stream) {
        if (this.rtpListener != null) {
            this.rtpListener.endStream(stream);
        }
    }

}
