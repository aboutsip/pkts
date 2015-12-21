/**
 * 
 */
package io.pkts.packet.sip;

import io.pkts.PktsTestBase;
import io.pkts.RawData;
import io.pkts.buffer.Buffer;
import io.pkts.packet.sip.header.CSeqHeader;
import io.pkts.packet.sip.header.ContactHeader;
import io.pkts.packet.sip.header.ContentTypeHeader;
import io.pkts.packet.sip.header.SipHeader;
import io.pkts.packet.sip.header.ViaHeader;
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
     * When acting a proxy there are a few common tasks one has to do
     * when forwarding a response upstream. Typically, you will pop
     * the top-most Via-header, perhaps add some headers and, even
     * though not recommended, re-write the Contact...
     *
     * @throws Exception
     */
    @Test
    public void testCreateResponseFromCopy() throws Exception {
        final SipResponse response = this.response.copy()
                .withReasonPhrase("really ringing")
                .withPoppedVia() // pop the old one that hopefully pointed to us
                .withHeader(SipHeader.create("X-Hello", "World"))
                .onContactHeader(contact -> contact.withHost("12.13.14.15").withPort(5678))
                .build();

        assertThat(response.getStatus(), is(180));
        assertThat(response.getReasonPhrase().toString(), is("really ringing"));
        assertHeader(response.getFromHeader(), "sipp <sip:sipp@127.0.1.1:5060>;tag=16732SIPpTag001");

        assertHeader(response.getToHeader(), "sut <sip:service@127.0.0.1:5090>;tag=16640SIPpTag0115");
        assertHeader(response.getCallIDHeader(), "1-16732@127.0.1.1");
        assertHeader(response.getCSeqHeader(), "1 INVITE");
        assertHeader(response.getHeader("X-Hello"), "World");
        assertThat(response.getContentLength(), is(0));


        // we registered a function for the Contact-header and in that we re-wrote
        // it. A common scenario for doing this is when you have an edge-proxy that
        // does topology hiding and even your internal nodes aren't aware so therefore
        // you will re-write their contact-addresses on the way out.
        // WARNING: you really never ever want to do this!!!
        assertAddressHeader(response.getContactHeader(), null, null, "12.13.14.15", "udp", 5678);

        // there were only a single Via on the original response
        // and we popped that one. In a real scenario, you wouldn't
        // want to end up in a situation where you have zero Via-headers
        // because then something would be wrong. However, there is nothing
        // that should prevent us from doing it.
        assertHeaderNotPresent(response.getViaHeader());
        assertHeaderNotPresent(response.getViaHeaders());
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

    @Test
    public void testCreateResponseFromRequestManyVias() throws Exception {
        final SipRequest req = parseMessage(RawData.sipInviteFourViaHeaders).toRequest();
        final SipResponse resp = req.createResponse(200)
                .onTopMostViaHeader(via -> via.withReceived("77.88.99.11").withRPort(9999))
                .build();
        assertResponseWithTheFourViaHeaders(resp);

        // now, we also have the response as generated by SIPp so
        // create a response from it, copy() it etc.
        final SipResponse sippResponse = parseMessage(RawData.twoHundredOkFourViaOnOneLine).toResponse();
        final SipResponse copy = sippResponse.copy()
                .onTopMostViaHeader(via -> via.withReceived("77.88.99.11").withRPort(9999))
                .build();
        assertResponseWithTheFourViaHeaders(copy);

        // pop the three first vias
        final SipResponse popEmAllAlmost = sippResponse.copy()
                .withPoppedVia()
                .withPoppedVia()
                .withPoppedVia()
                .onTopMostViaHeader(via -> via.withReceived("111.111.111.111").withRPort(1111))
                .build();

        assertViaHeader(popEmAllAlmost.getViaHeader(), "hello.com", "UDP", -1);
        assertThat(popEmAllAlmost.getViaHeader().getReceived().toString(), is("111.111.111.111"));
        assertThat(popEmAllAlmost.getViaHeader().getRPort(), is(1111));
        assertThat(popEmAllAlmost.getViaHeaders().size(), is(1));
    }

    private void assertResponseWithTheFourViaHeaders(final SipResponse resp) {
        assertThat(resp.getViaHeaders().size(), is(4));
        assertThat(resp.getViaHeaders().get(0), is(resp.getViaHeader()));
        assertViaHeader(resp.getViaHeader(), "127.0.1.1", "UDP", 5061);
        assertThat(resp.getViaHeader().getReceived().toString(), is("77.88.99.11"));
        assertThat(resp.getViaHeader().getRPort(), is(9999));
        assertHeader(resp.getViaHeaders().get(1), "SIP/2.0/WSS 12.13.14.15:443;branch=kjsa0-23-asdf-Aeas-D;rport=443;received=10.11.12.13");
        assertHeader(resp.getViaHeaders().get(2), "SIP/2.0/TLS 192.168.0.100:5061;branch=123-123-123-123");
        assertHeader(resp.getViaHeaders().get(3), "SIP/2.0/UDP hello.com;branch=asdf-asdf-123-123-abc;received=68.67.66.65");
    }
    /**
     * By default via headers are pushed onto the response simply because the most common use
     * case for a "real" sip stack is to indeed copy-paste the Via-headers from the
     * request onto the response so therefore this is the default behavior.
     *
     * That the via-header is actually pushed is already tested but a common scenario
     * is to also add parameters to the via so let's make sure that we can do that.
     *
     * @throws Exception
     */
    @Test
    public void testCreateResponseFromRequestAddViaHeaders() throws Exception {
        SipRequest req = (SipRequest) parseMessage(RawData.sipInviteOneRouteHeader);

        SipResponse resp = req.createResponse(200)
                .onTopMostViaHeader(via -> via.withReceived("65.66.67.68").withRPort(3456))
                .build();

        // ensure that the original Via didn't have any received or rport
        assertThat(req.getViaHeader().getReceived(), is((Buffer)null));
        assertThat(req.getViaHeader().getRPort(), is(-1));
        assertThat(req.getViaHeader().hasRPort(), is(false));

        assertThat(resp.getViaHeader().getReceived().toString(), is("65.66.67.68"));
        assertThat(resp.getViaHeader().getRPort(), is(3456));
        assertThat(resp.getViaHeader().hasRPort(), is(true));
        assertThat(resp.getViaHeaders().size(), is(1));

        // now, ensure that you can actually push more Via-headers
        // onto the response. Not something you normally would do
        // (you push on requests) but you should be allowed to.
        resp = req.createResponse(200)
                .withTopMostViaHeader(ViaHeader.withHost("12.23.34.45").withBranch(ViaHeader.generateBranch()).build())
                .onTopMostViaHeader(via -> via.withReceived("65.66.67.68").withRPort(3456))
                .build();

        assertThat(resp.getViaHeader().getReceived().toString(), is("65.66.67.68"));
        assertThat(resp.getViaHeader().getRPort(), is(3456));
        assertThat(resp.getViaHeader().hasRPort(), is(true));

        // and this is the Via that already was on the request and as such
        // would have been added to the response by default. Note that
        // we only manipulated the top-most via so this one should have
        // been left as is.
        assertThat(resp.getViaHeaders().size(), is(2));
        assertThat(resp.getViaHeaders().get(1).getValue(), is(req.getViaHeader().getValue()));
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
