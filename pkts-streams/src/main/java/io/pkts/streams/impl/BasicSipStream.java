/**
 * 
 */
package io.pkts.streams.impl;

import io.pkts.PcapOutputStream;
import io.pkts.frame.PcapGlobalHeader;
import io.pkts.packet.sip.SipPacket;
import io.pkts.packet.sip.SipParseException;
import io.pkts.sdp.SDP;
import io.pkts.streams.SipStream;
import io.pkts.streams.StreamId;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

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

    @Override
    public void addMessage(final SipPacket message) throws SipParseException {
        this.fsm.onEvent(message);
    }

    public boolean isTerminated() {
        return this.fsm.isTerminated();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SipPacket> getPackets() {
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
        for (final SipPacket msg : this.fsm.getMessages()) {
            msg.write(out);
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

    @Override
    public void save(final OutputStream out) throws IOException {
        this.globalHeader.write(out);
        this.write(out);
    }

    @Override
    public long getTimeOfFirstPacket() {
        return this.fsm.getTimeOfFirstMessage();
    }

    @Override
    public long getTimeOfLastPacket() {
        return this.fsm.getTimeOfLastMessage();
    }

    @Override
    public SipStream createEmptyClone() {
        return new BasicSipStream(this.globalHeader, this.streamIdentifier);
    }

    @Override
    public SDP getInviteSDP() throws SipParseException {
        for (final SipPacket msg : this.fsm.getMessages()) {
            if (msg.isRequest() && msg.isInvite()) {
                return getSDPorNull(msg);
            }
        }
        return null;
    }

    @Override
    public SDP get200OkSDP() throws SipParseException {
        for (final SipPacket msg : this.fsm.getMessages()) {
            if (msg.isResponse() && msg.isInvite() && msg.toResponse().isSuccess()) {
                return getSDPorNull(msg);
            }
        }
        return null;
    }

    private SDP getSDPorNull(final SipPacket msg) throws SipParseException {
        final Object content = msg.getContent();
        if (content instanceof SDP) {
            return (SDP) content;
        }
        return null;
    }

}
