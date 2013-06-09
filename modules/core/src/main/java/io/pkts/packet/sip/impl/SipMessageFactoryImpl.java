/**
 * 
 */
package io.pkts.packet.sip.impl;

import io.pkts.buffer.Buffers;
import io.pkts.packet.SipMessageFactory;
import io.pkts.packet.TransportPacket;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.SipRequest;
import io.pkts.packet.sip.SipResponse;
import io.pkts.packet.sip.header.CSeqHeader;
import io.pkts.packet.sip.header.CallIdHeader;
import io.pkts.packet.sip.header.FromHeader;
import io.pkts.packet.sip.header.MaxForwardsHeader;
import io.pkts.packet.sip.header.SipHeader;
import io.pkts.packet.sip.header.ToHeader;
import io.pkts.packet.sip.header.ViaHeader;


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
     * This dummy frame is only needed because currently the {@link SipMessage}
     * is "dummy". Will re-write the {@link SipMessage} to do things in a better
     * way and then I can get rid of this stupidity...
     */
    // private static final DummyLayer7Frame dummyFrame = new
    // DummyLayer7Frame();

    /**
     * {@inheritDoc}
     * 
     * @throws SipParseException
     */
    @Override
    public SipResponse createResponse(final int statusCode, final SipRequest request) throws SipParseException {
        final SipResponseLine initialLine = new SipResponseLine(statusCode, Buffers.wrap("OK"));
        final SipRequestImpl req = (SipRequestImpl) request;
        final TransportPacket pkt = req.getTransportPacket();
        final SipResponse response = new SipResponseImpl(pkt, initialLine, null, null, null);
        final CallIdHeader callID = req.getCallIDHeader();
        final FromHeader from = req.getFromHeader();
        final ToHeader to = req.getToHeader();
        final CSeqHeader cseq = request.getCSeqHeader();

        // TODO: need to extract all via headers
        final ViaHeader via = request.getViaHeader();
        final SipHeader maxForwards = req.getHeader(MaxForwardsHeader.NAME);
        response.setHeader(from);
        response.setHeader(to);
        response.setHeader(callID);
        response.setHeader(cseq);
        response.setHeader(via);
        response.setHeader(maxForwards);

        // The TimeStamp header should be there as well but screw it.
        // TODO: need to add any record-route headers

        return response;
    }

    @Override
    public SipRequest createRequest(final SipRequest originalRequest) throws SipParseException {
        final SipRequestImpl impl = (SipRequestImpl) originalRequest;
        return impl.clone();
    }
}
