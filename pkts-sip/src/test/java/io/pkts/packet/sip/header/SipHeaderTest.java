package io.pkts.packet.sip.header;

import org.junit.Test;

import static io.pkts.packet.sip.header.SipHeader.frame;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * @author jonas@jonasborjesson.com
 */
public class SipHeaderTest {

    @Test
    public void testIsXXX() {
        SipHeader h1 = null;

        // Via Header
        h1 = frame("Via: SIP/2.0/UDP pkts.io:5088;branch=asdf");
        assertThat(h1.isViaHeader(), is(true));
        assertThat(h1.ensure().toViaHeader().getTransport().toString(), is("UDP"));

        h1 = frame("v: SIP/2.0/UDP pkts.io:5088;branch=asdf");
        assertThat(h1.isViaHeader(), is(true));
        assertThat(h1.ensure().toViaHeader().getTransport().toString(), is("UDP"));

        // From
        h1 = frame("From: sip:alice@pkts.io");
        assertThat(h1.isFromHeader(), is(true));
        assertThat(h1.ensure().toFromHeader().getAddress().getURI().toSipURI().getHost().toString(), is("pkts.io"));

        h1 = frame("f: sip:alice@pkts.io");
        assertThat(h1.isFromHeader(), is(true));
        assertThat(h1.ensure().toFromHeader().getAddress().getURI().toSipURI().getHost().toString(), is("pkts.io"));

        // To
        h1 = frame("To: sip:alice@pkts.io");
        assertThat(h1.isToHeader(), is(true));
        assertThat(h1.ensure().toToHeader().getAddress().getURI().toSipURI().getHost().toString(), is("pkts.io"));

        h1 = frame("t: sip:alice@pkts.io");
        assertThat(h1.isToHeader(), is(true));
        assertThat(h1.ensure().toToHeader().getAddress().getURI().toSipURI().getHost().toString(), is("pkts.io"));

        // Contact
        h1 = frame("Contact: sip:alice@pkts.io");
        assertThat(h1.isContactHeader(), is(true));
        assertThat(h1.ensure().toContactHeader().getAddress().getURI().toSipURI().getHost().toString(), is("pkts.io"));

        h1 = frame("m: sip:alice@pkts.io");
        assertThat(h1.isContactHeader(), is(true));
        assertThat(h1.ensure().toContactHeader().getAddress().getURI().toSipURI().getHost().toString(), is("pkts.io"));

        // Route
        h1 = frame("Route: sip:alice@pkts.io");
        assertThat(h1.isRouteHeader(), is(true));
        assertThat(h1.ensure().toRouterHeader().getAddress().getURI().toSipURI().getHost().toString(), is("pkts.io"));

        // Record-Route
        h1 = frame("Record-Route: sip:alice@pkts.io");
        assertThat(h1.isRecordRouteHeader(), is(true));
        assertThat(h1.ensure().toRecordRouteHeader().getAddress().getURI().toSipURI().getHost().toString(), is("pkts.io"));

        // Max-Forwards
        h1 = frame("Max-Forwards: 88");
        assertThat(h1.isMaxForwardsHeader(), is(true));
        assertThat(h1.ensure().toMaxForwardsHeader().getMaxForwards(), is(88));

        // Expires
        h1 = frame("Expires: 876");
        assertThat(h1.isExpiresHeader(), is(true));
        assertThat(h1.ensure().toExpiresHeader().getExpires(), is(876));

        // CSeq
        h1 = frame("CSeq: 1 INVITE");
        assertThat(h1.isCSeqHeader(), is(true));
        assertThat(h1.ensure().toCSeqHeader().getSeqNumber(), is(1L));

        // Content-Type
        h1 = frame("Content-Type: application/sdp");
        assertThat(h1.isContentTypeHeader(), is(true));
        assertThat(h1.ensure().toContentTypeHeader().getContentSubType().toString(), is("sdp"));

        h1 = frame("c: application/sdp");
        assertThat(h1.isContentTypeHeader(), is(true));
        assertThat(h1.ensure().toContentTypeHeader().getContentSubType().toString(), is("sdp"));

        // Content-Length
        h1 = frame("Content-Length: 123");
        assertThat(h1.isContentLengthHeader(), is(true));
        assertThat(h1.ensure().toContentLengthHeader().getContentLength(), is(123));

        h1 = frame("l: 123");
        assertThat(h1.isContentLengthHeader(), is(true));
        assertThat(h1.ensure().toContentLengthHeader().getContentLength(), is(123));
    }

}