/**
 * 
 */
package com.aboutsip.yajpcap.packet.impl.sip;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.yajpcap.packet.CSeqHeader;
import com.aboutsip.yajpcap.packet.SipHeader;
import com.aboutsip.yajpcap.packet.SipResponse;

/**
 * @author jonas@jonasborjesson.com
 */
public final class SipResponseImpl extends SipMessageImpl implements SipResponse {

    private CSeqHeader cseq;

    /**
     * @param initialLine
     * @param headers
     * @param payload
     */
    public SipResponseImpl(final SipResponseLine initialLine, final Buffer headers, final Buffer payload) {
        super(initialLine, headers, payload);
    }

    /**
     * {@inheritDoc}
     * 
     * @throws SipParseException
     */
    @Override
    public Buffer getMethod() throws SipParseException {
        if (this.cseq == null) {
            final SipHeader header = getHeader(CSEQ_HEADER);
            this.cseq = CSeqHeaderImpl.parseValue(header.getValue());
        }
        return this.cseq.getMethod();
    }

}
