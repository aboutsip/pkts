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
import com.aboutsip.streams.StreamId;
import com.aboutsip.streams.StreamListener;
import com.aboutsip.yajpcap.frame.Frame;
import com.aboutsip.yajpcap.frame.SipFrame;
import com.aboutsip.yajpcap.framer.Framer;
import com.aboutsip.yajpcap.framer.FramerManager;
import com.aboutsip.yajpcap.packet.PacketParseException;
import com.aboutsip.yajpcap.packet.sip.SipMessage;
import com.aboutsip.yajpcap.packet.sip.impl.SipParseException;
import com.aboutsip.yajpcap.protocol.Protocol;

/**
 * @author jonas@jonasborjesson.com
 */
public class SipStreamHandler {

    private static final Logger logger = LoggerFactory.getLogger(SipStreamHandler.class);

    private final Map<StreamId, BasicSipStream> sipStreams = new HashMap<StreamId, BasicSipStream>();

    private StreamListener<SipMessage> sipListener;

    /**
     * We use the framer manager to update hints when it comes to what protocols
     * the {@link FramerManager} can expect to see from certain ports etc. This
     * will help it figure out if e.g. there is RTP being sent to/from a
     * particular port pair and as such it is more likely that it will pick the
     * correct {@link Framer}.
     */
    private final FramerManager framerManager;

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
            final SipFrame sipFrame = ((SipFrame) frame.getFrame(Protocol.SIP));
            final SipMessage msg = sipFrame.parse();
            final StreamId id = getStreamId(msg);
            // checkMessageForContent(msg);
            BasicSipStream stream = this.sipStreams.get(id);
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

}
