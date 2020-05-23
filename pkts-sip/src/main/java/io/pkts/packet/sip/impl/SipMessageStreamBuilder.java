package io.pkts.packet.sip.impl;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipMessage;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.header.ContentLengthHeader;
import io.pkts.packet.sip.header.SipHeader;
import io.pkts.packet.sip.header.impl.SipHeaderImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;





/**
 * A very specialized SIP message builder for streams and is also highly specific
 * to how ByteBuf's within Netty works.
 *
 * @author jonas@jonasborjesson.com
 */
public class SipMessageStreamBuilder {

    private final Configuration config;

    private State state = State.INIT;

    /**
     * Because the beginning of the buffer could contain stuff we don't care
     * about so we have to mask that away.
     */
    private int start;

    private SipInitialLine sipInitialLine;
    private Buffer buffer;

    private Buffer payload = Buffers.EMPTY_BUFFER;

    // Move along as long as we actually can consume an header and
    private Buffer headerName = null;
    private Map<String, List<SipHeader>> headers = new HashMap<>();
    private int contentLength;

    private SipHeader toHeader;
    private SipHeader fromHeader;
    private SipHeader cSeqHeader;
    private SipHeader callIdHeader;
    private SipHeader maxForwardsHeader;
    private SipHeader viaHeader;
    private SipHeader routeHeader;
    private SipHeader recordRouteHeader;
    private SipHeader contactHeader;

    private final SipParser.HeaderValueState headerValueState;

    private final Function<Buffer, State>[] actions = new Function[State.values().length];

    public SipMessageStreamBuilder(final Configuration config) {
        this.config = config;

        resetBuffer();
        reset();
        headerValueState = new SipParser.HeaderValueState(0);

        actions[State.INIT.ordinal()] = this::onInit;
        actions[State.GET_INITIAL_LINE.ordinal()] = this::onInitialLine;
        actions[State.GET_HEADER_NAME.ordinal()] = this::onHeaderName;
        actions[State.CONSUME_HCOLON.ordinal()] = this::onConsumeHColon;
        actions[State.CONSUME_SWS_AFTER_HCOLON.ordinal()] = this::onConsumeSWSAfterHColon;
        actions[State.GET_HEADER_VALUES.ordinal()] = this::onHeaderValues;
        actions[State.CHECK_FOR_END_OF_HEADER_SECTION.ordinal()] = this::onCheckEndHeaderSection;
        actions[State.GET_PAYLOAD.ordinal()] = this::onPayload;
        actions[State.DONE.ordinal()] = this::onDone;
    }

    private void resetBuffer() {
        buffer = Buffers.createBuffer(config.getMaxAllowedInitialLineSize()
                                      + config.getMaxAllowedHeadersSize()
                                      + config.getMaxAllowedContentLength());
    }

    private void reset() {
        state = State.INIT;
        payload = Buffers.EMPTY_BUFFER;
        sipInitialLine = null;
        start = 0;

        headerName = null;
        headers = new HashMap<>();
        contentLength = 0;

        toHeader = null;
        fromHeader = null;
        cSeqHeader = null;
        callIdHeader = null;
        maxForwardsHeader = null;
        viaHeader = null;
        routeHeader = null;
        recordRouteHeader = null;
        contactHeader = null;
    }

    public SipMessage build() throws IllegalStateException {
        if (state != State.DONE) {
            throw new IllegalStateException("We have not framed enough data for a complete message yet");
        }

        // final byte[] array = buffer.getRawArray();
        // final byte[] msgArray = new byte[buffer.getReaderIndex() - start];
        // System.arraycopy(array, start, msgArray, 0, msgArray.length);
        // final Buffer msg = Buffers.wrap(msgArray);

        // There may be extra data left after we have processed
        // the one SIP message. If there is, we want to preserve that
        // data by copying it over to the beginning of the next buffer
        // so mark the start and stop, hide that portion to the old
        // buffer and then copy it over to the new
        final int extraDataStart = buffer.getReaderIndex();
        final int length = buffer.getWriterIndex() - extraDataStart;

        // hide in old. Remember that the 'start' value is
        // were the data of this message actually starts as in
        // there may have been empty spaces and what not in the beginning
        // of the data and that has to be masked away, which is done by
        // manipulating the reader index.
        final Buffer msg = buffer;
        msg.setWriterIndex(msg.getReaderIndex());
        msg.setReaderIndex(start);

        // reset and copy over
        resetBuffer();
        final byte[] oldData = msg.getRawArray();
        final byte[] newData = buffer.getRawArray();
        System.arraycopy(oldData, extraDataStart, newData, 0, length);
        buffer.setWriterIndex(length);

        SipMessage sipMessage = null;
        if (sipInitialLine.isRequestLine()) {
            sipMessage = new ImmutableSipRequest(msg,
                                                 sipInitialLine.toRequestLine(),
                                                 headers,
                                                 toHeader,
                                                 fromHeader,
                                                 cSeqHeader,
                                                 callIdHeader,
                                                 maxForwardsHeader,
                                                 viaHeader,
                                                 routeHeader,
                                                 recordRouteHeader,
                                                 contactHeader,
                                                 payload);
        } else {
            sipMessage = new ImmutableSipResponse(msg,
                                                  sipInitialLine.toResponseLine(),
                                                  headers,
                                                  toHeader,
                                                  fromHeader,
                                                  cSeqHeader,
                                                  callIdHeader,
                                                  maxForwardsHeader,
                                                  viaHeader,
                                                  routeHeader,
                                                  recordRouteHeader,
                                                  contactHeader,
                                                  payload);
        }

        reset();
        return sipMessage;
    }

    public int getWriterIndex() {
        return buffer.getWriterIndex();
    }

    public int getWritableBytes() {
        return buffer.getWritableBytes();
    }

    /**
     * After you have actually constructed a new {@link SipMessage} there may
     * be other messages behind it in the same stream, or parts of one. Therefore,
     * after you have {@link #build()} a message you can check if there are still
     * data available and if so, call {@link #process()} to kick off more
     * processing.
     *
     * @return if there are more data available for processing
     */
    public boolean hasUnprocessData() {
        return buffer.hasReadableBytes();
    }

    /**
     *
     * @return true if a message is ready to be built.
     */
    public boolean process() {
        return process(null);
    }

    /**
     * Process more incoming data.
     *
     * @param newData
     * @return
     */
    public boolean process(final byte[] newData) {
        if (newData != null) {
            buffer.write(newData);
        }

        boolean done = false;
        while (!done) {
            final int index = buffer.getReaderIndex();
            final State currentState = state;
            state = actions[state.ordinal()].apply(buffer);
            done = state == currentState && buffer.getReaderIndex() == index;
        }

        return state == State.DONE;
    }

    public boolean isDone() {
        return state == State.DONE;
    }

    /**
     * Just so we don't have to check for null once we reach this state.
     *
     * @param buffer
     * @return
     */
    private final State onDone(final Buffer buffer) {
        return State.DONE;
    }

    /**
     * While in the INIT state, we are just consuming any empty space
     * before heading off to start parsing the initial line
     * @param b
     * @return
     */
    private final State onInit(final Buffer buffer) {
        try {
            while (buffer.hasReadableBytes()) {
                final byte b = buffer.peekByte();
                if (b == SipParser.SP || b == SipParser.HTAB || b == SipParser.CR || b == SipParser.LF) {
                    buffer.readByte();
                } else {
                    start = buffer.getReaderIndex();
                    return State.GET_INITIAL_LINE;
                }
            }
        } catch (final IOException e) {
            throw new RuntimeException("Unable to read from stream due to IOException", e);
        }

        return State.INIT;
    }

    /**
     * Since it is quite uncommon to not have enough data on the line
     * to read the entire first line we are taking the simple approach
     * of just resetting the entire effort and we'll retry later. This
     * of course means that in the worst case scenario we will actually
     * iterate over data we have already seen before. However, seemed
     * like it is worth it instead of having the extra space for keeping
     * track of the extra state.
     *
     * @param buffer
     * @return
     */
    private final State onInitialLine(final Buffer buffer) {
        try {
            buffer.markReaderIndex();
            final Buffer part1 = buffer.readUntilSafe(config.getMaxAllowedInitialLineSize(), SipParser.SP);
            final Buffer part2 = buffer.readUntilSafe(config.getMaxAllowedInitialLineSize(), SipParser.SP);
            final Buffer part3 = buffer.readUntilSingleCRLF();
            if (part1 == null || part2 == null || part3 == null) {
                buffer.resetReaderIndex();
                return State.GET_INITIAL_LINE;
            }
            sipInitialLine = SipInitialLine.parse(part1, part2, part3);
        } catch (final IOException e) {
            throw new RuntimeException("Unable to read from stream due to IOException", e);
        }

        return State.GET_HEADER_NAME;
    }

    private final State onHeaderName(final Buffer buffer) {
        headerName = SipParser.nextHeaderNameDontCheckHColon(buffer);

        // not enough data in the stream, go back to the same
        // state and re-try when more data shows up.
        if (headerName == null) {
            return State.GET_HEADER_NAME;
        }

        return State.CONSUME_HCOLON;
    }

    /**
     * Consume HCOLON but we need to be slightly smarter than what the SipParser
     * is offering up since we may run out of bytes from the stream just as we
     * received the ':' and as such, we also need to be prepared to continue
     * the "other half" of the {@link SipParser#expectHCOLON(Buffer)}.
     *
     * @param buffer
     * @return
     */
    private final State onConsumeHColon(final Buffer buffer) {
        try {
            if (SipParser.expectHCOLONStreamFriendly(buffer) == -1) {
                return State.CONSUME_HCOLON;
            }
            return State.CONSUME_SWS_AFTER_HCOLON;
        } catch (final SipParseException e) {
            throw e;
        }
    }

    private final State onConsumeSWSAfterHColon(final Buffer buffer) {
        try {
            SipParser.consumeSWSAfterHColon(buffer);

            // if we still have stuff left in the buffer that means
            // that we must have consumed all SWS and then hit a non-sws
            // character and as such, we are done.
            // However, if there are no bytes left then we don't know
            // if the next thing that is about to show up on the wire is
            // yet another white spaces so therefore we have to once again
            // go back to this method...
            if (buffer.hasReadableBytes()) {
                headerValueState.reset(buffer.getReaderIndex());
                return State.GET_HEADER_VALUES;
            } else {
                return State.CONSUME_SWS_AFTER_HCOLON;
            }
        } catch (final IOException e) {
            throw new RuntimeException("Unable to read from stream due to IOException", e);
        }
    }

    private final State onHeaderValues(final Buffer buffer) {
        try {

            SipParser.readHeaderValues(headerValueState, headerName, buffer);
            if (headerValueState.done) {
                SipHeader header;
                final List<Buffer> values = headerValueState.values;
                for (final Buffer value : values) {
                    header = new SipHeaderImpl(headerName, value);
                    // The headers that are most commonly used will be fully
                    // parsed just because no stack can really function without
                    // looking into these headers.
                    if (header.isContentLengthHeader()) {
                        final ContentLengthHeader l = header.ensure().toContentLengthHeader();
                        contentLength = l.getContentLength();
                        header = l;
                    } else if (contactHeader == null && header.isContactHeader()) {
                        header = header.ensure();
                        contactHeader = header;
                    } else if (cSeqHeader == null && header.isCSeqHeader()) {
                        header = header.ensure();
                        cSeqHeader = header;
                    } else if ( maxForwardsHeader == null && header.isMaxForwardsHeader()) {
                        header = header.ensure();
                        maxForwardsHeader = header;
                    } else if (fromHeader == null && header.isFromHeader()) {
                        header = header.ensure();
                        fromHeader = header;
                    } else if (toHeader == null && header.isToHeader()) {
                        header = header.ensure();
                        toHeader = header;
                    } else if (viaHeader == null && header.isViaHeader()) {
                        header = header.ensure();
                        viaHeader = header;
                    } else if (callIdHeader == null && header.isCallIdHeader()) {
                        header = header.ensure();
                        callIdHeader = header;
                    } else if (routeHeader == null && header.isRouteHeader()) {
                        header = header.ensure();
                        routeHeader = header;
                    } else if (recordRouteHeader == null && header.isRecordRouteHeader()) {
                        header = header.ensure();
                        recordRouteHeader = header;
                    }

                    headers.computeIfAbsent(headerName.toString(), k -> new ArrayList<>()).add(header);
                }

                return State.CHECK_FOR_END_OF_HEADER_SECTION;
            }
            return State.GET_HEADER_VALUES;
        } catch (final IOException e) {
            throw new RuntimeException("Unable to read from stream due to IOException", e);
        }
    }

    /**
     * Every time we have parsed a header we need to check if there are more headers or
     * if we have reached the end of the section and as such there may be a body we need
     * to take care of. We know this by checking if there is a CRLF up next or not.
     *
     * @param buffer
     * @return
     */
    private final State onCheckEndHeaderSection(final Buffer buffer) {
        // can't tell. We need two bytes at least to check if there is
        // more headers or not
        if (buffer.getReadableBytes() < 2) {
            return State.CHECK_FOR_END_OF_HEADER_SECTION;
        }

        // ok, so there was a CRLF so we are going to fetch the payload
        // if there is one...
        if (SipParser.consumeCRLF(buffer) == 2) {
            return State.GET_PAYLOAD;
        }

        // nope, still headers
        return State.GET_HEADER_NAME;
    }

    /**
     * We may or may not have a payload, which depends on whether there is a Content-length
     * header available or not. If yes, then we will just slice out that entire thing once
     * we have enough readable bytes in the buffer.
     *
     * @param buffer
     * @return
     */
    private final State onPayload(final Buffer buffer) {
        if (contentLength == 0) {
            return State.DONE;
        }

        if (buffer.getReadableBytes() >= contentLength) {
            try {
                payload = buffer.readBytes(contentLength);
            } catch (final IOException e) {
                throw new RuntimeException("Unable to read from stream due to IOException", e);
            }
            return State.DONE;
        }

        return State.GET_PAYLOAD;
    }


    /**
     * This is very very super duper ugly but is highly adapted to how netty and its buffers
     * work and we are trying to avoid copying memory if we don't have to.
     *
     * @return
     */
    public byte[] getArray() {
        return buffer.getRawArray();
    }

    /**
     * Configuration interface for controlling aspects of the {@link SipMessageStreamBuilder}. The
     * reason for an interface for such a simple thing is that when building e.g. a real
     * SIP stack you may want to allow a user to configure this in a config file using
     * e.g. jackson and yaml but I didn't want to have that dependency on this library
     * itself.
     */
    public interface Configuration {

        /**
         * The maximum allowed initial line. If we pass this threshold we will drop
         * the message and close down the connection (if we are using a connection
         * oriented protocol ie)
         *
         * MAX_ALLOWED_INITIAL_LINE_SIZE = 1024;
         */
        int getMaxAllowedInitialLineSize() ;

        /**
         * The maximum allowed size of ALL headers combined (in bytes).
         *
         * MAX_ALLOWED_HEADERS_SIZE = 8192;
         */
        int getMaxAllowedHeadersSize() ;

        /**
         * MAX_ALLOWED_CONTENT_LENGTH = 2048;
         */
        int getMaxAllowedContentLength();

    }

    public static class DefaultConfiguration implements Configuration {

        private int maxAllowedInitialLineSize = 1024;
        private int maxAllowedHeadersSize = 4096;
        private int maxAllowedContentLength = 4096;

        @Override
        public int getMaxAllowedInitialLineSize() {
            return maxAllowedInitialLineSize;
        }

        @Override
        public int getMaxAllowedHeadersSize() {
            return maxAllowedHeadersSize;
        }

        @Override
        public int getMaxAllowedContentLength() {
            return maxAllowedContentLength;
        }

        public void setMaxAllowedInitialLineSize(final int value) {
            this.maxAllowedInitialLineSize = value;
        }

        public void setMaxAllowedHeadersSize(final int value) {
            this.maxAllowedHeadersSize = value;
        }

        public void setMaxAllowedContentLength(final int value) {
            this.maxAllowedContentLength = value;
        }
    }

    private static enum State {
        /**
         * In the INIT state we will consume any CRLF we find. This is allowed
         * for stream based protocols according to RFC 3261.
         */
        INIT,
        /**
         * While in this state, we will consume the initial line in the SIP
         * message. We will not verify it is correct that is up to later stages
         * of the processing.
         */
        GET_INITIAL_LINE,

        GET_HEADER_NAME,

        CONSUME_HCOLON,

        CONSUME_SWS_AFTER_HCOLON,

        GET_HEADER_VALUES,

        CHECK_FOR_END_OF_HEADER_SECTION,

        /**
         * Once we have found the separator between the headers and the payload
         * we will just blindly a byte at a time until we have read the entire
         * payload. The length of the payload was discovered during the
         * GET_HEADERS phase where we were not only just consuming all headers
         * but we were looking for the Content-Length header as well.
         */
        GET_PAYLOAD,

        DONE;
    }
}
