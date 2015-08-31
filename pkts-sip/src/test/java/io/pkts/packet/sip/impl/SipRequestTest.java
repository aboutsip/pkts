/**
 * 
 */
package io.pkts.packet.sip.impl;

import io.pkts.PktsTestBase;
import io.pkts.RawData;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipMessage;
import io.pkts.packet.sip.SipRequest;
import io.pkts.packet.sip.SipResponse;
import io.pkts.packet.sip.header.impl.SipHeaderImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * @author jonas
 * 
 */
public class SipRequestTest extends PktsTestBase {

    /*
     * (non-Javadoc)
     * 
     * @see com.aboutsip.yajpcap.YajTestBase#setUp()
     */
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.aboutsip.yajpcap.YajTestBase#tearDown()
     */
    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Simple test for making sure that the payload makes it into the toString
     * stuff.
     * 
     * @throws Exception
     */
    @Test
    public void testToString() throws Exception {
        final SipRequest req = (SipRequest) parseMessage(RawData.sipInvite);
        System.out.println(req);
        assertThat(req.toString().contains("o=user1 53655765 2353687637 IN IP4 127.0.1.1"), is(true));
    }
    
    @Test
    public void testCreateResponse() throws Exception {
        assertReasonPhrase(100, "Trying");
        assertReasonPhrase(180, "Ringing");
        assertReasonPhrase(200, "OK");
        assertReasonPhrase(202, "Accepted");
        assertReasonPhrase(302, "Moved Temporarily");
        assertReasonPhrase(400, "Bad Request");
        assertReasonPhrase(500, "Server Internal Error");
        assertReasonPhrase(600, "Busy Everywhere");
        assertReasonPhrase(603, "Decline");
    }
    
    private void assertReasonPhrase(int statusCode, String expectedReason) throws Exception {
        final SipMessage msg = parseMessage(RawData.sipInviteOneRecordRouteHeader);
        final SipResponse response = msg.createResponse(statusCode);
        assertThat(response.getReasonPhrase().toString(), is(expectedReason));
    }

    /**
     * Make sure that when we clone a request that the clone and the original
     * indeed are truly separated.
     * 
     * @throws Exception
     */
    @Test
    public void testClone() throws Exception {
        final SipRequest req = (SipRequest) parseMessage(RawData.sipInvite);
        final SipRequest clone = req.clone();
        assertThat(req.toString(), is(clone.toString()));
        assertThat(req.toBuffer(), is(clone.toBuffer()));
        req.addHeader(new SipHeaderImpl(Buffers.wrap("Hello"), Buffers.wrap("world")));

        assertThat(req.toString().contains("Hello: world"), is(true));
        assertThat(clone.toString().contains("Hello: world"), is(false));

        // TODO: has to be fixed once the sip request stuff has been modified.
        // ((SipURI) req.getRequestUri()).setPort(7777);
        // ((SipURI) clone.getRequestUri()).setPort(8888);

        // assertThat(req.toString().contains("sip:service@127.0.0.1:7777"), is(true));
        // assertThat(clone.toString().contains("sip:service@127.0.0.1:8888"), is(true));
    }

    @Test
    public void testCloneAfterManipulation() throws Exception {
        final SipRequest req = (SipRequest) parseMessage(RawData.sipInvite);
        req.getCallIDHeader();
        System.err.println(req.getHeader("Content-Length"));
        req.getContentTypeHeader();
        System.err.println(req.getContent());
        final SipRequest clone = req.clone();
        System.err.println(clone);
        // assertThat(req.toString(), is(clone.toString()));
        assertThat(req.toBuffer(), is(clone.toBuffer()));
        req.addHeader(new SipHeaderImpl(Buffers.wrap("Hello"), Buffers.wrap("world")));

        System.err.println(clone);

        assertThat(req.toString().contains("Hello: world"), is(true));
        assertThat(clone.toString().contains("Hello: world"), is(false));

        // TODO: has to be fixed once the sip request stuff has been modified.
        // ((SipURI) req.getRequestUri()).setPort(7777);
        // ((SipURI) clone.getRequestUri()).setPort(8888);

        // assertThat(req.toString().contains("sip:service@127.0.0.1:7777"), is(true));
        // assertThat(clone.toString().contains("sip:service@127.0.0.1:8888"), is(true));
    }
}
