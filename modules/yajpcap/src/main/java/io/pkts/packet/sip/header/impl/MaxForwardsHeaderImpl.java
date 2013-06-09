/**
 * 
 */
package io.pkts.packet.sip.header.impl;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.header.MaxForwardsHeader;

import java.io.IOException;


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

    public static MaxForwardsHeader frame(final Buffer buffer) throws SipParseException {
        try {
            final int value = buffer.parseToInt();
            return new MaxForwardsHeaderImpl(value);
        } catch (final NumberFormatException e) {
            throw new SipParseException(buffer.getReaderIndex(),
                    "Unable to parse the Max-Forwards header. Value is not an integer");
        } catch (final IOException e) {
            throw new SipParseException(buffer.getReaderIndex(),
                    "Unable to parse the Max-Forwards header. Got an IOException", e);
        }
    }

    @Override
    public MaxForwardsHeader clone() {
        return new MaxForwardsHeaderImpl(this.maxForwards);
    }

    @Override
    public void setMaxForwards(final int value) {
        this.maxForwards = value;
    }

}
