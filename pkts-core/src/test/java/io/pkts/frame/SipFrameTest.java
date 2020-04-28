/**
 *
 */
package io.pkts.frame;





import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.pkts.PktsTestBase;
import io.pkts.buffer.Buffers;
import io.pkts.framer.SIPFramer;
import io.pkts.packet.IPPacket;
import io.pkts.packet.TransportPacket;
import io.pkts.packet.sip.SipPacket;
import io.pkts.packet.sip.SipRequestPacket;

/**
 * @author jonas@jonasborjesson.com
 */
public class SipFrameTest extends PktsTestBase {

    private IPPacket ipPkt;
    private TransportPacket transportPkt;

    /**
     * {@inheritDoc}
     */
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        transportPkt = mock(TransportPacket.class);
        ipPkt = mock(IPPacket.class);
        when(transportPkt.getParentPacket()).thenReturn(ipPkt);
        when(transportPkt.getArrivalTime()).thenReturn(123L);
        when(ipPkt.getSourceIP()).thenReturn("192.168.0.100");
        when(ipPkt.getDestinationIP()).thenReturn("10.36.10.10");
        when(transportPkt.getDestinationPort()).thenReturn(5060);
        when(transportPkt.getSourcePort()).thenReturn(5060);
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
        final SipPacket sip = framer.frame(transportPkt, sipFrameBuffer);
        testSipRequest(sip);
        assertThat(sip.getArrivalTime(), is(123L));
        assertThat(((IPPacket) sip.getParentPacket().getParentPacket()).getSourceIP(), is("192.168.0.100"));
        assertThat(((IPPacket) sip.getParentPacket().getParentPacket()).getDestinationIP(), is("10.36.10.10"));
        assertThat(((TransportPacket) sip.getParentPacket()).getDestinationPort(), is(5060));
        assertThat(((TransportPacket) sip.getParentPacket()).getSourcePort(), is(5060));
    }

    @Test
    public void testParseSipResponse() throws Exception {
        final SIPFramer framer = new SIPFramer();
        testSipResponse(framer.frame(transportPkt, sipFrameBuffer180Response));
    }

    @Test
    public void testParseSipRequestRaw() throws Exception {
        final SIPFramer framer = new SIPFramer();
        final SipPacket sip = framer.frame(null, sipFrameBuffer);
        assertNull(sip.getParentPacket());
        testSipRequest(sip);
    }

    @Test
    public void testParseSipResponseRaw() throws Exception {
        final SIPFramer framer = new SIPFramer();
        final SipPacket sip = framer.frame(null, sipFrameBuffer180Response);
        assertNull(sip.getParentPacket());
        testSipResponse(sip);
    }

    /**
     * Tests basic sip response content
     *
     * @param sip
     *            sip packet from {@link SIPFramer}
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private static void testSipResponse(final SipPacket sip) {

        assertThat(sip.getMethod().toString(), is("INVITE"));
        assertThat(sip.getHeader(Buffers.wrap("Call-ID")).get().getValue().toString(), is("1-16732@127.0.1.1"));
        assertThat(sip.getHeader(Buffers.wrap("Content-Length")).get().getValue().toString(), is("0"));
    }

    /**
     * Tests basic sip request content
     *
     * @param sip
     *            sip packet from {@link SIPFramer}
     */
    private static void testSipRequest(final SipPacket sip) {

        assertThat(sip.getMethod().toString(), is("INVITE"));
        assertThat(((SipRequestPacket) sip).getRequestUri().toString(), is("sip:service@127.0.0.1:5090"));

        // just check some random headers
        assertThat(sip.getHeader(Buffers.wrap("Content-Length")).get().getValue().toString(), is("129"));
        assertThat(sip.getHeader(Buffers.wrap("Call-ID")).get().getValue().toString(), is("1-16732@127.0.1.1"));
    }
}
