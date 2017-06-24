package io.pkts.packet.sip.impl;

import io.pkts.PktsTestBase;
import io.pkts.RawData;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipMessage;
import io.pkts.packet.sip.SipRequest;
import io.pkts.packet.sip.header.ContentTypeHeader;
import io.pkts.packet.sip.header.ExpiresHeader;
import io.pkts.packet.sip.header.FromHeader;
import io.pkts.packet.sip.header.RecordRouteHeader;
import io.pkts.packet.sip.header.RouteHeader;
import io.pkts.packet.sip.header.SipHeader;
import io.pkts.packet.sip.header.ToHeader;
import io.pkts.packet.sip.header.ViaHeader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class SipMessageImplTest extends PktsTestBase {

    private SipRequest request;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.request = (SipRequest) parseMessage(this.sipFrameBuffer);
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     *
     * A user can specify a function to be called when a non-system header is about
     * to be added to the message under construction via the onHeader-method. Make sure
     * that NO system headers are actually showing up. In the message below, the
     * following headers exists on the message:
     *
     * <ul>
     *    <li>Via</li>
     *    <li>From</li>
     *    <li>To</li>
     *    <li>Call-ID</li>
     *    <li>CSeq</li>
     *    <li>Contact</li>
     *    <li>Max-Forwards</li>
     *    <li>Subject</li>
     *    <li>Content-Type</li>
     *    <li>Record-Route</li>
     *    <li>Content-Length</li>
     * </ul>
     *
     *
     * @throws Exception
     */
    @Test
    public void testNoSystemHeadersInOnHeaderFunction() throws Exception {
        final SipMessage msg = parseMessage(RawData.sipInviteOneRecordRouteHeader);
        msg.copy().onHeader(h -> {
            final String failMsg = "A system header showed up in the onHeader-function. Not allowed! Header was: " + h;
            assertThat(failMsg, h.isSystemHeader(), is(false));
            return h;
        });
    }

    /**
     * For any of the system headers, if you specify it through its corresponding
     * withXXXX method, such as {@link io.pkts.packet.sip.SipMessage.Builder#withFromHeader(FromHeader)}
     * then it will take precedence over any other way you may have specified that header.
     *
     * @throws Exception
     */
    @Test
    public void testSpecifyFromHeader() throws Exception {
        SipMessage msg = parseMessage(RawData.sipInviteOneRecordRouteHeader);
        assertThat(msg.getFromHeader().getValue().toString(), is("sipp <sip:sipp@127.0.0.1:5060>;tag=5647SIPpTag001"));


        msg = msg.copy().withFromHeader(FromHeader.withHost("pkts.io").withUser("hello").build()).build();
        assertThat(msg.getFromHeader().getValue().toString(), is("sip:hello@pkts.io"));

        // also make sure you can still manipulate it
        msg = msg.copy()
                .withFromHeader(FromHeader.withHost("aboutsip.com").withUser("hello").build())
                .onFromHeader(f -> f.withPort(6789).withDisplayName("Alice"))
                .build();
        assertThat(msg.getFromHeader().getValue().toString(), is("Alice <sip:hello@aboutsip.com:6789>"));

        // you should be able to specify any System header through the withHeader-generic way
        msg = msg.copy()
                .withHeader(SipHeader.create("From", "sip:bob@foo.com"))
                .onFromHeader(f -> f.withDisplayName("BOB"))
                .build();
        assertThat(msg.getFromHeader().getValue().toString(), is("BOB <sip:bob@foo.com>"));
    }

    /**
     * Make sure that we can manipulate the from-header. Also make sure that we can
     * register multiple functions that will get called in a chain...
     *
     * @throws Exception
     */
    @Test
    public void testManipulateFromHeader() throws Exception {
        SipMessage msg = parseMessage(RawData.sipInviteOneRecordRouteHeader);

        assertThat(msg.getFromHeader().getAddress().getDisplayName().toString(), is("sipp"));
        assertThat(msg.getFromHeader().getValue().toString(), is("sipp <sip:sipp@127.0.0.1:5060>;tag=5647SIPpTag001"));

        // the mere act of specifying a function on the onFromHeader indicates
        // that you wish to change it so therefore you will be getting
        // a builder object right away. This is true for all system
        // headers.
        msg = msg.copy().onFromHeader(from -> from.withDisplayName("Nisse")).build();
        assertThat(msg.getFromHeader().getAddress().getDisplayName().toString(), is("Nisse"));
        assertThat(msg.getFromHeader().getValue().toString(), is("Nisse <sip:sipp@127.0.0.1:5060>;tag=5647SIPpTag001"));

        // now, make sure that you can specify multiple functions that will be called in a chain...
        // Those should be called in the same order they were registered so since both
        // of them manipulates the display name, the last transformation "wins"
        msg = msg.copy().onFromHeader(from -> from.withDisplayName("Apa")).onFromHeader(from -> from.withDisplayName("Kalle")).build();
        assertThat(msg.getFromHeader().getAddress().getDisplayName().toString(), is("Kalle"));
        assertThat(msg.getFromHeader().getValue().toString(), is("Kalle <sip:sipp@127.0.0.1:5060>;tag=5647SIPpTag001"));

        // in this test, there are still two functions chained together but since they
        // are changing different things both of the changes will make it into the
        // final value
        msg = msg.copy().onFromHeader(from -> from.withUser("alice")).onFromHeader(from -> from.withHost("pkts.io")).build();
        assertThat(msg.getFromHeader().getAddress().getDisplayName().toString(), is("Kalle"));
        assertThat(msg.getFromHeader().getValue().toString(), is("Kalle <sip:alice@pkts.io:5060>;tag=5647SIPpTag001"));
    }

    /**
     * See {@link SipMessageImplTest#testSpecifyFromHeader()}
     *
     * @throws Exception
     */
    @Test
    public void testSpecifyToHeader() throws Exception {
        SipMessage msg = parseMessage(RawData.sipInviteOneRecordRouteHeader);
        assertThat(msg.getToHeader().getValue().toString(), is("sut <sip:service@127.0.0.1:6060>"));

        msg = msg.copy().withToHeader(ToHeader.withHost("pkts.io").withUser("hello").build()).build();
        assertThat(msg.getToHeader().getValue().toString(), is("sip:hello@pkts.io"));

        // also make sure you can still manipulate it
        msg = msg.copy()
                .withToHeader(ToHeader.withHost("aboutsip.com").withUser("hello").build())
                .onToHeader(f -> f.withPort(6789).withDisplayName("Alice"))
                .build();
        assertThat(msg.getToHeader().getValue().toString(), is("Alice <sip:hello@aboutsip.com:6789>"));

        // you should be able to specify any System header through the withHeader-generic way
        msg = msg.copy()
                .withHeader(SipHeader.create("To", "sip:bob@foo.com"))
                .onToHeader(f -> f.withDisplayName("BOB"))
                .build();
        assertThat(msg.getToHeader().getValue().toString(), is("BOB <sip:bob@foo.com>"));
    }

    /**
     * Make sure that we can manipulate the to-header. Also make sure that we can
     * register multiple functions that will get called in a chain...
     *
     * @throws Exception
     */
    @Test
    public void testManipulateToHeader() throws Exception {
        SipMessage msg = parseMessage(RawData.sipInviteOneRecordRouteHeader);

        assertThat(msg.getToHeader().getAddress().getDisplayName().toString(), is("sut"));
        assertThat(msg.getToHeader().getValue().toString(), is("sut <sip:service@127.0.0.1:6060>"));

        // the mere act of specifying a function on the onFromHeader indicates
        // that you wish to change it so therefore you will be getting
        // a builder object right away. This is true for all system
        // headers.
        msg = msg.copy().onToHeader(to -> to.withDisplayName("Nisse")).build();
        assertThat(msg.getToHeader().getAddress().getDisplayName().toString(), is("Nisse"));
        assertThat(msg.getToHeader().getValue().toString(), is("Nisse <sip:service@127.0.0.1:6060>"));

        // now, make sure that you can specify multiple functions that will be called in a chain...
        // Those should be called in the same order they were registered so since both
        // of them manipulates the display name, the last transformation "wins"
        msg = msg.copy().onToHeader(to -> to.withDisplayName("Apa")).onToHeader(to -> to.withDisplayName("Kalle")).build();
        assertThat(msg.getToHeader().getAddress().getDisplayName().toString(), is("Kalle"));
        assertThat(msg.getToHeader().getValue().toString(), is("Kalle <sip:service@127.0.0.1:6060>"));

        // in this test, there are still two functions chained together but since they
        // are changing different things both of the changes will make it into the
        // final value
        msg = msg.copy().onToHeader(to -> to.withUser("alice")).onToHeader(to -> to.withHost("pkts.io")).build();
        assertThat(msg.getToHeader().getAddress().getDisplayName().toString(), is("Kalle"));
        assertThat(msg.getToHeader().getValue().toString(), is("Kalle <sip:alice@pkts.io:6060>"));
    }

    /**
     * Ensure so that we can drop a header from the message we use a copy/template.
     *
     * @throws Exception
     */
    @Test
    public void testDropHeader() throws Exception {
        SipMessage msg = parseMessage(RawData.sipInviteOneRecordRouteHeader);

        assertThat(msg.getAllHeaders().size(), is(11));
        assertThat(msg.toString().contains("Subject: Performance Test"), is(true));
        assertThat(msg.getHeader("Subject").get().getValue().toString(), is("Performance Test"));

        msg = msg.copy().onHeader(h -> {
            // Drop the Subject header
            if (h.getNameStr().equals("Subject")) {
                return null;
            }

            // all else, include as is...
            return h;
        }).build();

        assertThat(msg.getAllHeaders().size(), is(10));
        assertThat(msg.getHeader("Subject").isPresent(), is(false));
        assertThat(msg.toString().contains("Subject: Performance Test"), is(false));

    }

    @Test
    public void testSetMaxForwardsHeader() throws Exception {
        SipMessage msg = parseMessage(RawData.sipInviteOneRecordRouteHeader);
        assertThat(msg.toString().contains("Max-Forwards: 70"), is(true));

        msg = msg.copy().onMaxForwardsHeader(max -> max.withValue(55)).build();

        assertThat(msg.toString().contains("Max-Forwards: 55"), is(true));

        msg = msg.copy().onMaxForwardsHeader(max -> max.withValue(32)).build();
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
