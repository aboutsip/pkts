package io.pkts.streams;

import io.pkts.frame.PcapGlobalHeader;
import io.pkts.packet.sip.SipPacket;
import io.pkts.packet.sip.SipParseException;
import io.pkts.sdp.SDP;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Represents a stream of related SIP messages.
 * 
 * @author jonas@jonasborjesson.com
 */
public interface SipStream extends Stream<SipPacket> {

    /**
     * Get all the {@link SipPacket}s that belongs to this {@link Stream}.
     * 
     * {@inheritDoc}
     */
    @Override
    List<SipPacket> getPackets();

    /**
     * Check whether this {@link SipStream} is in the terminated state, which it is if any of the following is true:
     * For INVITE streams:
     * <ul>
     *     <li>If the initial handshake failed with an error 
     *         response (which will include CANCEL scenarios)</li>
     *     <li>If the call was successfully established and a BYE 
     *         request and the corresponding final response has been processed</li>
     * </ul>
     * @return
     */
    boolean isTerminated();

    /**
     * Post Dial Delay (PDD) is defined as the time it takes between the INVITE
     * and until some sort of ringing signal is received (18x responses).
     * 
     * PDD only applies to INVITE scenarios.
     * 
     * @return the PDD in milliseconds or -1 (negative one) in case it hasn't
     *         been calculated yet or because this {@link SipStream} is not an
     *         INVITE scenario.
     * @throws SipParseException
     *             in case anything goes wrong while trying to calculate the
     *             PDD.
     */
    long getPostDialDelay() throws SipParseException;

    /**
     * Convenience method for returning the {@link SDP} found on the first INVITE request. Note, if
     * you want to find the SDP for a re-invite, then you will have to {@link #getPackets()} and
     * find the particular INVITE you are looking for.
     * 
     * @return the SDP found in the first INVITE request or null if no INVITE was found in the
     *         stream or if that INVITE simply didn't contain an SDP
     * @throws SipParseException
     */
    SDP getInviteSDP() throws SipParseException;

    /**
     * Convenience method for returning the {@link SDP} found on the 200 OK to the first INVITE
     * request.
     * 
     * Note, if you want to find the SDP for 200 OK to a re-invite, then you will have to
     * {@link #getPackets()} and find the particular response you are looking for.
     * 
     * @return the SDP found in the first INVITE request or null if no INVITE was found in the
     *         stream or if that INVITE simply didn't contain an SDP
     * @throws SipParseException
     */
    SDP get200OkSDP() throws SipParseException;

    /**
     * Get the identifier used for grouping the {@link SipPacket}s together. Currently, this is the
     * same as the call-id.
     * 
     * Note, perhaps this should be a dialog id instead since ideally that is what we should be
     * using for grouping together {@link SipStream}s. On the other hand, using the dialog-id as an
     * identifier can make things messy for forked dialogs etc. This works and keeps things simple
     * so we will stick with it for now.
     * 
     * @return
     */
    @Override
    StreamId getStreamIdentifier();

    /**
     * The duration for a {@link SipStream} is calculated differently depending
     * on what the stream is capturing. For dialogs (INVITE, SUBSCRIBE, REFER)
     * the duration is equal to the duration of the dialog. As such, any
     * re-transmissions of the terminating event etc will NOT be taken into
     * consideration when calculating the duration. However, if the underlying
     * sip stream is not a dialog, then the duration will simply be the time
     * between the first and last message that we recorded. {@inheritDoc}
     */
    @Override
    long getDuration();

    /**
     * Which {@link CallState} the call is in.
     * 
     * NOTE: this only applies to INVITE dialogs.
     * 
     * @return the current {@link CallState}.
     */
    CallState getCallState();

    /**
     * Indicates whether the INVITE handshake was completed. I.e., did we see
     * the ACK to the final response to the INVITE. Note, this does not mean
     * that the call was successfully setup, after all, the INVITE setup can be
     * error out for various reasons.
     * 
     * @return
     */
    boolean handshakeComplete();

    /**
     * Indicates whether there were retransmissions detected. If it was the
     * INVITE that was re-transmitted, or some other request is not conveyed
     * through this method. It just indicates that there were retransmissions in
     * this flow.
     * 
     * @return
     */
    boolean reTranmitsDetected();

    /**
     * Save this {@link SipStream} to the specified file.
     * 
     * @param filename
     */
    void save(String filename) throws FileNotFoundException, IOException;

    /**
     * Save this {@link SipStream} to the specified {@link OutputStream}. The
     * difference between this method and {@link #write(OutputStream)} is that
     * the latter assumes that the pcap headers etc already have been written to
     * the stream. However, this method will write this {@link SipStream} as a
     * standalone pcap.
     * 
     * @param out
     * @throws IOException
     */
    void save(OutputStream out) throws IOException;

    /**
     * Create an empty clone of this {@link SipStream}. What this means is that
     * you get a {@link SipStream} with the same {@link StreamId} and underlying
     * {@link PcapGlobalHeader} (which you really do not need to know) but
     * otherwise it is empty. I.e., it doesn't contain any {@link SipPacket}s.
     * 
     * Use this method when you e.g. have a {@link SipStream} that you want to
     * split in two. A typical scenario is if you have a {@link SipStream} that
     * went through a SIP Proxy but you want to split this stream in one "left"
     * side and one "right" side. You do so by figuring out which message
     * belongs to each side and then create two empty clones and then drive the
     * traffic from each side through the new {@link SipStream}s. This will then
     * give you two separate {@link SipStream} but that still will have the
     * stats available.
     * 
     * @return
     */
    SipStream createEmptyClone();

    /**
     * Add a {@link SipPacket} to this {@link SipStream}. By doing so you will
     * force the {@link SipStream} to move its internal state machine along
     * since it just "received" a new {@link SipPacket}.
     * 
     * @param message
     * @throws IllegalArgumentException
     *             in case the message you are trying to add does not have the
     *             same {@link StreamId}.
     * @throws SipParseException
     *             in case something goes wrong while parsing the
     *             {@link SipPacket}
     */
    void addMessage(SipPacket message) throws IllegalArgumentException, SipParseException;

    /**
     * Even though SIP can be used for so much more than just establishing
     * "phone calls" (VoIP) it is commonly used for this very purpose. As such,
     * it seems useful to provide some kind of "call state" from a phone call
     * perspective in the same way that wireshark does. Hence, this enum is used
     * for exactly that and only has a meaning for INVITE dialogs.
     * 
     * Note, RFC 4235 (an invite-initiated dialog event package) describes a
     * similar state machine and this one here is similar but slightly different
     * since we want to make it a little more high-level ala wireshark. Perhaps
     * the wrong decision? Comments???
     */
    enum CallState {
        /**
         * Just to have an initial state when everything is created (and since I
         * decided to call the first "real" state for INITIAL I couldn't use
         * INIT or similar since it would be confusing)
         */
        START,

        /**
         * We have only seen an INVITE request and nothing else. Hence, the call
         * state is currently in the initial phase.
         */
        INITIAL,

        /**
         * If we receive a 100 Trying response, the call-state will transition
         * over to the trying state.
         */
        TRYING,

        /**
         * If we receive a 180/183, the state will progress to the ringing
         * state.
         */
        RINGING,

        /**
         * If we receive a CANCEL request then we move over to the canceling
         * state. However, we may actually move back to in call because as
         * always there is a race condition around canceling.
         */
        CANCELLING,

        /**
         * The INVITE request was redirected (3xx responses)
         */
        REDIRECT,

        /**
         * If a 2xx response is generated for the INVITE request, then the
         * call-state progresses to the "in call" state. Note, technically we
         * must really wait for the ACK but there are a lof of real-world
         * clients that will start sending media (RTP) as soon as the 2xx is
         * sent and from their perspective the call has been established.
         * 
         * However, if the 3-way handshake fails the state will progress over to
         * {@link CallState#FAILED}. Hence, if you examine the states the call
         * has been and you see a transition from {@link CallState#IN_CALL} to
         * {@link CallState#FAILED} then you can be 100% sure that there is some
         * kind of NAT/FW issue going on. Typically, either the UA:s stamped the
         * wrong information in the Contact-header or there is a firewall
         * closing a little too fast.
         */
        IN_CALL,

        /**
         * If the call was cancelled you will end up in this state.
         */
        CANCELLED,

        /**
         * Completed always means that the call was first successfully
         * established and then completed at some point. Note, there are a lot
         * of corner cases where e.g. the BYE wouldn't make it through etc but
         * this is still marked as completed.
         */
        COMPLETED,

        /**
         * The INVITE request was rejected (4xx and 6xx responses) for some
         * reason.
         */
        REJECTED,

        /**
         * The call failed.
         * 
         * The most typical scenario is receiving 5xx responses but if we e.g.
         * get a 408, 481 we will also mark the call as failed. Only if we get
         * these on the initial INVITE though.
         * 
         * 
         * Also, this can happen during the initial 3-way handshake where the
         * 200 OK doesn't make it back to the UAC or the UAS doesn't get the ACK
         * so both of them keeps re-transmitting (depending where the error is
         * of course. Typically, the 200 OK will make it through but the ACK
         * will no so you will see a lot of re-transmission of the 200 as well
         * as the ACK but the ACK will not reach the UAS)
         */
        FAILED,

        /**
         * Sorry, couldn't figure it out! Examine the call flow and let me know
         * which state you believe the call should be in.
         */
        UNKNOWN;
    }

}
