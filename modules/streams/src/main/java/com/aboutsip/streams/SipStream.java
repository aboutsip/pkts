package com.aboutsip.streams;

import java.util.Iterator;

import com.aboutsip.yajpcap.packet.sip.SipMessage;

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
     */
    long getPostDialDelay();

}
