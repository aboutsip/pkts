/**
 * 
 */
package com.aboutsip.streams.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import com.aboutsip.streams.SipStream;
import com.aboutsip.streams.StreamId;
import com.aboutsip.yajpcap.PcapOutputStream;
import com.aboutsip.yajpcap.frame.PcapGlobalHeader;
import com.aboutsip.yajpcap.packet.sip.SipMessage;
import com.aboutsip.yajpcap.packet.sip.SipParseException;

/**
 * The {@link BasicSipStream} only does some very basic analysis of the SIP
 * messages in order to determine which dialog the message belongs to. It is
 * faster and consumes less memory than the {@link DefaultSipStream} but is not
 * capable of detecting so-called derived dialogs since it (falsely) assumes it
 * can use the call-id as a unique key for each dialog.
 * 
 * Also, if multiple pcaps are merged it will not always handle the case where
 * e.g. the initial message indicating the start of the stream shows up after we
 * have already seen other messages in the stream (could happen if you merge two
 * pcaps where the first pcap contains messages that arrived later time - hence,
 * you didn't merge the pcaps in cronological order). If you need this accuracy,
 * then you should be using the {@link DefaultSipStream} instead.
 * 
 * @author jonas@jonasborjesson.com
 */
public class BasicSipStream implements SipStream {

    /**
     * This is the unique identifier of this particular {@link SipStream}.
     * Currently, this is the same
     */
    private final StreamId streamIdentifier;

    private final SimpleCallStateMachine fsm;

    private final PcapGlobalHeader globalHeader;

    /**
     * 
     */
    public BasicSipStream(final PcapGlobalHeader globalHeader, final StreamId streamIdentifier) {
        this.globalHeader = globalHeader;
        this.streamIdentifier = streamIdentifier;
        this.fsm = new SimpleCallStateMachine(this.streamIdentifier.asString());
    }

    public void addMessage(final SipMessage message) throws SipParseException {
        this.fsm.onEvent(message);
    }

    public boolean isTerminated() {
        return this.fsm.isTerminated();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<SipMessage> getPackets() {
        return this.fsm.getMessages();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getPostDialDelay() throws SipParseException {
        return this.fsm.getPostDialDelay();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getDuration() {
        return this.fsm.getDuration();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StreamId getStreamIdentifier() {
        return this.streamIdentifier;
    }

    @Override
    public void write(final OutputStream out) throws IOException {
        final Iterator<SipMessage> it = this.fsm.getMessages();
        while (it.hasNext()) {
            it.next().write(out);
        }
    }

    @Override
    public CallState getCallState() {
        return this.fsm.getCallState();
    }

    @Override
    public boolean handshakeComplete() {
        return this.fsm.isHandshakeCompleted();
    }

    @Override
    public boolean reTranmitsDetected() {
        return this.fsm.reTransmitsDetected();
    }

    @Override
    public void save(final String filename) throws IOException {
        final File file = new File(filename);
        final FileOutputStream os = new FileOutputStream(file);
        final PcapOutputStream out = PcapOutputStream.create(this.globalHeader, os);
        try {
            this.write(out);
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }
}
