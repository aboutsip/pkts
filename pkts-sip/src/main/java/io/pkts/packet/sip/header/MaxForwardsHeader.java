/**
 * 
 */
package io.pkts.packet.sip.header;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;

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

}
