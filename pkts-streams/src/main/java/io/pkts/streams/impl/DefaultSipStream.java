/**
 * 
 */
package io.pkts.streams.impl;

import io.pkts.packet.sip.SipMessage;
import io.pkts.packet.sip.SipParseException;
import io.pkts.streams.SipStream;
import io.pkts.streams.StreamId;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

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
    public Collection<SipMessage> getPackets() {
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

    @Override
    public long getTimeOfFirstPacket() {
        return -1;
    }

    @Override
    public long getTimeOfLastPacket() {
        return -1;
    }

    @Override
    public void save(final OutputStream out) throws IOException {
        // TODO Auto-generated method stub
    }

    @Override
    public SipStream createEmptyClone() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addMessage(final SipMessage message) throws IllegalArgumentException, SipParseException {
        // TODO Auto-generated method stub

    }

}
