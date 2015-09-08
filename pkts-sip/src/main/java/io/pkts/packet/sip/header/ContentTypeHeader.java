/**
 * 
 */
package io.pkts.packet.sip.header;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.header.impl.ContentTypeHeaderImpl;

import static io.pkts.packet.sip.impl.PreConditions.assertNotEmpty;

/**
 * Represents the a content type header.
 * 
 * @author jonas@jonasborjesson.com
 */
public interface ContentTypeHeader extends SipHeader, MediaTypeHeader, Parameters {

    Buffer NAME = Buffers.wrap("Content-Type");

    Buffer COMPACT_NAME = Buffers.wrap("c");

    @Override
    ContentTypeHeader clone();

    @Override
    Builder copy();

    @Override
    default boolean isContentTypeHeader() {
        return true;
    }

    @Override
    default ContentTypeHeader toContentTypeHeader() {
        return this;
    }

    /**
     * Frame the value as a {@link ContentTypeHeader}. This method assumes that you have already
     * parsed out the actual header name "Content-Type: ". Also, this method assumes that a message
     * framer (or similar) has framed the buffer that is being passed in to us to only contain this
     * header and nothing else.
     * 
     * Note, as with all the frame-methods on all headers/messages/whatever, they do not do any
     * validation that the information is actually correct. This method will simply only try and
     * validate just enough to get the framing done.
     * 
     * @param value
     * @return
     * @throws SipParseException in case anything goes wrong while parsing.
     */
    static ContentTypeHeader frame(final Buffer buffer) throws SipParseException {
        assertNotEmpty(buffer, "The supplied buffer cannot be null or empty");
        final Buffer original = buffer.slice();
        final Buffer[] mediaType = MediaTypeHeader.frame(buffer);
        return new ContentTypeHeaderImpl(original, mediaType[0], mediaType[1], buffer);
    }

    static Builder withType(final Buffer type) {
        final Builder builder = new Builder();
        builder.withType(type);
        return builder;
    }

    static Builder withType(final String type) {
        return withType(Buffers.wrap(type));
    }

    static Builder withParams(final Buffer params) {
        return new Builder(params);
    }

    class Builder extends MediaTypeHeader.Builder<ContentTypeHeader> {

        protected Builder() {
            super(NAME);
        }

        protected Builder(final Buffer params) {
            super(NAME, params);
        }

        @Override
        protected ContentTypeHeader internalBuild(Buffer rawValue, Buffer type, Buffer subType, Buffer params) {
            return new ContentTypeHeaderImpl(rawValue, type, subType, params);
        }

        @Override
        public SipHeader.Builder<ContentTypeHeader> withValue(Buffer value) {
            // TODO: implement me...
            throw new RuntimeException("TODO: not implemented yet");
        }
    }

}
