package io.pkts.packet.sip.header;

import org.junit.Test;

import java.util.function.Function;
import java.util.function.Predicate;

import static io.pkts.packet.sip.header.SipHeader.frame;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author jonas@jonasborjesson.com
 */
public class SipHeaderTest {


    @Test
    public void testIsXXX() {
        // Via Header
        ensureViaHeader("Via");
        ensureViaHeader("VIA");
        ensureViaHeader("vIa");
        ensureViaHeader("via");
        ensureViaHeader("v");
        ensureViaHeader("V");

        // From
        ensureFromHeader("From");
        ensureFromHeader("from");
        ensureFromHeader("FROM");
        ensureFromHeader("FRoM");
        ensureFromHeader("FroM");
        ensureFromHeader("f");
        ensureFromHeader("F");

        // To
        ensureToHeader("To");
        ensureToHeader("TO");
        ensureToHeader("to");
        ensureToHeader("t");
        ensureToHeader("T");

        // Contact
        ensureContactHeader("Contact");
        ensureContactHeader("contact");
        ensureContactHeader("CONTACT");
        ensureContactHeader("CONTaCt");
        ensureContactHeader("CoNTaCt");
        ensureContactHeader("m");
        ensureContactHeader("M");

        // Route
        ensureRouteHeader("Route");
        ensureRouteHeader("ROUTE");
        ensureRouteHeader("route");
        ensureRouteHeader("rOuTE");
        ensureRouteHeader("rOuTe");

        // Record-Route
        ensureRecordRouteHeader("Record-Route");
        ensureRecordRouteHeader("record-route");
        ensureRecordRouteHeader("RECORD-ROUTE");
        ensureRecordRouteHeader("RECOrD-RouTE");
        ensureRecordRouteHeader("rECOrD-rouTE");

        // Max-Forwards
        ensureMaxForwards("Max-Forwards");
        ensureMaxForwards("max-forwards");
        ensureMaxForwards("maX-foRWArds");
        ensureMaxForwards("maX-foRWArdS");
        ensureMaxForwards("MAX-FORWARDS");

        // Expires
        ensureExpires("Expires");
        ensureExpires("expires");
        ensureExpires("exPIRes");
        ensureExpires("exPIReS");
        ensureExpires("EXPIRES");

        // CSeq
        ensureCSeq("CSeq");
        ensureCSeq("cseq");
        ensureCSeq("cSeq");
        ensureCSeq("cSeQ");
        ensureCSeq("CSEQ");

        // Content-Type
        ensureContentType("Content-Type");
        ensureContentType("content-type");
        ensureContentType("conTENt-tYPE");
        ensureContentType("cOnTEnt-tyPE");
        ensureContentType("CONTENT-TYPE");
        ensureContentType("c");
        ensureContentType("C");

        // Content-Length
        ensureContentLength("Content-Length");
        ensureContentLength("content-length");
        ensureContentLength("cONTEnt-leNGTH");
        ensureContentLength("cONTEnT-lENgTH");
        ensureContentLength("CONTEnT-lENgTH");
        ensureContentLength("CONTENT-LENGTH");
        ensureContentLength("l");
        ensureContentLength("L");
    }

    private void ensureViaHeader(String viaName) {
        final SipHeader h1 = frame(viaName + ": SIP/2.0/UDP pkts.io:5088;branch=asdf");
        assertThat(h1.isViaHeader(), is(true));
        assertThat(h1.ensure().toViaHeader().getTransport().toString(), is("UDP"));
    }

    private void ensureFromHeader(final String name) {
        ensureAddressHeader(name, SipHeader::isFromHeader, header -> header.toFromHeader());
    }

    private void ensureToHeader(final String name) {
        ensureAddressHeader(name, SipHeader::isToHeader, header -> header.toToHeader());
    }

    private void ensureContactHeader(final String name) {
        ensureAddressHeader(name, SipHeader::isContactHeader, header -> header.toContactHeader());
    }

    private void ensureRouteHeader(String name) {
        ensureAddressHeader(name, SipHeader::isRouteHeader, header -> header.toRouteHeader());
    }

    private void ensureRecordRouteHeader(final String name) {
        ensureAddressHeader(name, SipHeader::isRecordRouteHeader, header -> header.toRecordRouteHeader());
    }

    private void ensureAddressHeader(String header, Predicate<SipHeader> isHeader, Function<SipHeader, AddressParametersHeader> mapper) {
        final SipHeader h1 = frame(header + ": sip:alice@pkts.io");
        assertThat(isHeader.test(h1), is(true));
        final AddressParametersHeader addressHeader = mapper.apply(h1.ensure());
        assertThat(addressHeader.getAddress().getURI().toSipURI().getHost().toString(), is("pkts.io"));
    }

    private void ensureMaxForwards(String maxName) {
        final SipHeader h1 = frame(maxName + ": 88");
        assertThat(h1.isMaxForwardsHeader(), is(true));
        assertThat(h1.ensure().toMaxForwardsHeader().getMaxForwards(), is(88));
    }

    private void ensureExpires(String name) {
        final SipHeader h1 = frame(name + ": 876");
        assertThat(h1.isExpiresHeader(), is(true));
        assertThat(h1.ensure().toExpiresHeader().getExpires(), is(876));
    }

    private void ensureCSeq(String name) {
        final SipHeader h1 = frame(name + ": 1 INVITE");
        assertThat(h1.isCSeqHeader(), is(true));
        assertThat(h1.ensure().toCSeqHeader().getSeqNumber(), is(1L));
    }

    private void ensureContentType(String name) {
        final SipHeader h1 = frame(name + ": application/sdp");
        assertThat(h1.isContentTypeHeader(), is(true));
        assertThat(h1.ensure().toContentTypeHeader().getContentSubType().toString(), is("sdp"));
    }

    private void ensureContentLength(String name) {
        final SipHeader h1 = frame(name + ": 123");
        assertThat(h1.isContentLengthHeader(), is(true));
        assertThat(h1.ensure().toContentLengthHeader().getContentLength(), is(123));
    }
}