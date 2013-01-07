/**
 * 
 */
package com.aboutsip.streams.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.aboutsip.streams.StreamId;
import com.aboutsip.streams.StreamListener;
import com.aboutsip.yajpcap.frame.Frame;
import com.aboutsip.yajpcap.frame.SipFrame;
import com.aboutsip.yajpcap.packet.PacketParseException;
import com.aboutsip.yajpcap.packet.sip.SipMessage;
import com.aboutsip.yajpcap.packet.sip.impl.SipParseException;
import com.aboutsip.yajpcap.protocol.Protocol;

/**
 * @author jonas@jonasborjesson.com
 */
public class SipStreamHandler {

    private final Map<StreamId, DefaultSipStream> sipStreams = new HashMap<StreamId, DefaultSipStream>();

    private StreamListener<SipMessage> sipListener;

    /**
     * 
     */
    public SipStreamHandler() {
        // TODO Auto-generated constructor stub
    }

    private StreamId getStreamId(final SipMessage msg) throws SipParseException {
        return new BufferStreamId(msg.getCallIDHeader().getValue());
    }

    public void processFrame(final Frame frame) throws PacketParseException {
        try {
            final SipFrame sipFrame = ((SipFrame) frame.getFrame(Protocol.SIP));
            final SipMessage msg = sipFrame.parse();
            final StreamId id = getStreamId(msg);
            DefaultSipStream stream = this.sipStreams.get(id);
            if (stream == null) {
                stream = new DefaultSipStream(id);
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

    public void addListener(final StreamListener<SipMessage> listener) {
        this.sipListener = listener;
    }

}
