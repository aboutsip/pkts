/**
 * 
 */
package io.pkts.packet.sip;

import io.pkts.PktsTestBase;
import io.pkts.RawData;
import io.pkts.packet.sip.header.CSeqHeader;
import io.pkts.packet.sip.header.ContactHeader;
import io.pkts.packet.sip.header.ContentTypeHeader;
import io.pkts.packet.sip.header.SipHeader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

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
     * Ensure that we can create a response off of a request and that we also
     * can change values of headers by registering functions for those values.
     *
     * @throws Exception
     */
    @Test
    public void testCreateResponseFromRequest() throws Exception {
        final SipRequest req = (SipRequest) parseMessage(RawData.sipInvite);
        final SipResponse resp = req.createResponse(184)
                .withContactHeader(ContactHeader.withHost("hello.com").build())
                .withHeader(SipHeader.create("X-Blah", "Hello"))
                .withHeader(SipHeader.create("X-Filter-Me-Out", "Bye bye"))
                .onHeader(h -> {
                    if ("X-Filter-Me-Out".equals(h.getNameStr())) {
                        // drop a header by returning null. Now,
                        // why you just added it above only to drop it
                        // is obviously an interesting question but this
                        // is only for testing purposes...
                        return null;
                    }
                    return h;
                })
                .onToHeader(to -> to.withParameter("tag", "asdf-asdf-123"))
                .onContactHeader(c -> c.withParameter("apa", "monkey").withUser("Kalle").withDisplayName("Kalle Karlsson"))
                .build();

        // of course, the status should be 184
        assertThat(resp.getStatus(), is(184));

        // because we dropped this header...
        assertThat(resp.getHeader("X-Filter-Me-Out").isPresent(), is(false));

        // because we didn't drop this header.
        assertThat(resp.getHeader("X-Blah").get().getValue().toString(), is("Hello"));

        // We added a tag to the To-header. The actual header
        // should have been copied from the request.
        assertThat(resp.getToHeader().getValue().toString(), is("sut <sip:service@127.0.0.1:5090>;tag=asdf-asdf-123"));

        // because we registered a function when the contact header is built
        // and in that function we changed the user and added a parameter.
        assertThat(resp.getContactHeader().getValue().toString(), is("\"Kalle Karlsson\" <sip:Kalle@hello.com>;apa=monkey"));

        // The content length should be zero
        assertThat(resp.getContentLength(), is(0));
    }

    /**
     * By default, no route headers are pushed onto the response so ensure that
     * and then ensure that we can add them and the appropriate functions are called etc.
     *
     * @throws Exception
     */
    @Test
    public void testCreateResponseFromRequestAddRouteHeaders() throws Exception {
        SipRequest req = (SipRequest) parseMessage(RawData.sipInviteOneRouteHeader);

        // No route headers should be copied by default
        SipResponse resp = req.createResponse(200).build();
        assertHeaderNotPresent(resp.getRouteHeaders());

        // now we ask to add the routes headers from the request so
        // there should be one...
        resp = req.createResponse(200).withRouteHeaders(req.getRouteHeaders()).build();
        assertHeader(resp.getRouteHeader(), "<sip:one@aboutsip.com;transport=udp>");
        assertThat(resp.getRouteHeaders().size(), is(1));

        // now we ask to add a route header but we will also register a function
        // to be called so we can process the Route header.
        resp = req.createResponse(200)
                .withRouteHeaders(req.getRouteHeaders())
                .onTopMostRouteHeader(route -> route.withDisplayName("Hello World").withUser("bob"))
                .onRouteHeader(route -> route.withDisplayName("Should not be called"))
                .build();

        assertHeader(resp.getRouteHeader(), "\"Hello World\" <sip:bob@aboutsip.com;transport=udp>");
        assertThat(resp.getRouteHeaders().size(), is(1));

        // do the same but with a request that has multiple route headers...
        req = (SipRequest) parseMessage(RawData.sipInviteThreeRouteHeaders);
        resp = req.createResponse(200)
                .withRouteHeaders(req.getRouteHeaders())
                .onTopMostRouteHeader(route -> route.withDisplayName("First One").withUser("no1"))
                .onRouteHeader(route -> route.withDisplayName("The next").withUser("not_first"))
                .build();

        // this should always return the top one...
        assertHeader(resp.getRouteHeader(), "\"First One\" <sip:no1@aboutsip.com;transport=udp>");

        // then check that we have a list of three...
        assertThat(resp.getRouteHeaders().size(), is(3));
        assertHeader(resp.getRouteHeaders().get(0), "\"First One\" <sip:no1@aboutsip.com;transport=udp>");
        assertHeader(resp.getRouteHeaders().get(1), "\"The next\" <sip:not_first@aboutsip.com;transport=tcp>");
        assertHeader(resp.getRouteHeaders().get(2), "\"The next\" <sip:not_first@aboutsip.com;transport=tcp>");
    }

    /**
     * Whenever a response is created, certain headers should be copied from the request,
     * other shouldn't so check that...
     *
     * @throws Exception
     */
    @Test
    public void testCreateResponseEnsureDefaultValues() throws Exception {
        final SipMessage msg = parseMessage(RawData.sipInviteOneRouteHeader);
        final SipResponse resp = msg.createResponse(200).build();

        System.out.println(msg);
        System.out.println(resp);

        assertThat(resp.getStatus(), is(200));

        // the only headers that should be copied are
        // to, from, via, call-id and cseq.
        assertHeader(resp.getFromHeader(), "sipp <sip:sipp@192.168.8.110:5060>;tag=17354SIPpTag001");
        assertHeader(resp.getToHeader(), "sut <sip:service@8.8.8.8:5060>");
        assertHeader(resp.getViaHeader(), "SIP/2.0/UDP 192.168.8.110:5060;branch=z9hG4bK-17354-1-0");
        assertThat(resp.getViaHeaders().size(), is(1));
        assertHeader(resp.getCallIDHeader(), "1-17354@192.168.8.110");
        assertHeader(resp.getCSeqHeader(), "1 INVITE");

        // so a total of 5 headers
        assertThat(resp.getAllHeaders().size(), is(5));

        // the rest of the headers should have been dropped
        assertHeaderNotPresent(resp.getContactHeader());
        assertHeaderNotPresent(resp.getMaxForwards());
        assertHeaderNotPresent(resp.getHeader("Subject"));

        // the Route headers are not moved over by default (should they?)
        assertHeaderNotPresent(resp.getRouteHeaders());

        // no content type or body etc is copied either
        assertThat(resp.getContentLength(), is(0));
        assertThat(resp.getContentTypeHeader(), is((ContentTypeHeader)null));
        assertThat(resp.hasContent(), is(false));
        assertThat(resp.getContent(), is((Object)null));
    }

    /**
     * Assert the value of the header.
     *
     * @param header
     * @param expectedValue
     */
    private void assertHeader(final SipHeader header, final String expectedValue) {
        assertThat(header.getValue().toString(), is(expectedValue));
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
