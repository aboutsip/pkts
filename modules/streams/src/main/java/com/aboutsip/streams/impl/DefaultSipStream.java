/**
 * 
 */
package com.aboutsip.streams.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.aboutsip.streams.SipStream;
import com.aboutsip.streams.StreamId;
import com.aboutsip.yajpcap.packet.sip.SipMessage;
import com.aboutsip.yajpcap.packet.sip.SipResponse;
import com.aboutsip.yajpcap.packet.sip.header.ToHeader;
import com.aboutsip.yajpcap.packet.sip.impl.SipParseException;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public final class DefaultSipStream implements SipStream {

    private boolean isTerminated = false;

    /**
     * This is the unique identifier of this particular {@link SipStream}.
     * Currently, this is the same
     */
    private final StreamId streamIdentifier;

    /**
     * All the SIP messages that we have received.
     */
    private final List<SipMessage> messages;

    /**
     * The index into the list of message where we can find the initial message.
     * Typically, this would be the first message but does not necessarily have
     * to be that (unless we start sorting the messages by arrival time)
     */
    private int indexOfInitialMessage = -1;

    /**
     * The index of where we can find the "ringing" message for INVITE dialogs.
     * I.e., the 18x response. We will only record the first response 18x
     * received.
     */
    private int indexOf18xMessage = -1;

    /**
     * The index where we can find the ACK for INVITE scenarios.
     */
    private final int indexOfAckMessage = -1;

    /**
     * The index of the terminating message which for INVITE would e.g. by a BYE
     * or an error response to the initial INVITE.
     */
    private int indexOfTerminatingMessage = -1;

    /**
     * 
     */
    public DefaultSipStream(final StreamId streamIdentifier) {
        this.streamIdentifier = streamIdentifier;
        this.messages = new ArrayList<SipMessage>();
    }

    public void addMessage(final SipMessage message) throws SipParseException {
        this.messages.add(message);

        // TOOD: if multiple pcaps are merged then there may
        // be a "new" initial message that we haven't recorded
        // yet and therefore it should "win"
        if ((this.indexOfInitialMessage == -1) && isInitialRequest(message)) {
            this.indexOfInitialMessage = this.messages.size() - 1;
        }

        if ((this.indexOf18xMessage == -1) && isRingingResponse(message)) {
            this.indexOf18xMessage = this.messages.size() - 1;
        }

        if ((this.indexOfTerminatingMessage == -1) && isTerminatingEvent(message)) {
            this.indexOfTerminatingMessage = this.messages.size() - 1;
            this.isTerminated = true;
        }
    }

    public boolean isTerminated() {
        return this.isTerminated;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<SipMessage> getPackets() {
        return this.messages.iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getPostDialDelay() throws SipParseException {
        if ((this.indexOf18xMessage != -1) && (this.indexOfInitialMessage != -1)) {
            final long start = this.messages.get(this.indexOfInitialMessage).getArrivalTime();
            final long stop = this.messages.get(this.indexOf18xMessage).getArrivalTime();
            return stop - start;
        }
        return -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getDuration() {
        // TODO: this is overly simplified and only works for INVITE
        // dialogs (and hardly that) right now
        if ((this.indexOfTerminatingMessage != -1) && (this.indexOfInitialMessage != -1)) {
            final long start = this.messages.get(this.indexOfInitialMessage).getArrivalTime();
            final long stop = this.messages.get(this.indexOfTerminatingMessage).getArrivalTime();
            return stop - start;
        }
        return -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StreamId getStreamIdentifier() {
        return this.streamIdentifier;
    }

    /**
     * Check whether the {@link SipMessage} is a 18x response.
     * 
     * @param msg
     * @return
     * @throws SipParseException
     */
    private boolean isRingingResponse(final SipMessage msg) throws SipParseException {
        return msg.isResponse() && ((((SipResponse) msg).getStatus() / 10) == 18);
    }

    /**
     * Determines whether this request goes outside of a dialog, i.e., is
     * "initial". This terminology doesn't really exist in SIP but there are
     * similar concepts in jsr289.
     * 
     * @param msg
     * @return
     */
    private boolean isInitialRequest(final SipMessage msg) throws SipParseException {
        if (msg.isRequest() && msg.isInvite()) {
            final ToHeader to = msg.getToHeader();
            return to.getTag() == null;
        }
        return false;
    }

    /**
     * Helper function that is trying to figure out if the messgage would
     * terminate any ongoing dialogs.
     * 
     * @param msg
     * @return
     */
    private boolean isTerminatingEvent(final SipMessage message) throws SipParseException {
        // TODO: WAY too simplified. Need to add error responses to initial INVITEs
        // as well as the ability to detect error responses to sub-sequent INVITE
        // requests etc.
        return message.isBye();
    }

    @Override
    public void write(final OutputStream out) throws IOException {
        for (final SipMessage msg : this.messages) {
            msg.write(out);
        }
    }

}
