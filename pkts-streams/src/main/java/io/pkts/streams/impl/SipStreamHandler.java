/**
 * 
 */
package io.pkts.streams.impl;

import io.pkts.frame.PcapGlobalHeader;
import io.pkts.framer.Framer;
import io.pkts.framer.FramerManager;
import io.pkts.packet.Packet;
import io.pkts.packet.PacketParseException;
import io.pkts.packet.sip.SipPacket;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.SipRequestPacket;
import io.pkts.packet.sip.SipResponsePacket;
import io.pkts.protocol.Protocol;
import io.pkts.sdp.RTPInfo;
import io.pkts.sdp.SDP;
import io.pkts.streams.SipStatistics;
import io.pkts.streams.SipStream;
import io.pkts.streams.Stream;
import io.pkts.streams.StreamId;
import io.pkts.streams.StreamListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jonas@jonasborjesson.com
 */
public class SipStreamHandler {

    private static final Logger logger = LoggerFactory.getLogger(SipStreamHandler.class);

    private final Map<StreamId, BasicSipStream> sipStreams = new HashMap<StreamId, BasicSipStream>();
    private final Map<StreamId, BasicSipStream> terminatedStreams = new HashMap<StreamId, BasicSipStream>();

    private StreamListener<SipPacket> sipListener;

    /**
     * We use the framer manager to update hints when it comes to what protocols
     * the {@link FramerManager} can expect to see from certain ports etc. This
     * will help it figure out if e.g. there is RTP being sent to/from a
     * particular port pair and as such it is more likely that it will pick the
     * correct {@link Framer}.
     */
    private final FramerManager framerManager;

    private final SipStatisticsImpl stats = new SipStatisticsImpl();

    /**
     * 
     */
    public SipStreamHandler(final FramerManager framerManager) {
        this.framerManager = framerManager;
    }

    private StreamId getStreamId(final SipPacket msg) throws SipParseException {
        try {
            return new BufferStreamId(msg.getCallIDHeader().getValue());
        } catch (final NullPointerException e) {
            System.err.println("============= holy shit, we blew up =================");
            System.err.println(msg);
            System.err.println("============= holy shit, we blew up =================");
            throw e;

        }
    }

    public void processFrame(final Packet frame) throws PacketParseException {
        try {
            final SipPacket msg = (SipPacket) frame.getPacket(Protocol.SIP);
            final StreamId id = getStreamId(msg);
            if (id == null) {
                return;
            }
            this.stats.count(msg);
            if (msg.isInfo() || msg.isOptions() || msg.isMessage()) {
                return;
            }
            // checkMessageForContent(msg);
            BasicSipStream stream = this.sipStreams.get(id);
            if (stream == null) {
                stream = this.terminatedStreams.get(id);
            }
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
                stream = new BasicSipStream(header, id);
                stream.addMessage(msg);
                notifyStartStream(stream, msg);
                this.sipStreams.put(id, stream);
            } else {
                final boolean wasAlreadyTerminated = stream.isTerminated();
                stream.addMessage(msg);
                notifyPacketReceived(stream, msg);
                if (!wasAlreadyTerminated && stream.isTerminated()) {
                    this.sipStreams.remove(id);
                    this.terminatedStreams.put(id, stream);
                    notifyEndStream(stream);
                }
            }
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void notifyStartStream(final SipStream stream, final SipPacket pkt) {
        if (this.sipListener != null) {
            this.sipListener.startStream(stream, pkt);
        }
    }

    private void notifyPacketReceived(final SipStream stream, final SipPacket pkt) {
        if (this.sipListener != null) {
            this.sipListener.packetReceived(stream, pkt);
        }
    }

    private void notifyEndStream(final SipStream stream) {
        if (this.sipListener != null) {
            this.sipListener.endStream(stream);
        }
    }

    /**
     * Check whether a {@link SipPacket} has a message body and if it is SDP
     * then figure out what ports etc we can expect to see RTP on (if that is
     * what is being advertised) and tell the {@link FramerManager} about this.
     * 
     * @param msg
     */
    private void checkMessageForContent(final SipPacket msg) {
        if (!msg.hasContent()) {
            return;
        }

        try {
            final Object content = msg.getContent();
            if (content instanceof SDP) {
                for (final RTPInfo rtpInfo : ((SDP) content).getRTPInfo()) {
                    final String address = rtpInfo.getAddress();
                    final int port = rtpInfo.getMediaPort();
                    // System.out.println("Address: " + address + " port : " + port);
                }
            }
        } catch (final SipParseException e) {
            // System.err.println("Ok so the total length is: " + msg.getTotalLength());
            logger.warn("Unable to parse the content of the sip message", e);
            // System.exit(1);
        }

    }

    public void addListener(final StreamListener<SipPacket> listener) {
        this.sipListener = listener;
    }

    public SipStatistics getStatistics() {
        return this.stats;
    }

    private static class SipStatisticsImpl implements SipStatistics {

        private long total;

        private long inviteRequests;

        private long byeRequests;

        private long ackRequests;

        private long optionsRequests;

        private long messageRequests;

        private long infoRequests;

        private long cancelRequests;

        private final int[] responses = new int[600];

        public SipStatisticsImpl() {
            // left empty intentionally
        }

        public void count(final SipPacket msg) throws SipParseException {
            ++this.total;
            if (msg.isRequest()) {
                countRequest(msg.toRequest());
            } else {
                countResponse(msg.toResponse());
            }
        }

        private void countRequest(final SipRequestPacket request) throws SipParseException {
            if (request.isInvite()) {
                ++this.inviteRequests;
            } else if (request.isAck()) {
                ++this.ackRequests;
            } else if (request.isBye()) {
                ++this.byeRequests;
            } else if (request.isOptions()) {
                ++this.optionsRequests;
            } else if (request.isCancel()) {
                ++this.cancelRequests;
            } else if (request.isMessage()) {
                ++this.messageRequests;
            } else if (request.isInfo()) {
                ++this.infoRequests;
            }
        }

        private void countResponse(final SipResponsePacket response) throws SipParseException {
            ++this.responses[response.getStatus() - 100];
        }

        @Override
        public long totalSipMessages() {
            return this.total;
        }

        @Override
        public long totalInviteRequests() {
            return this.inviteRequests;
        }

        @Override
        public long totalAckRequests() {
            return this.ackRequests;
        }

        @Override
        public long totalByeRequests() {
            return this.byeRequests;
        }

        @Override
        public long totalOptionsRequests() {
            return this.optionsRequests;
        }

        @Override
        public long totalInfoRequests() {
            return this.infoRequests;
        }

        @Override
        public long totalCancelRequests() {
            return this.cancelRequests;
        }

        @Override
        public int[] totalResponses() {
            return this.responses;
        }

        public void dump() {
            final int[] responses = totalResponses();
            for (int i = 0; i < responses.length; ++i) {
                if (responses[i] > 0) {
                    System.out.println(i + 100 + ": " + responses[i]);
                }
            }
        }

        @Override
        public String dumpInfo() {
            final StringBuilder sb = new StringBuilder();
            sb.append("Total: ").append(this.total);
            sb.append("\nRequests");
            sb.append("\n   INVITE: ").append(this.inviteRequests);
            sb.append("\n   ACK: ").append(this.ackRequests);
            sb.append("\n   OPTIONS: ").append(this.optionsRequests);
            sb.append("\n   BYE: ").append(this.byeRequests);
            sb.append("\n   MESSAGE: ").append(this.messageRequests);
            sb.append("\n   CANCEL: ").append(this.cancelRequests);
            sb.append("\n   INFO: ").append(this.infoRequests);
            sb.append("\nResponses: ");
            for (int i = 0; i < this.responses.length; ++i) {
                if (this.responses[i] > 0) {
                    sb.append("\n   ").append(i + 100).append(": ").append(this.responses[i]);
                }
            }
            return sb.toString();
        }
    }

    public Map<StreamId, ? extends Stream> getStreams() {
        return this.sipStreams;
    }
}
