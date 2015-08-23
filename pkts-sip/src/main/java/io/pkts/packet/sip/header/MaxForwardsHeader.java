/**
 * 
 */
package io.pkts.packet.sip.header;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.header.impl.MaxForwardsHeaderImpl;

import java.io.IOException;

import static io.pkts.packet.sip.impl.PreConditions.assertArgument;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public interface MaxForwardsHeader extends SipHeader {

    Buffer NAME = Buffers.wrap("Max-Forwards");

    int getMaxForwards();

    static MaxForwardsHeader frame(final Buffer buffer) throws SipParseException {
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

    @Override
    Builder copy();

    class Builder implements SipHeader.Builder<MaxForwardsHeader> {

        private int value;

        public Builder() {
            this(70);
        }

        public Builder(final int value) {
            this.value = value;
        }

        public Builder withValue(final int value) {
            this.value = value;
            return this;
        }

        public Builder decrement() {
            --this.value;
            return this;
        }

        @Override
        public MaxForwardsHeader build() throws SipParseException {
            assertArgument(this.value >= 0, "The value must be greater or equal to zero");
            return new MaxForwardsHeaderImpl(this.value);
        }
    }

}
