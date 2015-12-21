/**
 * 
 */
package io.pkts.packet.sip.header.impl;

import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.header.MaxForwardsHeader;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public class MaxForwardsHeaderImpl extends SipHeaderImpl implements MaxForwardsHeader {

    private final int maxForwards;

    public MaxForwardsHeaderImpl(final int value) {
        super(MaxForwardsHeader.NAME, Buffers.wrap(value));
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
    public MaxForwardsHeader clone() {
        return new MaxForwardsHeaderImpl(this.maxForwards);
    }

    @Override
    public MaxForwardsHeader.Builder copy() {
        return new MaxForwardsHeader.Builder(this.maxForwards);
    }

    @Override
    public MaxForwardsHeader ensure() {
        return this;
    }

}
