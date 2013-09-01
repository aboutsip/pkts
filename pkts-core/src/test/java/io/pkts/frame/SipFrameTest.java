/**
 * 
 */
package io.pkts.frame;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import io.pkts.PktsTestBase;
import io.pkts.buffer.Buffers;
import io.pkts.framer.SIPFramer;
import io.pkts.packet.TransportPacket;
import io.pkts.packet.sip.SipPacket;
import io.pkts.packet.sip.SipRequestPacket;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author jonas@jonasborjesson.com
 */
public class SipFrameTest extends PktsTestBase {

    private TransportPacket transportPkt;

    /**
     * {@inheritDoc}
     */
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.transportPkt = mock(TransportPacket.class);
        when(this.transportPkt.getArrivalTime()).thenReturn(123L);
        when(this.transportPkt.getSourceIP()).thenReturn("192.168.0.100");
        when(this.transportPkt.getDestinationIP()).thenReturn("10.36.10.10");
        when(this.transportPkt.getDestinationPort()).thenReturn(5060);
        when(this.transportPkt.getSourcePort()).thenReturn(5060);
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
        final SIPFramer framer = new SIPFramer();
        final SipPacket sip = framer.frame(this.transportPkt, this.sipFrameBuffer);
        assertThat(sip.getMethod().toString(), is("INVITE"));
        assertThat(((SipRequestPacket) sip).getRequestUri().toString(), is("sip:service@127.0.0.1:5090"));

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
        final SIPFramer framer = new SIPFramer();
        final SipPacket sip = framer.frame(this.transportPkt, this.sipFrameBuffer180Response);
        assertThat(sip.getMethod().toString(), is("INVITE"));
        assertThat(sip.getHeader(Buffers.wrap("Call-ID")).getValue().toString(), is("1-16732@127.0.1.1"));
        assertThat(sip.getHeader(Buffers.wrap("Content-Length")).getValue().toString(), is("0"));
    }

}
