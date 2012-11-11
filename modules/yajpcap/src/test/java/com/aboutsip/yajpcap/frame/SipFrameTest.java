/**
 * 
 */
package com.aboutsip.yajpcap.frame;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.aboutsip.buffer.Buffers;
import com.aboutsip.yajpcap.YajTestBase;
import com.aboutsip.yajpcap.framer.SIPFramer;
import com.aboutsip.yajpcap.packet.TransportPacket;
import com.aboutsip.yajpcap.packet.sip.SipMessage;
import com.aboutsip.yajpcap.packet.sip.SipRequest;

/**
 * @author jonas@jonasborjesson.com
 */
public class SipFrameTest extends YajTestBase {

    private Layer4Frame layer4Frame;

    /**
     * {@inheritDoc}
     */
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        // mock up the layer above us...
        this.layer4Frame = mock(Layer4Frame.class);
        final TransportPacket pkt = mock(TransportPacket.class);
        when(this.layer4Frame.parse()).thenReturn(pkt);

        when(pkt.getArrivalTime()).thenReturn(123L);
        when(pkt.getSourceIP()).thenReturn("192.168.0.100");
        when(pkt.getDestinationIP()).thenReturn("10.36.10.10");
        when(pkt.getDestinationPort()).thenReturn(5060);
        when(pkt.getSourcePort()).thenReturn(5060);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testParseSipRequest() throws Exception {
        final SIPFramer framer = new SIPFramer(this.framerManager);
        final SipFrame frame = framer.frame(this.layer4Frame, this.sipFrameBuffer);
        final SipMessage sip = frame.parse();
        assertThat(sip.getMethod().toString(), is("INVITE"));
        assertThat(((SipRequest) sip).getRequestUri().toString(), is("sip:service@127.0.0.1:5090"));

        // just check some random headers
        assertThat(sip.getHeader(Buffers.wrap("Content-Length")).getValue().toString(), is("129"));
        assertThat(sip.getHeader(Buffers.wrap("Call-ID")).getValue().toString(), is("1-16732@127.0.1.1"));

        assertThat(sip.getArrivalTime(), is(123L));
        assertThat(sip.getSourceIP(), is("192.168.0.100"));
        assertThat(sip.getDestinationIP(), is("10.36.10.10"));
        assertThat(sip.getDestinationPort(), is(5060));
        assertThat(sip.getSourcePort(), is(5060));
    }

    @Test
    public void testParseSipResponse() throws Exception {
        final SIPFramer framer = new SIPFramer(this.framerManager);
        final SipFrame frame = framer.frame(this.layer4Frame, this.sipFrameBuffer180Response);
        final SipMessage sip = frame.parse();
        assertThat(sip.getMethod().toString(), is("INVITE"));
        assertThat(sip.getHeader(Buffers.wrap("Call-ID")).getValue().toString(), is("1-16732@127.0.1.1"));
        assertThat(sip.getHeader(Buffers.wrap("Content-Length")).getValue().toString(), is("0"));
    }

}
