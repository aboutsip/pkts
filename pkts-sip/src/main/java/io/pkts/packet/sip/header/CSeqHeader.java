/**
 * 
 */
package io.pkts.packet.sip.header;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.header.impl.CSeqHeaderImpl;
import io.pkts.packet.sip.impl.SipParser;

import java.io.IOException;

import static io.pkts.packet.sip.impl.PreConditions.assertArgument;
import static io.pkts.packet.sip.impl.PreConditions.assertNotEmpty;

/**
 * @author jonas@jonasborjesson.com
 */
public interface CSeqHeader extends SipHeader {

    public static final String METHOD_CANNOT_BE_NULL_OR_EMPTY = "Method cannot be null or empty";
    Buffer NAME = Buffers.wrap("CSeq");

    Buffer getMethod();

    long getSeqNumber();

    @Override
    CSeqHeader clone();

    @Override
    Builder copy();

    @Override
    default boolean isCSeqHeader() {
        return true;
    }

    @Override
    default CSeqHeader toCSeqHeader() {
        return this;
    }

    /**
     * Parse the value as a cseq value. This method assumes that you have already parsed out the
     * actual header name "CSeq: "
     * 
     * @param value
     * @return
     * @throws SipParseException
     */
    static CSeqHeader frame(final Buffer value) throws SipParseException {
        try {
            final Buffer valueCopy = value.slice();
            final Buffer cseq = SipParser.expectDigit(value);
            final long number = Long.parseLong(cseq.toString());
            SipParser.consumeWS(value);
            final Buffer method = value.readLine();
            return new CSeqHeaderImpl(number, method, valueCopy);
        } catch (final IOException e) {
            throw new SipParseException(value.getReaderIndex(),
                    "Could not read from the underlying stream while parsing method");
        }
    }

    static Builder withMethod(final Buffer method) {
        return new Builder(assertNotEmpty(method, METHOD_CANNOT_BE_NULL_OR_EMPTY));
    }

    static Builder withMethod(final String method) {
        return new Builder(Buffers.wrap(assertNotEmpty(method, METHOD_CANNOT_BE_NULL_OR_EMPTY)));
    }

    class Builder implements SipHeader.Builder<CSeqHeader> {

        private long cseq;
        private Buffer method;

        private Builder(final Buffer method) {
            this.method = method;
        }

        /**
         * 
         * @param cseq
         * @return
         * @throws SipParseException in case the specified sequence number is less than zero.
         */
        public Builder withCSeq(final long cseq) throws SipParseException {
            assertArgument(cseq >= 0, "Sequence number must be greater or equal to zero");
            this.cseq = cseq;
            return this;
        }

        public Builder increment() {
            ++cseq;
            return this;
        }

        public Builder withMethod(final Buffer method) throws SipParseException {
            assertNotEmpty(method, METHOD_CANNOT_BE_NULL_OR_EMPTY);
            this.method = method;
            return this;
        }

        public Builder withMethod(final String method) throws SipParseException {
            this.method = Buffers.wrap(assertNotEmpty(method, METHOD_CANNOT_BE_NULL_OR_EMPTY));
            return this;
        }

        @Override
        public Builder withValue(Buffer value) {
            throw new RuntimeException("Not implemented yet");
        }

        public CSeqHeader build() {
            final int size = Buffers.stringSizeOf(this.cseq);
            final Buffer value = Buffers.createBuffer(size + 1 + this.method.getReadableBytes());
            value.writeAsString(this.cseq);
            value.write(SipParser.SP);
            this.method.getBytes(value);
            return new CSeqHeaderImpl(cseq, method, value);
        }

    }

}
