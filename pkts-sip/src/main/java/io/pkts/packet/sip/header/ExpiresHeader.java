package io.pkts.packet.sip.header;

import static io.pkts.packet.sip.impl.PreConditions.assertArgument;
import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.header.impl.ExpiresHeaderImpl;

import java.io.IOException;

public interface ExpiresHeader extends SipHeader {

    Buffer NAME = Buffers.wrap("Expires");

    int getExpires();

    void setExpires(int expires);

    @Override
    ExpiresHeader clone();

    static ExpiresHeader create(final int expires) {
        assertArgument(expires >= 0, "The value must be greater or equal to zero");
        return new ExpiresHeaderImpl(expires);
    }

    public static ExpiresHeader frame(final Buffer buffer) throws SipParseException {
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

}
