/**
 * 
 */
package io.pkts.packet.sip.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import io.pkts.PktsTestBase;
import io.pkts.packet.sip.SipMessage;
import io.pkts.packet.sip.SipResponse;
import io.pkts.packet.sip.header.CSeqHeader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author jonas@jonasborjesson.com
 */
public class SipResponseTest extends PktsTestBase {

    private SipResponse response;

    /**
     * {@inheritDoc}
     */
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.response = (SipResponse) parseMessage(this.sipFrameBuffer180Response);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * The method in a response is a little "trickier" than in a request since
     * you will need to grab it from the CSeq header
     */
    @Test
    public void testGetMethod() throws Exception {
        assertThat(this.response.getMethod().toString(), is("INVITE"));
    }

    /**
     * For responses, there was a bug that if you asked for the method and then asked for the CSeq
     * we would blow up. Make sure that we don't ever end up in that situation again!!!
     * 
     * @throws Exception
     */
    @Test
    public void testCSeqHeader() throws Exception {
        final StringBuilder sb = new StringBuilder();
        sb.append("SIP/2.0 200 OK\r\n");
        sb.append("From: <sip:bob@example.com>\r\n");
        sb.append("To: <sip:alice@example.com>;tag=15801SIPpTag013\r\n");
        sb.append("Call-ID: 3dec1fff-cdfe-49f0-9b99-76d58a6d0d31\r\n");
        sb.append("CSeq: 0 INVITE\r\n");
        sb.append("Contact: <sip:127.0.0.1:5070;transport=UDP>\r\n");
        sb.append("Content-Type: io.sipstack.application.application/sdp\r\n");
        sb.append("Content-Length:   129\r\n");
        sb.append("\r\n");
        sb.append("v=0\r\n");
        sb.append("o=user1 53655765 2353687637 IN IP4 127.0.0.1\r\n");
        sb.append("s=-\r\n");
        sb.append("c=IN IP4 127.0.0.1\r\n");
        sb.append("t=0 0\r\n");
        sb.append("m=audio 6000 RTP/AVP 0\r\n");
        sb.append("a=rtpmap:0 PCMU/8000\r\n");
        final SipMessage msg = parseMessage(sb.toString());
        assertThat(msg.getMethod().toString(), is("INVITE"));
        final CSeqHeader cseq = msg.getCSeqHeader();
        assertThat(cseq.getSeqNumber(), is(0L));
    }

}
