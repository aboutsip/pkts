package io.pkts.packet.sip.header;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.header.impl.ContentLengthHeaderImpl;
import io.pkts.packet.sip.impl.SipParser;

import java.io.IOException;

import static io.pkts.packet.sip.impl.PreConditions.assertArgument;

public interface ContentLengthHeader extends SipHeader {

    Buffer NAME = Buffers.wrap("Content-Length");

    Buffer COMPACT_NAME = Buffers.wrap("l");

    int getContentLength();

    @Override
    ContentLengthHeader clone();

    static ContentLengthHeader create(final int contentLength) {
        assertArgument(contentLength >= 0, "The value must be greater or equal to zero");
        return new ContentLengthHeaderImpl(contentLength);
    }

    static ContentLengthHeader frame(final Buffer buffer) throws SipParseException {
        try {
            SipParser.consumeWS(buffer);
            final int value = buffer.parseToInt();
            return new ContentLengthHeaderImpl(value);
        } catch (final NumberFormatException e) {
            throw new SipParseException(buffer.getReaderIndex(),
                    "Unable to parse the Content-Length header. Value is not an integer");
        } catch (final IOException e) {
            throw new SipParseException(buffer.getReaderIndex(),
                    "Unable to parse the Expires header. Got an IOException", e);
        }
    }

    @Override
    Builder copy();

    @Override
    default boolean isContentLengthHeader() {
        return true;
    }

    @Override
    default ContentLengthHeader toContentLengthHeader() {
        return this;
    }

    class Builder implements SipHeader.Builder<ContentLengthHeader> {

        private int value;

        public Builder() {
        }

        public Builder(final int value) {
            this.value = value;
        }

        public Builder withValue(final int value) {
            this.value = value;
            return this;
        }

        @Override
        public SipHeader.Builder<ContentLengthHeader> withValue(Buffer value) {
            // TODO: implement me...
            throw new RuntimeException("TODO: not implemented yet");
        }

        @Override
        public ContentLengthHeader build() throws SipParseException {
            assertArgument(this.value >= 0, "The value must be greater or equal to zero");
            return new ContentLengthHeaderImpl(this.value);
        }
    }

}
