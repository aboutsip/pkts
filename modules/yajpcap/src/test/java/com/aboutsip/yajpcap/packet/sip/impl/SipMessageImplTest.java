package com.aboutsip.yajpcap.packet.sip.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.buffer.Buffers;
import com.aboutsip.yajpcap.RawData;
import com.aboutsip.yajpcap.YajTestBase;
import com.aboutsip.yajpcap.packet.TransportPacket;
import com.aboutsip.yajpcap.packet.sip.SipHeader;
import com.aboutsip.yajpcap.packet.sip.SipMessage;
import com.aboutsip.yajpcap.packet.sip.SipParseException;
import com.aboutsip.yajpcap.packet.sip.SipRequest;
import com.aboutsip.yajpcap.packet.sip.address.SipURI;
import com.aboutsip.yajpcap.packet.sip.address.URI;
import com.aboutsip.yajpcap.packet.sip.header.ContentTypeHeader;
import com.aboutsip.yajpcap.packet.sip.header.RecordRouteHeader;
import com.aboutsip.yajpcap.packet.sip.header.RouteHeader;
import com.aboutsip.yajpcap.packet.sip.header.ViaHeader;

public class SipMessageImplTest extends YajTestBase {

    private SipRequest request;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        // the boundaries between headers and payload has been checked
        // with wireshark...
        final Buffer line = this.sipFrameBuffer.readLine();
        final Buffer headers = this.sipFrameBuffer.readBytes(331);
        final Buffer payload = this.sipFrameBuffer.slice();

        final SipInitialLine initialLine = SipInitialLine.parse(line);
        assertThat(initialLine.isRequestLine(), is(true));

        final TransportPacket pkt = mock(TransportPacket.class);
        this.request = new SipRequestImpl(pkt, (SipRequestLine) initialLine, headers, payload, null);
    }

    @Override
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Record-Route headers are typically handled a little differently since
     * they actually are ordered. These tests focuses on making sure that we
     * maintain the order of the RR headers as found in the original request.
     * 
     * @throws Exception
     */
    @Test
    public void testGetRecordRouteHeaders() throws Exception {
        final SipMessage msg = parseMessage(RawData.sipInviteOneRecordRouteHeader, 382);
        final RecordRouteHeader rr = msg.getRecordRouteHeader();
        assertThat(rr, not((RecordRouteHeader) null));
        assertThat(rr.toString(), is("Record-Route: <sip:one@aboutsip.com;transport=udp>"));
        assertThat(rr.getValue().toString(), is("<sip:one@aboutsip.com;transport=udp>"));
        assertThat(rr.getAddress().getDisplayName().isEmpty(), is(true));
        final URI uri = rr.getAddress().getURI();
        assertThat(uri.isSipURI(), is(true));
        final SipURI sipUri = (SipURI) uri;
        assertThat(sipUri.getHost().toString(), is("aboutsip.com"));
    }

    @Test
    public void testGetViaHeader() throws Exception {
        final SipMessage msg = parseMessage(RawData.sipInviteOneRecordRouteHeader, 382);
        final ViaHeader via = msg.getViaHeader();
        assertThat(via.getPort(), is(5060));
        assertThat(via.getHost().toString(), is("127.0.0.1"));
        assertThat(via.getBranch().toString(), is("z9hG4bK-5647-1-0"));
        assertThat(via.getRPort(), is(-1));
        assertThat(via.hasRPort(), is(false));
    }

    /**
     * Make sure that we can handle Via-headers correctly.
     */
    @Test
    public void testGetViaHeaders() throws Exception {
        final SipMessage msg = parseMessage(RawData.sipInviteOneRecordRouteHeader, 382);

    }

    private SipMessage parseMessage(final byte[] rawData, final int headerSize) throws SipParseException, IOException {
        final Buffer msg = Buffers.wrap(rawData);
        final Buffer line = msg.readLine();
        final Buffer headers = msg.readBytes(headerSize);
        final Buffer payload = msg.slice();
        final SipInitialLine initialLine = SipInitialLine.parse(line);
        final TransportPacket pkt = mock(TransportPacket.class);
        if (initialLine.isRequestLine()) {
            return new SipRequestImpl(pkt, (SipRequestLine) initialLine, headers, payload, null);
        } else {
            return new SipResponseImpl(pkt, (SipResponseLine) initialLine, headers, payload, null);
        }
    }

    @Test
    public void testGetHeaders() throws Exception {
        SipHeader from = this.request.getFromHeader();
        assertThat(from.getName(), is(Buffers.wrap("From")));
        assertThat(from.getValue(), is(Buffers.wrap("sipp <sip:sipp@127.0.1.1:5060>;tag=16732SIPpTag001")));

        // should also be able to do like so
        from = this.request.getHeader(Buffers.wrap("From"));
        assertThat(from.getName(), is(Buffers.wrap("From")));
        assertThat(from.getValue(), is(Buffers.wrap("sipp <sip:sipp@127.0.1.1:5060>;tag=16732SIPpTag001")));

        // Grab the Via header
        final SipHeader via = this.request.getHeader(Buffers.wrap("Via"));
        assertThat(via.getName(), is(Buffers.wrap("Via")));
        assertThat(via.getValue(), is(Buffers.wrap("SIP/2.0/UDP 127.0.1.1:5060;branch=z9hG4bK-16732-1-0")));

        // Lets skip a few headers. The contact header comes further
        // into the message
        final SipHeader contact = this.request.getHeader(Buffers.wrap("Contact"));
        assertThat(contact.getName(), is(Buffers.wrap("Contact")));
        assertThat(contact.getValue(), is(Buffers.wrap("sip:sipp@127.0.1.1:5060")));

        // fetch a header that doesn't exist.
        assertThat(this.request.getHeader("Whatever"), is((SipHeader) null));

        // this message does not have a Record-Route header
        final RecordRouteHeader rr = this.request.getRecordRouteHeader();
        assertThat(rr, is((RecordRouteHeader) null));

        // nor does it have a route header
        final RouteHeader route = this.request.getRouteHeader();
        assertThat(route, is((RouteHeader) null));

        // and the purpose with that is that now that we ask for a header that
        // appear before the contact, we should actually find it in the internal
        // parsed storage
        final SipHeader callId = this.request.getHeader(Buffers.wrap("Call-ID"));
        assertThat(callId.getName(), is(Buffers.wrap("Call-ID")));
        assertThat(callId.getValue(), is(Buffers.wrap("1-16732@127.0.1.1")));

        // ask for something that doesn't exist...
        // which would have caused us to go through everything
        // in the headers buffer...
        assertThat(this.request.getHeader(Buffers.wrap("Whatever")), is((SipHeader) null));

        // but of course, we should still be able to ask
        // for anything we want
        final SipHeader maxForwards = this.request.getHeader(Buffers.wrap("Max-Forwards"));
        assertThat(maxForwards.getName(), is(Buffers.wrap("Max-Forwards")));
        assertThat(maxForwards.getValue(), is(Buffers.wrap("70")));

        final SipHeader contentLength = this.request.getHeader(Buffers.wrap("Content-Length"));
        assertThat(contentLength.getName(), is(Buffers.wrap("Content-Length")));
        assertThat(contentLength.getValue(), is(Buffers.wrap("129")));

        final SipHeader contentType = this.request.getHeader(Buffers.wrap("Content-Type"));
        assertThat(contentType.getName(), is(Buffers.wrap("Content-Type")));
        assertThat(contentType.getValue(), is(Buffers.wrap("application/sdp")));

        final ContentTypeHeader contentTypeHeader = this.request.getContentTypeHeader();
        assertThat(contentTypeHeader.getName(), is(Buffers.wrap("Content-Type")));
        assertThat(contentTypeHeader.getContentType(), is(Buffers.wrap("application")));
        assertThat(contentTypeHeader.getContentSubType(), is(Buffers.wrap("sdp")));
        assertThat(contentTypeHeader.isSDP(), is(true));
        assertThat(contentTypeHeader.getValue(), is(Buffers.wrap("application/sdp")));
    }

}
