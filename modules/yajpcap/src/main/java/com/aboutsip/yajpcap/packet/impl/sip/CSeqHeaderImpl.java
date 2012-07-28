/**
 * 
 */
package com.aboutsip.yajpcap.packet.impl.sip;

import java.io.IOException;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.yajpcap.packet.CSeqHeader;

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
        super(null, value);
        this.cseqNumber = cseqNumber;
        this.method = method;
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public Buffer getName() {
        return CSeqHeader.NAME;
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

}
