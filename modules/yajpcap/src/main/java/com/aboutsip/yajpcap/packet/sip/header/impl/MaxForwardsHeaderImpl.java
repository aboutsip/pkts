/**
 * 
 */
package com.aboutsip.yajpcap.packet.sip.header.impl;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.buffer.Buffers;
import com.aboutsip.yajpcap.packet.sip.SipParseException;
import com.aboutsip.yajpcap.packet.sip.header.MaxForwardsHeader;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public class MaxForwardsHeaderImpl extends SipHeaderImpl implements MaxForwardsHeader {

    private final int maxForwards;

    public MaxForwardsHeaderImpl(final int value) {
        super(MaxForwardsHeader.NAME, null);
        this.maxForwards = value;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int getMaxForwards() {
        return this.maxForwards;
    }

    @Override
    public Buffer getValue() {
        return Buffers.wrap(this.maxForwards);
    }

    public static MaxForwardsHeader frame(final Buffer buffer) throws SipParseException {
        final int value = 5;
        return new MaxForwardsHeaderImpl(value);
    }

}
