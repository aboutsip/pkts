/**
 * 
 */
package com.aboutsip.yajpcap.packet.sip;

import com.aboutsip.buffer.Buffers;
import com.aboutsip.yajpcap.packet.SipMessageFactory;
import com.aboutsip.yajpcap.packet.TransportPacket;
import com.aboutsip.yajpcap.packet.sip.header.CSeqHeader;
import com.aboutsip.yajpcap.packet.sip.header.CallIdHeader;
import com.aboutsip.yajpcap.packet.sip.header.FromHeader;
import com.aboutsip.yajpcap.packet.sip.header.MaxForwardsHeader;
import com.aboutsip.yajpcap.packet.sip.header.ToHeader;
import com.aboutsip.yajpcap.packet.sip.header.ViaHeader;
import com.aboutsip.yajpcap.packet.sip.impl.SipRequestImpl;
import com.aboutsip.yajpcap.packet.sip.impl.SipResponseImpl;
import com.aboutsip.yajpcap.packet.sip.impl.SipResponseLine;

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

}
