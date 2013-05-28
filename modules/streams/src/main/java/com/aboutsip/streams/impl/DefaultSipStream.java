/**
 * 
 */
package com.aboutsip.streams.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import com.aboutsip.streams.SipStream;
import com.aboutsip.streams.StreamId;
import com.aboutsip.yajpcap.packet.sip.SipMessage;
import com.aboutsip.yajpcap.packet.sip.SipParseException;

/**
 * The {@link DefaultSipStream} implements a complete SIP state machine in order
 * to figure out what SIP messages belongs to a particular SIP dialog etc.
 * Compared to the {@link BasicSipStream}, the {@link DefaultSipStream} does a
 * much better and more accurate job of detecting a {@link SipStream} but will
 * waste more memory in doing so and will overall be slower.
 * 
 * @author jonas@jonasborjesson.com
 * 
 */
public final class DefaultSipStream implements SipStream {

    /**
     * 
     */
    public DefaultSipStream(final StreamId streamIdentifier) {
    }

    @Override
    public void write(final OutputStream out) throws IOException {
        // TODO Auto-generated method stub
    }

    @Override
    public Iterator<SipMessage> getPackets() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getPostDialDelay() throws SipParseException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public StreamId getStreamIdentifier() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getDuration() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public CallState getCallState() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean handshakeComplete() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean reTranmitsDetected() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void save(final String filename) {
        throw new RuntimeException("sorry, not implemeneted yet");
    }

}
