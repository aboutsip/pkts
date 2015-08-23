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

    Buffer NAME = Buffers.wrap("CSeq");

    Buffer getMethod();

    long getSeqNumber();

    @Override
    CSeqHeader clone();

    /**
     * Parse the value as a cseq value. This method assumes that you have already parsed out the
     * actual header name "CSeq: "
     * 
     * @param value
     * @return
     * @throws SipParseException
     */
    public static CSeqHeader frame(final Buffer value) throws SipParseException {
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

    static CSeqHeaderBuilder withMethod(final Buffer method) {
        return new CSeqHeaderBuilder(assertNotEmpty(method, "Method cannot be null or empty"));
    }

    static CSeqHeaderBuilder withMethod(final String method) {
        return new CSeqHeaderBuilder(Buffers.wrap(assertNotEmpty(method, "Method cannot be null or empty")));
    }

    class CSeqHeaderBuilder {

        private long cseq;
        private final Buffer method;

        private CSeqHeaderBuilder(final Buffer method) {
            this.method = method;
        }

        /**
         * 
         * @param cseq
         * @return
         * @throws SipParseException in case the specified sequence number is less than zero.
         */
        public CSeqHeaderBuilder cseq(final long cseq) throws SipParseException {
            assertArgument(cseq >= 0, "Sequence number must be greater or equal to zero");
            this.cseq = cseq;
            return this;
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
