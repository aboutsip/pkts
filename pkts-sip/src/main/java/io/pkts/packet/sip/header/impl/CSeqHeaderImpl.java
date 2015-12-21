/**
 * 
 */
package io.pkts.packet.sip.header.impl;

import io.pkts.buffer.Buffer;
import io.pkts.packet.sip.header.CSeqHeader;


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

    @Override
    public CSeqHeader.Builder copy() {
        return CSeqHeader.withMethod(this.method).withCSeq(this.cseqNumber);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public long getSeqNumber() {
        return this.cseqNumber;
    }


    @Override
    public CSeqHeader clone() {
        // TODO: no need to clone once the Buffer is truly immutable.
        return new CSeqHeaderImpl(this.cseqNumber, this.method.clone(), getValue().clone());
    }

    @Override
    public CSeqHeader ensure() {
        return this;
    }

}
