/**
 * 
 */
package io.pkts.packet.sip.header.impl;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.header.MaxForwardsHeader;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public class MaxForwardsHeaderImpl extends SipHeaderImpl implements MaxForwardsHeader {

    private int maxForwards;

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

    @Override
    public MaxForwardsHeader clone() {
        return new MaxForwardsHeaderImpl(this.maxForwards);
    }

    @Override
    public void setMaxForwards(final int value) {
        this.maxForwards = value;
    }

    @Override
    public void decrement() {
        --this.maxForwards;
    }

    @Override
    public MaxForwardsHeader ensure() {
        return this;
    }

}
