/**
 * 
 */
package io.pkts.packet.sip.header.impl;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.header.CSeqHeader;
import io.pkts.packet.sip.impl.SipParser;

import java.io.IOException;


/**
 * @author jonas@jonasborjesson.com
 * 
 */
public final class CSeqHeaderImpl extends SipHeaderImpl implements CSeqHeader {

    private final long cseqNumber;
    private final Buffer method;

    /**
     * 
     */
    public CSeqHeaderImpl(final long cseqNumber, final Buffer method, final Buffer value) {
        super(CSeqHeader.NAME, value);
        this.cseqNumber = cseqNumber;
        this.method = method;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Buffer getMethod() {
        return this.method;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getSeqNumber() {
        return this.cseqNumber;
    }

    @Override
    public Buffer getValue() {
        if (super.getValue() != null) {
            return super.getValue();
        }

        final int size = Buffers.stringSizeOf(this.cseqNumber);
        final Buffer value = Buffers.createBuffer(size + 1 + this.method.getReadableBytes());
        value.writeAsString(this.cseqNumber);
        value.write(SipParser.SP);
        this.method.getBytes(value);
        return value;
    }

    /**
     * Parse the value as a cseq value. This method assumes that you have
     * already parsed out the actual header name "CSeq: "
     * 
     * @param value
     * @return
     * @throws SipParseException
     */
    public static CSeqHeader parseValue(final Buffer value) throws SipParseException {
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

    @Override
    public CSeqHeader clone() {
        return new CSeqHeaderImpl(this.cseqNumber, this.method.clone(), getValue().clone());
    }

}
