package com.aboutsip.yajpcap.packet.sip.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.buffer.Buffers;
import com.aboutsip.yajpcap.YajTestBase;
import com.aboutsip.yajpcap.packet.TransportPacket;
import com.aboutsip.yajpcap.packet.sip.SipHeader;
import com.aboutsip.yajpcap.packet.sip.SipRequest;

public class SipMessageImplTest extends YajTestBase {

    private SipRequest request;
    private final int sampleRate = 8000;

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
    }

}
