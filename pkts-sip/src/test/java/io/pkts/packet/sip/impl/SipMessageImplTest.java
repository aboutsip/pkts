package io.pkts.packet.sip.impl;

import io.pkts.PktsTestBase;
import io.pkts.RawData;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipMessage;
import io.pkts.packet.sip.SipRequest;
import io.pkts.packet.sip.address.SipURI;
import io.pkts.packet.sip.address.URI;
import io.pkts.packet.sip.header.ContentTypeHeader;
import io.pkts.packet.sip.header.ExpiresHeader;
import io.pkts.packet.sip.header.RecordRouteHeader;
import io.pkts.packet.sip.header.RouteHeader;
import io.pkts.packet.sip.header.SipHeader;
import io.pkts.packet.sip.header.ViaHeader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

public class SipMessageImplTest extends PktsTestBase {

    private SipRequest request;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        // the boundaries between headers and payload has been checked
        // with wireshark...
        // final Buffer line = this.sipFrameBuffer.readLine();
        // final Buffer headers = this.sipFrameBuffer.readBytes(331);
        // final Buffer payload = this.sipFrameBuffer.slice();

        // final SipInitialLine initialLine = SipInitialLine.parse(line);
        // assertThat(initialLine.isRequestLine(), is(true));

        this.request = (SipRequest) parseMessage(this.sipFrameBuffer);
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
        final List<RouteHeader> routes = msg.getRouteHeaders();
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
    }

    /*
    public void testNewAPI() throws Exception {
        final SipMessage msg = parseMessage(RawData.sipInviteThreeRouteHeaders);
        // msg.copy().headerStream().filter(h -> h.name.equals(X-Twilio)).map(h.builder()).collect().stream().h.chan

        msg.copy().stream().onRequestURI(r -> r.setUserParam("nisse")).onHeaders(h -> {
            if (h.isVia()) {
                return h.copy().withBlah.withPort();
            }
            return h;
        }).onViaIsAboutToGetConstructed(viaBuilder -> viaBuilder.setParam("nisse", "apa")).onFrom().onTo().onContact().onRoutes().onMyRoute().onTopVia().build();`
    }
    */

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

        // also make sure that we get the same result after we have asked for all routes up front
        msg = parseMessage(RawData.sipInviteThreeRouteHeaders);
        routes = msg.getRouteHeaders();
        assertThat(routes.size(), is(3));
        assertRouteHeader(routes.get(0), "one", "aboutsip.com", "<sip:one@aboutsip.com;transport=udp>");
        assertRouteHeader(routes.get(1), "two", "aboutsip.com", "<sip:two@aboutsip.com;transport=tcp>");
        assertRouteHeader(routes.get(2), "three", "aboutsip.com", "<sip:three@aboutsip.com;transport=tcp>");
    }

    private void assertRouteHeader(final RouteHeader route, final String user, final String host,
            final String headerValue) {
        assertThat(((SipURI) route.getAddress().getURI()).getHost().toString(), is(host));
        assertThat(((SipURI) route.getAddress().getURI()).getUser().toString(), is(user));
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
        final SipMessage msg = parseMessage(RawData.sipInviteThreeRecordRoutes);
        final List<RecordRouteHeader> headers = msg.getRecordRouteHeaders();
        assertThat(headers.size(), is(3));
        assertRecordRouteHeader(headers.get(0), "one", "aboutsip.com", "<sip:one@aboutsip.com;transport=udp>");
        assertRecordRouteHeader(headers.get(1), "two", "aboutsip.com", "<sip:two@aboutsip.com;transport=tcp>");
        assertRecordRouteHeader(headers.get(2), "three", "aboutsip.com", "<sip:three@aboutsip.com;transport=tcp>");
    }

    private void assertRecordRouteHeader(final RecordRouteHeader route, final String user, final String host,
            final String headerValue) {
        assertThat(((SipURI) route.getAddress().getURI()).getHost().toString(), is(host));
        assertThat(((SipURI) route.getAddress().getURI()).getUser().toString(), is(user));
        assertThat(route.getValue().toString(), is(headerValue));
        assertThat(route.toString(), is(RecordRouteHeader.NAME + ": " + headerValue));
    }

    @Test
    public void testSetMaxForwardsHeader() throws Exception {
        SipMessage msg = parseMessage(RawData.sipInviteOneRecordRouteHeader);
        assertThat(msg.toString().contains("Max-Forwards: 70"), is(true));

        System.out.println(msg);
        System.out.println("======================");

        msg = msg.copy().onMaxForwardsHeader(max -> max.copy().withValue(55)).build();
        System.out.println(msg);
        System.out.println("======================");
        assertThat(msg.toString().contains("Max-Forwards: 55"), is(true));

        msg = msg.copy().onMaxForwardsHeader(max -> max.copy().withValue(32)).build();
        assertThat(msg.toString().contains("Max-Forwards: 32"), is(true));
    }

    @Test
    public void testGetViaHeader() throws Exception {
        final SipMessage msg = parseMessage(RawData.sipInviteOneRecordRouteHeader);
        final ViaHeader via = msg.getViaHeader();
        assertThat(via.getPort(), is(5060));
        assertThat(via.getHost().toString(), is("127.0.0.1"));
        assertThat(via.getBranch().toString(), is("z9hG4bK-5647-1-0"));
        assertThat(via.getRPort(), is(-1));
        assertThat(via.hasRPort(), is(false));
    }

    /**
     * Make sure that we can handle Via-headers correctly.
     */
    @Test
    public void testGetViaHeaders() throws Exception {
        final SipMessage msg = parseMessage(RawData.sipInviteOneRecordRouteHeader);

    }

    @Test
    public void testGetHeaders() throws Exception {
        SipHeader from = this.request.getFromHeader();
        assertThat(from.getName(), is(Buffers.wrap("From")));
        assertThat(from.getValue(), is(Buffers.wrap("sipp <sip:sipp@127.0.1.1:5060>;tag=16732SIPpTag001")));

        // should also be able to do like so
        from = this.request.getHeader(Buffers.wrap("From")).get();
        assertThat(from.getName(), is(Buffers.wrap("From")));
        assertThat(from.getValue(), is(Buffers.wrap("sipp <sip:sipp@127.0.1.1:5060>;tag=16732SIPpTag001")));

        // Grab the Via header
        final SipHeader via = this.request.getHeader(Buffers.wrap("Via")).get();
        assertThat(via.getName(), is(Buffers.wrap("Via")));
        assertThat(via.getValue(), is(Buffers.wrap("SIP/2.0/UDP 127.0.1.1:5060;branch=z9hG4bK-16732-1-0")));

        // Lets skip a few headers. The contact header comes further
        // into the message
        final SipHeader contact = this.request.getHeader(Buffers.wrap("Contact")).get();
        assertThat(contact.getName(), is(Buffers.wrap("Contact")));
        assertThat(contact.getValue(), is(Buffers.wrap("sip:sipp@127.0.1.1:5060")));

        // fetch a header that doesn't exist.
        assertThat(this.request.getHeader("Whatever").isPresent(), is(false));

        // this message does not have a Record-Route header
        final RecordRouteHeader rr = this.request.getRecordRouteHeader();
        assertThat(rr, is((RecordRouteHeader) null));

        // nor does it have a route header
        final RouteHeader route = this.request.getRouteHeader();
        assertThat(route, is((RouteHeader) null));

        // and the purpose with that is that now that we ask for a header that
        // appear before the contact, we should actually find it in the internal
        // parsed storage
        final SipHeader callId = this.request.getHeader(Buffers.wrap("Call-ID")).get();
        assertThat(callId.getName(), is(Buffers.wrap("Call-ID")));
        assertThat(callId.getValue(), is(Buffers.wrap("1-16732@127.0.1.1")));

        // ask for something that doesn't exist...
        // which would have caused us to go through everything
        // in the headers buffer...
        assertThat(this.request.getHeader(Buffers.wrap("Whatever")), is(Optional.empty()));

        // but of course, we should still be able to ask
        // for anything we want
        final SipHeader maxForwards = this.request.getHeader(Buffers.wrap("Max-Forwards")).get();
        assertThat(maxForwards.getName(), is(Buffers.wrap("Max-Forwards")));
        assertThat(maxForwards.getValue(), is(Buffers.wrap("70")));

        final SipHeader contentLength = this.request.getHeader(Buffers.wrap("Content-Length")).get();
        assertThat(contentLength.getName(), is(Buffers.wrap("Content-Length")));
        assertThat(contentLength.getValue(), is(Buffers.wrap("129")));

        final SipHeader contentType = this.request.getHeader(Buffers.wrap("Content-Type")).get();
        assertThat(contentType.getName(), is(Buffers.wrap("Content-Type")));
        assertThat(contentType.getValue(), is(Buffers.wrap("application/sdp")));

        final ContentTypeHeader contentTypeHeader = this.request.getContentTypeHeader();
        assertThat(contentTypeHeader.getName(), is(Buffers.wrap("Content-Type")));
        assertThat(contentTypeHeader.getContentType(), is(Buffers.wrap("application")));
        assertThat(contentTypeHeader.getContentSubType(), is(Buffers.wrap("sdp")));
        assertThat(contentTypeHeader.isSDP(), is(true));
        assertThat(contentTypeHeader.getValue(), is(Buffers.wrap("application/sdp")));
    }

    /**
     * Even though slightly odd, it is def happening in the wild where empty headers are pushed onto
     * a message (seems like you simply shouldn't push the header to begin with, certainly will save
     * space!). When this happens, we have to make sure that we don't continue reading the next
     * header as the value of the previous empty one.
     * 
     * In the example below, the "Hello" header is empty and the value got to be the Call-ID, hence,
     * there wouldn't be any Call-ID header in the request anymore..
     * 
     * @throws Exception
     */
    @Test
    public void testParsingEmptyHeaders() throws Exception {
        final StringBuilder sb = new StringBuilder();
        sb.append("REGISTER sip:127.0.0.1 SIP/2.0\r\n");
        sb.append("Via: SIP/2.0/UDP 10.0.1.14:5069;rport;branch=z9hG4bK662351435\r\n");
        sb.append("From: <sip:jonas@127.0.0.1>;tag=1923738050\r\n");
        sb.append("To: <sip:jonas@127.0.0.1>\r\n");
        sb.append("Hello: \r\n");
        sb.append("Call-ID: 123641868\r\n");
        sb.append("CSeq: 1 REGISTER\r\n");
        sb.append("Contact: <sip:jonas@10.0.1.14:5069;line=6227298e2959de7>\r\n");
        sb.append("Max-Forwards: 70\r\n");
        sb.append("Expires: 3600\r\n");
        sb.append("Content-Length: 0\r\n");

        final SipMessage message = SipMessage.frame(sb.toString());
        message.getCallIDHeader();
        assertThat(message.getCallIDHeader().getValue().toString(), is("123641868"));
        System.out.println(message);
    }

    /**
     * The {@link SipMessage} interface has many convenience methods for fetching the most common
     * headers however, we cannot add all headers to the interface and also in order to facilitate
     * new headers in the future we are allowing to register framers using lambda expressions so
     * that users can extend the API with their own header implementations and framing logic of
     * those headers.
     * 
     * @throws Exception
     */
    @Test
    public void testGetSpecificHeaders() throws Exception {
        final StringBuilder sb = new StringBuilder();
        sb.append("REGISTER sip:127.0.0.1 SIP/2.0\r\n");
        sb.append("Via: SIP/2.0/UDP 10.0.1.14:5069;rport;branch=z9hG4bK662351435\r\n");
        sb.append("From: <sip:jonas@127.0.0.1>;tag=1923738050\r\n");
        sb.append("To: <sip:jonas@127.0.0.1>\r\n");
        sb.append("Call-ID: 123641868\r\n");
        sb.append("CSeq: 1 REGISTER\r\n");
        sb.append("Contact: <sip:jonas@10.0.1.14:5069;line=6227298e2959de7>\r\n");
        sb.append("Max-Forwards: 70\r\n");
        sb.append("User-Agent: Linphone/3.5.2 (eXosip2/3.6.0)\r\n");
        sb.append("Expires: 3600\r\n");
        sb.append("Content-Length: 0\r\n");

        final SipMessage register = SipMessage.frame(sb.toString());
        final ExpiresHeader expires = (ExpiresHeader) register.getHeader(ExpiresHeader.NAME).get().ensure();
        assertThat(expires.getExpires(), is(3600));
    }

}
