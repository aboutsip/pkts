/**
 * 
 */
package io.pkts.packet.sip.header;

import static io.pkts.packet.sip.impl.PreConditions.assertArgument;
import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.header.impl.MaxForwardsHeaderImpl;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public interface MaxForwardsHeader extends SipHeader {

    Buffer NAME = Buffers.wrap("Max-Forwards");

    int getMaxForwards();

    void setMaxForwards(int value);

    /**
     * Decrement the value by one. Note, there is no check whether or not the
     * value goes to zero or even below it.
     */
    void decrement();

    @Override
    MaxForwardsHeader clone();

    static MaxForwardsHeader create(final int max) {
        assertArgument(max >= 0, "The value must be greater or equal to zero");
        return new MaxForwardsHeaderImpl(max);
    }

    /**
     * Create a new {@link MaxForwardsHeader} with a value of 70.
     * 
     * @return
     */
    static MaxForwardsHeader create() {
        return new MaxForwardsHeaderImpl(70);
    }

}
