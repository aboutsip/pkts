/**
 * 
 */
package io.pkts.packet.sip;

import io.pkts.PktsTestBase;
import io.pkts.RawData;
import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.address.SipURI;
import io.pkts.packet.sip.header.CSeqHeader;
import io.pkts.packet.sip.header.CallIdHeader;
import io.pkts.packet.sip.header.ContactHeader;
import io.pkts.packet.sip.header.FromHeader;
import io.pkts.packet.sip.header.MaxForwardsHeader;
import io.pkts.packet.sip.header.RecordRouteHeader;
import io.pkts.packet.sip.header.RouteHeader;
import io.pkts.packet.sip.header.SipHeader;
import io.pkts.packet.sip.header.ViaHeader;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author jonas@jonasborjesson.com
 */
public class SipRequestTest extends PktsTestBase {

    private FromHeader from;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.from = FromHeader.builder().withUser("bob").withHost("somewhere.com").build();
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
        assertThat(req.toString().contains("o=user1 53655765 2353687637 IN IP4 127.0.1.1"), is(true));
    }

    /**
     * Test for issue no 38: https://github.com/aboutsip/pkts/issues/38
     *
     * Conclusion, nothing wrong with that message. It is missing the branch parameter.
     *
     * @throws Exception
     */
    @Test
    public void testIssueNo38() throws Exception {
        try {
            loadSipMessage("subscribe.txt");
            fail("Expected the above message to fail");
        } catch (final SipParseException e) {
            // ensure it was the branch - perhaps not the best way to test
            // but currently there are not specif exceptions for every little
            // parsing detail that can go wrong.
            assertThat(e.getMessage(), is("You must specify a branch parameter"));
        }
    }

    /**
     * Test for issue no 106: https://github.com/aboutsip/pkts/issues/106
     *
     * @throws Exception
     */
    @Test
    public void testIssueNo106() throws Exception {
        assertThat(loadSipMessage("issue-106_001.txt").getFromHeader().getTag(), is((Buffer) null));
        assertThat(loadSipMessage("issue-106_002.txt").getFromHeader().getTag(), is((Buffer) null));
        assertThat(loadSipMessage("issue-106_003.txt").getFromHeader().getTag(), is((Buffer) null));
        assertThat(loadSipMessage("issue-106_004.txt").getFromHeader().getTag(), is((Buffer) null));
        assertThat(loadSipMessage("issue-106_005.txt").getFromHeader().getTag(), is((Buffer) null));
        assertThat(loadSipMessage("issue-106_006.txt").getFromHeader().getTag(), is((Buffer) null));
        assertThat(loadSipMessage("issue-106_007.txt").getFromHeader().getTag(), is((Buffer) null));
        assertThat(loadSipMessage("issue-106_008.txt").getFromHeader().getTag(), is((Buffer) null));
        assertThat(loadSipMessage("issue-106_009.txt").getFromHeader().getTag(), is((Buffer) null));
        assertThat(loadSipMessage("issue-106_010.txt").getFromHeader().getTag(), is((Buffer) null));
        assertThat(loadSipMessage("issue-106_011.txt").getFromHeader().getTag(), is(Buffers.EMPTY_BUFFER));
        assertThat(loadSipMessage("issue-106_012.txt").getFromHeader().getTag(), is(Buffers.EMPTY_BUFFER));

        assertFromHeaderTag("sip:blah@example.com;tag=", "");
        assertFromHeaderTag("sip:blah@example.com;tag   =        ", "");
        assertFromHeaderTag("<sip:blah@example.com;tag>", null);

        // at the end of the day, the issue was that when consuming the address-spec, which
        // does not actually allow a space between the uri and the potential params, which then
        // created a bad input to ParametersSupport, which then never progressed when asking
        // for parameters that didn't exist (it got stuck on the ">", which should not have been
        // passed to the {@link ParametersSupport} in the first place.
        assertFromHeaderTag("<sip:blah@example.com;nisse= asdf>", null);
        assertFromHeaderTag("<sip:blah@example.com;nisse =asdf>", null);
        assertFromHeaderTag("<sip:blah@example.com;nisse =   asdf>", null);
        assertFromHeaderTag("<sip:blah@example.com; asdf>", null);
        assertFromHeaderTag("<sip:blah@example.com;\tasdf>", null);
        assertFromHeaderTag("<sip:blah@example.com; >", null);
    }

    private static void assertFromHeaderTag(final String from, final String expected) throws SipParseException {
        final Buffer expectedBuffer = expected == null ? null : Buffers.wrap(expected);
        assertThat(FromHeader.frame(from).getTag(), is(expectedBuffer));
    }

    /**
     * You have the ability to specify an empty Via-header since you may not have all the information
     * at hand right now so you rather fill out the Via through the
     * {@link SipMessage.Builder#onTopMostViaHeader(Consumer)} but if you don't register that function
     * we should blow up on a {@link SipParseException}.
     *
     * @throws Exception
     */
    @Test(expected = SipParseException.class )
    public void testBlowUpOnNoFunctionToHandleEmptyTopMostVia() throws Exception {
        parseMessage(RawData.sipInviteOneRecordRouteHeader)
                .copy()
                .withTopMostViaHeader()
                .build();
    }

    /**
     * Same as the {@link SipRequestTest#testBlowUpOnNoFunctionToHandleEmptyTopMostVia()} but we
     * can also register an empty Via-header for the "non top-most" case by
     * calling {@link SipMessage.Builder#withTopMostViaHeader(ViaHeader)}
     * several times.
     *
     * @throws Exception
     */
    @Test(expected = SipParseException.class )
    public void testBlowUpOnNoFunctionToHandleEmptyVia() throws Exception {
        parseMessage(RawData.sipInviteOneRecordRouteHeader)
                .copy()
                .withTopMostViaHeader()
                .withTopMostViaHeader(ViaHeader.withHost("10.11.12.13").withBranch().build())
                .build();
    }

    /**
     * A common scenario for any real application is to create a b2bua application
     * which receives a request, creates a completely new request using the received
     * request as a template but will have its own dialog etc.
     *
     * Test that we can create such a b2bua-request.
     *
     * @throws Exception
     */
    @Test
    public void testCreateB2BUARequest() throws Exception {
        final SipRequest req = parseMessage(RawData.sipInviteThreeRouteHeaders).toRequest();

        final SipRequest b2bua = req.copy()
                .withTopMostViaHeader()
                .onTopMostViaHeader(via -> via.withHost("12.34.56.78").withBranch().withTransportWSS().withPort(443))
                .withTopMostRecordRouteHeader(RecordRouteHeader.withHost("12.34.56.78").withTransportWS().build())
                .withNoRoutes() // wipe out any routes that existed on the incoming request. We don't trust them
                .withRouteHeader(RouteHeader.withHost("192.168.0.100").withPort(5070).withTransportUDP().build())
                .withCallIdHeader(CallIdHeader.create())
                .onFromHeader(from -> from.withDefaultTag()) // set a new tag since this is a b2bua request
                .onToHeader(to -> to.withNoTag()) // ensure there is no tag param on the To.
                .build();

        // Ensure that the from-tags now are different and that the two are actually
        // set in the first place.
        assertThat(req.getFromHeader().getTag(), not((Buffer)null));
        assertThat(b2bua.getFromHeader().getTag(), not((Buffer)null));
        assertThat(req.getFromHeader().getTag(), not(b2bua.getFromHeader().getTag()));

        // ensure the Vias are correct. We pushed one, which should be at the top
        // and the one that came in on the original request should be un-touched.
        assertThat(b2bua.getViaHeaders().size(), is(2));
        assertViaHeader(b2bua.getViaHeader(), "12.34.56.78", "wss", 443);
        assertHeader(b2bua.getViaHeaders().get(1), "SIP/2.0/UDP 192.168.8.110:5060;branch=z9hG4bK-18844-1-0");

        // the incoming request had three route headers already but we wiped
        // them out and only added one of our own...
        assertThat(b2bua.getRouteHeaders().size(), is(1));
        assertHeader(b2bua.getRouteHeader(), "<sip:192.168.0.100:5070;transport=udp>");

        // make sure the body is left intact as well.
        final String expectedContent =
                "v=0\r\n"
                + "o=user1 53655765 2353687637 IN IP4 192.168.8.110\r\n"
                + "s=-\r\n"
                + "c=IN IP4 192.168.8.110\r\n"
                + "t=0 0\r\n"
                + "m=audio 6000 RTP/AVP 0\r\n"
                + "a=rtpmap:0 PCMU/8000\r\n";

        assertThat(b2bua.getContent().toString(), is(expectedContent));

        // and make sure that it actually shows up when we write the message
        // to stream
        assertThat("The actual body was not preserved in the raw message output",
                b2bua.toString().contains(expectedContent), is(true));

    }

    /**
     * A common scenario for any real stack is to proxy a request, which
     * then typically involves pushing a RR header, adding a Via
     * and perhaps push some other headers onto the request to be
     * proxied. Ensure we can do that...
     *
     * @throws Exception
     */
    @Test
    public void testCreateRequestToProxy() throws Exception {
        // this would be the request that came into the stack and that
        // we are to proxy. Just adding another X-header because we are
        // filtering it out further down as part of the test.
        final SipRequest req = parseMessage(RawData.sipInviteTwoRouteHeaders).copy()
                .withHeader(SipHeader.create("X-Hello", "world")).build().toRequest();
        assertHeader(req.getHeader("X-Hello"), "world");

        // The request we are going to proxy have the following properties:
        // 1. We will add some very important X-Company header, a common thing for many proxies
        // 2. We will push a Record-Route header
        // 3. We will push a Via-header
        // 4. We have to drop the top-most Route header since otherwise the request will at some point
        //    loop back to us. Of course, a real stack should also examine that this Route is indeed
        //    pointing to us before dropping it.
        // 5. We will push two Route headers. The top most will hit our ingress load balancer using TLS
        //    and its address is "first.lb.to.hit.company.com". The second route is slightly
        //    odd to push since you would expect the first load balancer to do this but this is just
        //    an example so whatever, but this second route is hitting "some.internal.node.to.hit.company.com"
        //    using UDP.
        // 6. We will also drop any other X-headers that are NOT X-Company ones.
        // 7. We will register a function for the top-most via header because once the request is
        //    being built we want to take this opportunity to change some of its values. Typically,
        //    you would do this e.g. in the transport layer of your stack when you actually do
        //    know which transport has been selected for the next hop as well as the interface
        //    (for multi-homed stacks) that is going to be used.
        // 8. We also need to set the received and rport value of the previous top-most Via-header
        //    so we will register a second function for the "second most" via.
        // 9. We will also register a function for the top-most route header for the very same reasons
        //    as for the Via-header.
        // 10. Finally, build it!
        final SipRequest proxy = req.copy()
                .onRequestURI(uri -> uri.copy().withHost("siplib.io").withUser("Kalle").build())
                .withPoppedRoute()
                .withTopMostViaHeader(ViaHeader.withHost("12.13.14.15").withBranch().build())
                .withHeader(SipHeader.create("X-Company-Foo", "Important value"))
                .withHeader(SipHeader.create("X-Company-Foo", "Also important"))
                .withTopMostRecordRouteHeader(RecordRouteHeader.withHost("12.13.14.15").build())
                .withTopMostRouteHeader(RouteHeader.withHost("some.internal.node.to.hit.company.com").withPort(5060).withTransportTLS().build())
                .withTopMostRouteHeader(RouteHeader.withHost("first.lb.to.hit.company.com").withPort(5060).withTransportTLS().build())
                .onHeader(h -> {
                    // drop all X-headers that isn't X-Company
                    final String name = h.getNameStr();
                    if (name.startsWith("X-") && !name.startsWith("X-Company")) {
                        return null;
                    }

                    // the rest we will keep so just return as is
                    return h;
                })
                .onTopMostViaHeader(via -> via.withTransportTCP().withParameter("x-connection-id", "asdf-123-asdf"))
                .onViaHeader((index, via) -> {
                    // for the second Via-header, which is the Via that we received
                    // this request from, set the received and rport parameter
                    if (index == 1) {
                        via.withReceived("100.110.120.130").withRPort(34567);
                    }
                })
                .onTopMostRouteHeader(route -> route.withTransportTCP())
                .build();

        // So, verify all of the above.

        // We changed the Request-URI:
        final SipURI uri = proxy.getRequestUri().toSipURI();
        assertThat(uri.getUser().get().toString(), is("Kalle"));
        assertThat(uri.getHost().toString(), is("siplib.io"));

        // There should be two via headers. One from the incoming request
        // and one that we pushed. The one we pushed should be at the top.
        // Also ensure that the first via on the list of vias is the same
        // as what you would get by getting the "top most via".
        final List<ViaHeader> vias = proxy.getViaHeaders();
        assertThat(vias.size(), is(2));
        assertThat(vias.get(0), is(proxy.getViaHeader())); // top-most and first on list should be the same

        final ViaHeader topVia = proxy.getViaHeader();
        assertThat(topVia.getParameter("x-connection-id").toString(), is("asdf-123-asdf"));
        assertThat(topVia.isTCP(), is(true));
        assertThat(topVia.getHost().toString(), is("12.13.14.15"));

        // the second via should have our received and rport params
        final ViaHeader secondVia = vias.get(1);
        assertThat(secondVia.getHost().toString(), is("192.168.8.110"));
        assertThat(secondVia.getPort(), is(5060));
        assertThat(secondVia.getBranch().toString(), is("z9hG4bK-18116-1-0"));
        assertThat(secondVia.getRPort(), is(34567));
        assertThat(secondVia.getReceived().toString(), is("100.110.120.130"));

        // Route-headers - there should be 3 in total. There were two on the "incoming"
        // request, one we should check and pop, we then pushed two routes of our own.
        final List<RouteHeader> routes = proxy.getRouteHeaders();
        assertThat(routes.size(), is(3));

        // because we re-wrote the transport with the onTopMostRouteHeader function
        assertAddressHeader(routes.get(0), null, null, "first.lb.to.hit.company.com", "tcp", 5060);
        assertAddressHeader(routes.get(1), null, null, "some.internal.node.to.hit.company.com", "tls", 5060);
        assertAddressHeader(routes.get(2), null, "two", "aboutsip.com", "tcp", -1);

        // we pushed one Record-Route header and there were
        // none from the beginning
        assertThat(proxy.getRecordRouteHeaders().size(), is(1));
        assertAddressHeader(proxy.getRecordRouteHeader(), null, null, "12.13.14.15", null, -1);

        // assert that we removed the X-Hello header and that there are two
        // X-Company headers
        assertHeaderNotPresent(proxy.getHeader("X-Hello"));
        final List<SipHeader> xCompanyHeaders = proxy.getHeaders("X-Company-Foo");
        assertThat(xCompanyHeaders.size(), is(2));
        assertHeader(xCompanyHeaders.get(0), "Important value");
        assertHeader(xCompanyHeaders.get(1), "Also important");
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

    /**
     * Test to create a new INVITE request and check all the headers that are supposed to be created
     * by default when not specified indeed are created with the correct values.
     * 
     * @throws Exception
     */
    @Test
    public void testCreateInvite() throws Exception {
        final SipRequest invite = SipRequest.invite("sip:alice@example.com").withFromHeader(this.from).build();
        assertThat(invite.getToHeader().toString(), is("To: sip:alice@example.com"));

        final CSeqHeader cseq = invite.getCSeqHeader();
        assertThat(cseq.getSeqNumber(), is(0L));
        assertThat(cseq.getMethod().toString(), is("INVITE"));

        final CallIdHeader callId = invite.getCallIDHeader();
        assertThat(callId, not((CallIdHeader) null));

        final MaxForwardsHeader max = invite.getMaxForwards();
        assertThat(max.getMaxForwards(), is(70));

        assertThat(invite.getFromHeader().toString(), is("From: sip:bob@somewhere.com"));
    }

    /**
     * Although not mandatory from the builder's perspective, having a request without a
     * {@link ContactHeader} is pretty much useless so make sure that we can add that as well.
     * 
     * @throws Exception
     */
    @Test
    public void testCreateInviteWithContactHeader() throws Exception {
        final ContactHeader contact = ContactHeader.with().withHost("12.13.14.15").withPort(1234).withTransportTCP().build();
        final SipRequest invite = SipRequest.invite("sip:alice@example.com").withFromHeader(this.from).withContactHeader(contact).build();
        final SipURI contactURI = (SipURI) invite.getContactHeader().getAddress().getURI();
        assertThat(contactURI.getPort(), is(1234));
        assertThat(contactURI.getHost().toString(), is("12.13.14.15"));
        assertThat(contact.getValue().toString(), is("<sip:12.13.14.15:1234;transport=tcp>"));
    }

    @Test
    public void testCreateInviteWithViaHeaders() throws Exception {
        final ViaHeader via =
                ViaHeader.withHost("127.0.0.1").withPort(9898).withTransportUDP().withBranch(ViaHeader.generateBranch()).build();
        SipRequest invite = SipRequest.invite("sip:alice@example.com").withFromHeader(this.from).withViaHeader(via).build();

        // since there is only one via header, getting the "top-most" via header should
        // be the same as getting the first via off of the list.
        assertThat(invite.getViaHeaders().size(), is(1));
        assertThat(
                invite.getViaHeaders().get(0).toString()
                .startsWith("Via: SIP/2.0/UDP 127.0.0.1:9898;branch=z9hG4bK"), is(true));
        assertThat(invite.getViaHeader().toString().startsWith("Via: SIP/2.0/UDP 127.0.0.1:9898;branch=z9hG4bK"),
                is(true));

        // two via headers
        final ViaHeader via2 =
                ViaHeader.withHost("192.168.0.100").withTransportTCP().withBranch(ViaHeader.generateBranch()).build();
        invite = SipRequest.invite("sip:alice@example.com").withFromHeader(this.from).withViaHeaders(via, via2).build();
        assertThat(invite.getViaHeaders().size(), is(2));

        // the top-most via should be the one we added first.
        assertThat(invite.getViaHeader().toString().startsWith("Via: SIP/2.0/UDP 127.0.0.1:9898;branch=z9hG4bK"),
                is(true));

        assertThat(
                invite.getViaHeaders().get(1).toString().startsWith("Via: SIP/2.0/TCP 192.168.0.100;branch=z9hG4bK"),
                is(true));
    }

    /**
     * Ensure that two copies are not actually affecting each other since if they did
     * we wouldn't have a immutability.
     *
     * There was an issue with the internal list of parameters in the Via-header where the
     * reference of the list was copied, which then obviously was shared between
     * instances...
     *
     * @throws Exception
     */
    @Test
    public void testCreateTwoCopies() throws Exception {
        final SipRequest invite = parseMessage(RawData.sipInviteOneRecordRouteHeader).toRequest();
        final SipRequest withRport = invite.copy().onTopMostViaHeader(v -> v.withRPortFlag()).build();

        final ViaHeader via = withRport.getViaHeader();
        assertThat(via.hasRPort(), is(true));
        assertThat(via.getRPort(), is(-1));
        assertThat(via.toString().contains("rport"), is(true));

        final SipRequest noRport = invite.copy().onTopMostViaHeader(v -> v.withHost("hello.com").withParameter("hello", "world")).build();
        final ViaHeader via2 = noRport.getViaHeader();
        assertThat(via2.hasRPort(), is(false));
        assertThat(via2.getRPort(), is(-1));
        assertThat(via2.toString().contains("rport"), is(false));

    }

    /**
     * Tests that the parser handles a packet that's prematurely truncated.
     *
     * @throws Exception
     */
    @Test
    public void testTruncatedPacket() throws Exception {
        final int truncatedLength = RawData.sipInvite.length - 16;
        final byte[] truncatedPacket = new byte[truncatedLength];
        System.arraycopy(RawData.sipInvite, 0, truncatedPacket, 0, truncatedLength);
        final SipRequest req = (SipRequest) parseMessage(truncatedPacket);
        assertThat(req.toString().contains("o=user1 53655765 2353687637 IN IP4 127.0.1.1"), is(true));
    }

}
