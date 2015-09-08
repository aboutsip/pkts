package io.pkts.packet.sip.header;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.header.impl.ExpiresHeaderImpl;

import java.io.IOException;

import static io.pkts.packet.sip.impl.PreConditions.assertArgument;

public interface ExpiresHeader extends SipHeader {

    Buffer NAME = Buffers.wrap("Expires");

    int getExpires();

    @Override
    ExpiresHeader clone();

    static ExpiresHeader create(final int expires) {
        assertArgument(expires >= 0, "The value must be greater or equal to zero");
        return new ExpiresHeaderImpl(expires);
    }

    static ExpiresHeader frame(final Buffer buffer) throws SipParseException {
        try {
            final int value = buffer.parseToInt();
            return new ExpiresHeaderImpl(value);
        } catch (final NumberFormatException e) {
            throw new SipParseException(buffer.getReaderIndex(),
                    "Unable to parse the Expires header. Value is not an integer");
        } catch (final IOException e) {
            throw new SipParseException(buffer.getReaderIndex(),
                    "Unable to parse the Expires header. Got an IOException", e);
        }
    }

    @Override
    Builder copy();

    @Override
    default boolean isExpiresHeader() {
        return true;
    }

    @Override
    default ExpiresHeader toExpiresHeader() {
        return this;
    }

    class Builder implements SipHeader.Builder<ExpiresHeader> {

        private int value;

        public Builder() {
            this(600);
        }

        public Builder(final int value) {
            this.value = value;
        }

        public Builder withValue(final int value) {
            this.value = value;
            return this;
        }

        @Override
        public Builder withValue(Buffer value) {
            throw new RuntimeException("Not implemented yet");
        }

        @Override
        public ExpiresHeader build() throws SipParseException {
            assertArgument(this.value >= 0, "The value must be greater or equal to zero");
            return new ExpiresHeaderImpl(this.value);
        }
    }

}
