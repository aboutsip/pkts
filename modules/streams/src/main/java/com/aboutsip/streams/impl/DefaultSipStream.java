/**
 * 
 */
package com.aboutsip.streams.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.aboutsip.streams.SipStream;
import com.aboutsip.yajpcap.packet.sip.SipMessage;
import com.aboutsip.yajpcap.packet.sip.SipResponse;
import com.aboutsip.yajpcap.packet.sip.impl.SipParseException;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public final class DefaultSipStream implements SipStream {

    private boolean isTerminated = false;

    /**
     * All the SIP messages that we have received.
     */
    private final List<SipMessage> messages;

    /**
     * 
     */
    public DefaultSipStream(final SipMessage initialMessage) {
        this.messages = new ArrayList<SipMessage>();
    }

    public void addMessage(final SipMessage message) throws SipParseException {
        this.messages.add(message);

        // TODO: WAY too simplified
        if (message.isResponse() && message.isBye() && ((((SipResponse) message).getStatus() / 100) == 2)) {
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
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getPostDialDelay() {
        // TODO Auto-generated method stub
        return 0;
    }

}
