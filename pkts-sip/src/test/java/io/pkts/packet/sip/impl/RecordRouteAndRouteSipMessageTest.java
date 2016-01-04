package io.pkts.packet.sip.impl;

import io.pkts.PktsTestBase;
import io.pkts.RawData;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipMessage;
import io.pkts.packet.sip.address.SipURI;
import io.pkts.packet.sip.address.URI;
import io.pkts.packet.sip.header.RecordRouteHeader;
import io.pkts.packet.sip.header.RouteHeader;
import io.pkts.packet.sip.header.SipHeader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

/**
 * These set of tests focuses on the handling of Route and Record-Route
 * headers. They, like the Via-header, has a very special meaning
 * within SIP and it is important to e.g. preserve their order
 * etc.
 *
 * The first set of tests are focusing on just parsing messages
 * but the second half is focusing on manipulating SIP messages
 * in various ways in relation to Route & Record-Route headers.
 */
public class RecordRouteAndRouteSipMessageTest extends PktsTestBase {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * If there are no route headers present, we should return an empty list.
     * Make sure we do!
     *
     * @throws Exception
     */
    @Test
    public void testGetRouteHeadersNoRoute() throws Exception {
        final SipMessage msg = parseMessage(RawData.sipInvite);
        assertThat(msg.getRouteHeader(), is((RouteHeader) null));
        assertThat(msg.getRouteHeaders().size(), is(0));
    }

    /**
     * Make sure we can extract out all Route-headers as expected.
     *
     * @throws Exception
     */
    @Test
    public void testGetRouteHeadersOneRoute() throws Exception {
        SipMessage msg = parseMessage(RawData.sipInviteOneRouteHeader);
        final RouteHeader route = msg.getRouteHeader();
        assertRouteHeader(route, "one", "aboutsip.com", "<sip:one@aboutsip.com;transport=udp>");

        msg = parseMessage(RawData.sipInviteOneRouteHeader);
        List<RouteHeader> routes = msg.getRouteHeaders();
        assertThat(routes.size(), is(1));
        assertRouteHeader(routes.get(0), "one", "aboutsip.com", "<sip:one@aboutsip.com;transport=udp>");

        msg = msg.copy().build();
        routes = msg.getRouteHeaders();
        assertThat(routes.size(), is(1));
        assertRouteHeader(routes.get(0), "one", "aboutsip.com", "<sip:one@aboutsip.com;transport=udp>");
    }

    /**
     * Make sure we can extract two route headers.
     *
     * @throws Exception
     */
    @Test
    public void testGetRouteHeadersTwoRoutes() throws Exception {
        SipMessage msg = parseMessage(RawData.sipInviteTwoRouteHeaders);

        final RouteHeader route = msg.getRouteHeader();
        assertRouteHeader(route, "one", "aboutsip.com", "<sip:one@aboutsip.com;transport=udp>");

        List<RouteHeader> routes = msg.getRouteHeaders();
        assertThat(routes.size(), is(2));
        assertRouteHeader(routes.get(0), "one", "aboutsip.com", "<sip:one@aboutsip.com;transport=udp>");
        assertRouteHeader(routes.get(1), "two", "aboutsip.com", "<sip:two@aboutsip.com;transport=tcp>");

        // also make sure that we get the same result after we have asked for all routes up front
        msg = parseMessage(RawData.sipInviteTwoRouteHeaders);
        routes = msg.getRouteHeaders();
        assertThat(routes.size(), is(2));
        assertRouteHeader(routes.get(0), "one", "aboutsip.com", "<sip:one@aboutsip.com;transport=udp>");
        assertRouteHeader(routes.get(1), "two", "aboutsip.com", "<sip:two@aboutsip.com;transport=tcp>");

        // also make sure they they all appear again after we have copied it.
        msg = msg.copy().build();
        routes = msg.getRouteHeaders();
        assertThat(routes.size(), is(2));
        assertRouteHeader(routes.get(0), "one", "aboutsip.com", "<sip:one@aboutsip.com;transport=udp>");
        assertRouteHeader(routes.get(1), "two", "aboutsip.com", "<sip:two@aboutsip.com;transport=tcp>");
    }

    /**
     * Make sure we can extract three route headers and where the 3rd header is
     * NOT directly following the other two headers.
     *
     * @throws Exception
     */
    @Test
    public void testGetRouteHeadersThreeRoutes() throws Exception {
        SipMessage msg = parseMessage(RawData.sipInviteThreeRouteHeaders);

        final RouteHeader route = msg.getRouteHeader();
        assertRouteHeader(route, "one", "aboutsip.com", "<sip:one@aboutsip.com;transport=udp>");

        List<RouteHeader> routes = msg.getRouteHeaders();
        assertThat(routes.size(), is(3));
        assertRouteHeader(routes.get(0), "one", "aboutsip.com", "<sip:one@aboutsip.com;transport=udp>");
        assertRouteHeader(routes.get(1), "two", "aboutsip.com", "<sip:two@aboutsip.com;transport=tcp>");
        assertRouteHeader(routes.get(2), "three", "aboutsip.com", "<sip:three@aboutsip.com;transport=tcp>");

        // also make sure they they all appear again after we have copied it.
        msg = msg.copy().build();
        routes = msg.getRouteHeaders();
        assertThat(routes.size(), is(3));
        assertRouteHeader(routes.get(0), "one", "aboutsip.com", "<sip:one@aboutsip.com;transport=udp>");
        assertRouteHeader(routes.get(1), "two", "aboutsip.com", "<sip:two@aboutsip.com;transport=tcp>");
        assertRouteHeader(routes.get(2), "three", "aboutsip.com", "<sip:three@aboutsip.com;transport=tcp>");
    }

    private void assertRouteHeader(final RouteHeader route, final String user, final String host,
            final String headerValue) {
        assertThat(((SipURI) route.getAddress().getURI()).getHost().toString(), is(host));
        assertThat(((SipURI) route.getAddress().getURI()).getUser().orElse(Buffers.EMPTY_BUFFER), is(Buffers.wrap(user)));
        assertThat(route.getValue().toString(), is(headerValue));
        assertThat(route.toString(), is(RouteHeader.NAME + ": " + headerValue));
    }

    /**
     * Record-Route headers are typically handled a little differently since
     * they actually are ordered. These tests focuses on making sure that we
     * maintain the order of the RR headers as found in the original request.
     *
     * @throws Exception
     */
    @Test
    public void testGetRecordRouteHeaders() throws Exception {
        final SipMessage msg = parseMessage(RawData.sipInviteOneRecordRouteHeader);
        final RecordRouteHeader rr = msg.getRecordRouteHeader();
        assertThat(rr, not((RecordRouteHeader) null));
        assertThat(rr.toString(), is("Record-Route: <sip:one@aboutsip.com;transport=udp>"));
        assertThat(rr.getValue().toString(), is("<sip:one@aboutsip.com;transport=udp>"));
        assertThat(rr.getAddress().getDisplayName().isEmpty(), is(true));
        final URI uri = rr.getAddress().getURI();
        assertThat(uri.isSipURI(), is(true));
        final SipURI sipUri = (SipURI) uri;
        assertThat(sipUri.getHost().toString(), is("aboutsip.com"));
    }

    /**
     * Make sure that we can parse multiple Record-Route headers.
     *
     * @throws Exception
     */
    @Test
    public void testGetRecordRouteHeadersThreeRRs() throws Exception {
        SipMessage msg = parseMessage(RawData.sipInviteThreeRecordRoutes);
        List<RecordRouteHeader> headers = msg.getRecordRouteHeaders();
        assertThat(headers.size(), is(3));
        assertRecordRouteHeader(headers.get(0), "one", "aboutsip.com", "<sip:one@aboutsip.com;transport=udp>");
        assertRecordRouteHeader(headers.get(1), "two", "aboutsip.com", "<sip:two@aboutsip.com;transport=tcp>");
        assertRecordRouteHeader(headers.get(2), "three", "aboutsip.com", "<sip:three@aboutsip.com;transport=tcp>");

        // ensure that if we just clone the message that the headers are still in there...
        msg = msg.copy().build();
        headers = msg.getRecordRouteHeaders();
        assertThat(headers.size(), is(3));
        assertRecordRouteHeader(headers.get(0), "one", "aboutsip.com", "<sip:one@aboutsip.com;transport=udp>");
        assertRecordRouteHeader(headers.get(1), "two", "aboutsip.com", "<sip:two@aboutsip.com;transport=tcp>");
        assertRecordRouteHeader(headers.get(2), "three", "aboutsip.com", "<sip:three@aboutsip.com;transport=tcp>");
    }

    private void assertRecordRouteHeader(final RecordRouteHeader route, final String user, final String host,
            final String headerValue) {
        assertThat(((SipURI) route.getAddress().getURI()).getHost().toString(), is(host));
        assertThat(((SipURI) route.getAddress().getURI()).getUser().orElse(Buffers.EMPTY_BUFFER), is(Buffers.wrap(user)));
        assertThat(route.getValue().toString(), is(headerValue));
        assertThat(route.toString(), is(RecordRouteHeader.NAME + ": " + headerValue));
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void testSpecifyRecordRouteHeader() throws Exception {
        SipMessage msg = parseMessage(RawData.sipInviteOneRecordRouteHeader);
        assertThat(msg.getRecordRouteHeader().getValue().toString(), is("<sip:one@aboutsip.com;transport=udp>"));

        msg = msg.copy().withRecordRouteHeader(RecordRouteHeader.withHost("pkts.io").withPort(4567).build()).build();
        assertThat(msg.getRecordRouteHeader().getValue().toString(), is("sip:pkts.io:4567"));

        // also make sure you can still manipulate it
        msg = msg.copy()
                .withRecordRouteHeader(RecordRouteHeader.withHost("aboutsip.com").withUser("hello").build())
                .onTopMostRecordRouteHeader(f -> f.withPort(6789).withDisplayName("Alice"))
                .build();
        assertThat(msg.getRecordRouteHeader().getValue().toString(), is("Alice <sip:hello@aboutsip.com:6789>"));

        // you should be able to specify any System header through the withHeader-generic way.
        // Note, when doing so the header just added to the list of headers, which in the
        // case for RR headers means that it will be appended last to the list of
        // already existing headers...
        msg = msg.copy()
                .withHeader(SipHeader.create("Record-Route", "<sip:bob@foo.com:8765>"))
                .onTopMostRecordRouteHeader(f -> f.withDisplayName("BOB"))
                .onRecordRouteHeader(f -> f.withDisplayName("BOB"))
                .build();
        assertThat(msg.getRecordRouteHeaders().get(0).getValue().toString(), is("BOB <sip:hello@aboutsip.com:6789>"));
        assertThat(msg.getRecordRouteHeaders().get(1).getValue().toString(), is("BOB <sip:bob@foo.com:8765>"));
    }

    /**
     * When the original template contains many Record-Route headers we have to
     * watch out so we either keep them all or wipe them all out depending on
     * whether we use {@link SipMessageBuilder#withTopMostRecordRouteHeader(RecordRouteHeader)}
     * or {@link SipMessageBuilder#withRecordRouteHeader(RecordRouteHeader)}.
     *
     * @throws Exception
     */
    @Test
    public void testManipulateRecordRouteHeaders() throws Exception {
        SipMessage msg = parseMessage(RawData.sipInviteThreeRecordRoutes);
        assertThat(msg.getRecordRouteHeaders().size(), is(3));

        // when specifying a RR header through withRecordRouteHeader then all
        // existing headers should be wiped out
        msg = msg.copy().withRecordRouteHeader(RecordRouteHeader.withHost("pkts.io").build()).build();
        assertThat(msg.getRecordRouteHeaders().size(), is(1));
        assertThat(msg.getRecordRouteHeader().getValue().toString(), is("sip:pkts.io"));

        // but when we use the push method then we should preserve any
        // already existing RR headers and add the pushed one to the top...
        msg = parseMessage(RawData.sipInviteThreeRecordRoutes).copy()
                .withTopMostRecordRouteHeader(RecordRouteHeader.withHost("pkts.io").build()).build();
        // top most one should be the one we pushed.
        assertThat(msg.getRecordRouteHeader().getValue().toString(), is("sip:pkts.io"));

        // should get 4 out, which should include the old ones too...
        List<RecordRouteHeader> rrs = msg.getRecordRouteHeaders();
        assertThat(rrs.size(), is(4));
        assertRecordRouteHeader(rrs.get(0), "", "pkts.io", "sip:pkts.io");
        assertRecordRouteHeader(rrs.get(1), "one", "aboutsip.com", "<sip:one@aboutsip.com;transport=udp>");
        assertRecordRouteHeader(rrs.get(2), "two", "aboutsip.com", "<sip:two@aboutsip.com;transport=tcp>");
        assertRecordRouteHeader(rrs.get(3), "three", "aboutsip.com", "<sip:three@aboutsip.com;transport=tcp>");

        // push two more... note that Bob1 should appear below Bob0 since Bob0 was pushed after Bob1
        // and as such ends up at the top. Therefore, the onTopMostRecordRoute header function should
        // be called on Bob0 only... check these by registering a function for the RR headers which
        // manipulates the headers further...
        msg = msg.copy()
                .onTopMostRecordRouteHeader(rr -> rr.withUser("bob0-manipulated"))
                .onRecordRouteHeader(rr -> rr.withParameter("not-on-top", "true"))
                .withTopMostRecordRouteHeader(RecordRouteHeader.withUser("bob1").withHost("pkts.io").build())
                .withTopMostRecordRouteHeader(RecordRouteHeader.withUser("bob0").withHost("pkts.io").build()).build();

        rrs = msg.getRecordRouteHeaders();
        assertThat(rrs.size(), is(6));
        assertRecordRouteHeader(rrs.get(0), "bob0-manipulated", "pkts.io", "sip:bob0-manipulated@pkts.io");
        assertRecordRouteHeader(rrs.get(1), "bob1", "pkts.io", "sip:bob1@pkts.io;not-on-top=true");
        assertRecordRouteHeader(rrs.get(2), "", "pkts.io", "sip:pkts.io;not-on-top=true");
        assertRecordRouteHeader(rrs.get(3), "one", "aboutsip.com", "<sip:one@aboutsip.com;transport=udp>;not-on-top=true");
        assertRecordRouteHeader(rrs.get(4), "two", "aboutsip.com", "<sip:two@aboutsip.com;transport=tcp>;not-on-top=true");
        assertRecordRouteHeader(rrs.get(5), "three", "aboutsip.com", "<sip:three@aboutsip.com;transport=tcp>;not-on-top=true");

    }

}
