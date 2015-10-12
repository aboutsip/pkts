/**
 * 
 */
package io.pkts.packet.sip;

import io.pkts.PktsTestBase;
import io.pkts.RawData;
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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

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
        SipRequest proxy = req.copy()
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
                ViaHeader.withHost("127.0.0.1").withPort(9898).withTransportUdp().withBranch(ViaHeader.generateBranch()).build();
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

}
