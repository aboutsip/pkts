package com.aboutsip.streams;

import java.util.Iterator;

import com.aboutsip.yajpcap.packet.sip.SipMessage;
import com.aboutsip.yajpcap.packet.sip.impl.SipParseException;

/**
 * Represents a stream of related SIP messages.
 * 
 * @author jonas@jonasborjesson.com
 */
public interface SipStream extends Stream<SipMessage> {

    /**
     * Get all the {@link SipMessage}s that belongs to this {@link Stream}.
     * 
     * {@inheritDoc}
     */
    @Override
    Iterator<SipMessage> getPackets();

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
     * Get the identifier used for grouping the {@link SipMessage}s together.
     * Currently, this is the same as the call-id.
     * 
     * Note, perhaps this should be a dialog id instead since ideally that is
     * what we should be using for grouping together {@link SipStream}s. On the
     * other hand, using the dialog-id as an identifier can make things messy
     * for forked dialogs etc. This works and keeps things simple so we will
     * stick with it for now.
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

}
