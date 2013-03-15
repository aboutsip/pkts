/**
 * 
 */
package com.aboutsip.streams.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aboutsip.sdp.RTPInfo;
import com.aboutsip.sdp.SDP;
import com.aboutsip.streams.SipStatistics;
import com.aboutsip.streams.Stream;
import com.aboutsip.streams.StreamId;
import com.aboutsip.streams.StreamListener;
import com.aboutsip.yajpcap.frame.Frame;
import com.aboutsip.yajpcap.frame.SipFrame;
import com.aboutsip.yajpcap.framer.Framer;
import com.aboutsip.yajpcap.framer.FramerManager;
import com.aboutsip.yajpcap.packet.PacketParseException;
import com.aboutsip.yajpcap.packet.sip.SipMessage;
import com.aboutsip.yajpcap.packet.sip.SipRequest;
import com.aboutsip.yajpcap.packet.sip.SipResponse;
import com.aboutsip.yajpcap.packet.sip.impl.SipParseException;
import com.aboutsip.yajpcap.protocol.Protocol;

/**
 * @author jonas@jonasborjesson.com
 */
public class SipStreamHandler {

    private static final Logger logger = LoggerFactory.getLogger(SipStreamHandler.class);

    private final Map<StreamId, BasicSipStream> sipStreams = new HashMap<StreamId, BasicSipStream>();
    private final Map<StreamId, BasicSipStream> terminatedStreams = new HashMap<StreamId, BasicSipStream>();

    private StreamListener<SipMessage> sipListener;

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

    private StreamId getStreamId(final SipMessage msg) throws SipParseException {
        return new BufferStreamId(msg.getCallIDHeader().getValue());
    }

    public void processFrame(final Frame frame) throws PacketParseException {
        try {
            final SipFrame sipFrame = (SipFrame) frame.getFrame(Protocol.SIP);
            final SipMessage msg = sipFrame.parse();
            final StreamId id = getStreamId(msg);
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
                stream = new BasicSipStream(id);
                stream.addMessage(msg);
                this.sipListener.startStream(stream, msg);
                this.sipStreams.put(id, stream);
            } else {
                final boolean wasAlreadyTerminated = stream.isTerminated();
                stream.addMessage(msg);
                this.sipListener.packetReceived(stream, msg);
                if (!wasAlreadyTerminated && stream.isTerminated()) {
                    this.sipStreams.remove(id);
                    this.terminatedStreams.put(id, stream);
                    this.sipListener.endStream(stream);
                }
            }
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Check whether a {@link SipMessage} has a message body and if it is SDP
     * then figure out what ports etc we can expect to see RTP on (if that is
     * what is being advertised) and tell the {@link FramerManager} about this.
     * 
     * @param msg
     */
    private void checkMessageForContent(final SipMessage msg) {
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

    public void addListener(final StreamListener<SipMessage> listener) {
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

        public void count(final SipMessage msg) throws SipParseException {
            ++this.total;
            if (msg.isRequest()) {
                countRequest(msg.toRequest());
            } else {
                countResponse(msg.toResponse());
            }
        }

        private void countRequest(final SipRequest request) throws SipParseException {
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

        private void countResponse(final SipResponse response) throws SipParseException {
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
