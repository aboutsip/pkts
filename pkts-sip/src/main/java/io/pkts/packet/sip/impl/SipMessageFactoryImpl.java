/**
 * 
 */
package io.pkts.packet.sip.impl;

import io.pkts.buffer.Buffer;
import io.pkts.packet.sip.SipMessageFactory;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.SipRequest;
import io.pkts.packet.sip.SipResponse;
import io.pkts.packet.sip.address.URI;
import io.pkts.packet.sip.header.CSeqHeader;
import io.pkts.packet.sip.header.CallIdHeader;
import io.pkts.packet.sip.header.FromHeader;
import io.pkts.packet.sip.header.MaxForwardsHeader;
import io.pkts.packet.sip.header.ToHeader;
import io.pkts.packet.sip.header.ViaHeader;

import java.util.List;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public class SipMessageFactoryImpl implements SipMessageFactory {

    /**
     * 
     */
    public SipMessageFactoryImpl() {
        // TODO Auto-generated constructor stub
    }

    /**
     * {@inheritDoc}
     * 
     * @throws SipParseException
     */
    @Override
    public SipResponse createResponse(final int statusCode, final SipRequest request) throws SipParseException {
        return request.createResponse(statusCode);
    }

    @Override
    public SipRequest createRequest(final SipRequest originalRequest) throws SipParseException {
        final SipRequestImpl impl = (SipRequestImpl) originalRequest;
        return impl.clone();
    }

    @Override
    public SipRequest createRequest(final URI requestURI, final Buffer method, final CallIdHeader callId,
            final CSeqHeader cseq,
            final FromHeader from, final ToHeader to, final List<ViaHeader> via, final MaxForwardsHeader maxForwards) {
        // TODO Auto-generated method stub
        return null;
    }
}
