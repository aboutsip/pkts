/**
 * 
 */
package com.aboutsip.streams.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.aboutsip.streams.StreamListener;
import com.aboutsip.yajpcap.frame.Frame;
import com.aboutsip.yajpcap.frame.SipFrame;
import com.aboutsip.yajpcap.packet.PacketParseException;
import com.aboutsip.yajpcap.packet.sip.SipMessage;
import com.aboutsip.yajpcap.protocol.Protocol;

/**
 * @author jonas@jonasborjesson.com
 */
public class SipStreamHandler {

    private final Map<String, DefaultSipStream> sipStreams = new HashMap<String, DefaultSipStream>();

    private StreamListener<SipMessage> sipListener;

    /**
     * 
     */
    public SipStreamHandler() {
        // TODO Auto-generated constructor stub
    }

    public void processFrame(final Frame frame) throws PacketParseException {
        try {
            final SipFrame sipFrame = ((SipFrame) frame.getFrame(Protocol.SIP));
            final SipMessage msg = sipFrame.parse();
            final String callId = msg.getCallIDHeader().getValue().toString();
            DefaultSipStream stream = this.sipStreams.get(callId);
            if (stream == null) {
                this.sipListener.startStream(stream);
                stream = new DefaultSipStream(msg);
                this.sipStreams.put(callId, stream);
            }
            stream.addMessage(msg);
            this.sipListener.packetReceived(stream, msg);
            if (stream.isTerminated()) {
                this.sipListener.endStream(stream);
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
