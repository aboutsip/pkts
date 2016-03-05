package io.pkts.packet.sip.header.impl;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.header.ViaHeader;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;


/**
 * This is a test for both the Via header implementation itself but also very much so
 * for the builder.
 *
 * @author jonas@jonasborjesson.com
 */
public class ViaHeaderImplTest {

    @Test
    public void testBuildFromScratch() throws Exception {
        final ViaHeader via = ViaHeader.withHost("pkts.io")
                .withBranch("hello")
                .withPort(5088)
                .build();

        assertThat(via.getPort(), is(5088));
        assertThat(via.getBranch().toString(), is("hello"));
        assertThat(via.getHost().toString(), is("pkts.io"));
        assertThat(via.toString(), is("Via: SIP/2.0/UDP pkts.io:5088;branch=hello"));
    }

    @Test
    public void testBuildFromScratchLotsOfParams() throws Exception {
        final ViaHeader via = ViaHeader.withHost("pkts.io")
                .withBranch("hello")
                .withReceived("received-from-here")
                .withParameter("apa", "monkey")
                .withParameter("nisse", "kalle")
                .build();

        assertThat(via.getReceived().toString(), is("received-from-here"));
        assertThat(via.getBranch().toString(), is("hello"));
        assertThat(via.getParameter("nisse").toString(), is("kalle"));
        assertThat(via.getParameter("apa").toString(), is("monkey"));
        assertThat(via.getHost().toString(), is("pkts.io"));
    }

    @Test
    public void testViaHeader() throws Exception {
        assertVia("SIP/2.0/UDP aboutsip.com;branch=45", "UDP", "aboutsip.com", -1, "45");
        assertVia("SIP/2.0/UDP aboutsip.com:5060;branch=45", "UDP", "aboutsip.com", 5060, "45");
        assertVia("SIP/2.0/UDP aboutsip.com:9;branch=45", "UDP", "aboutsip.com", 9, "45");
        assertVia("SIP/2.0/UDP aboutsip.com:9999;branch=z9klj-kljfljk-kjkjkj-ouklj", "UDP", "aboutsip.com", 9999,
                "z9klj-kljfljk-kjkjkj-ouklj");
        assertVia("SIP/2.0/UDP aboutsip.com:9;branch=45;foo=boo;rport", "UDP", "aboutsip.com", 9, "45");
    }

    /**
     * A Via header without a branch parameter is illegal
     * @throws Exception
     */
    @Test (expected = SipParseException.class)
    public void testNoViaBranchParameter() throws Exception {
        ViaHeader.frame(Buffers.wrap("SIP/2.0/UDP aboutsip.com;hello=45"));
    }

    /**
     * A Via header without a branch parameter is illegal so make sure that we get
     * the same result when using the builder directly.
     *
     * @throws Exception
     */
    @Test (expected = SipParseException.class)
    public void testNoViaBranchParameterUsingBuilder() throws Exception {
        ViaHeader.withHost("pkts.io").build();
    }

    @Test
    public void testSetReceived() throws Exception {
        assertViaReceived("SIP/2.0/UDP aboutsip.com;branch=45", "192.168.0.100");
        assertViaReceived("SIP/2.0/UDP aboutsip.com;received;branch=45", "192.168.0.101");
        assertViaReceived("SIP/2.0/TCP aboutsip.com;received=10.36.10.10;branch=45", "192.168.0.102");
    }

    @Test
    public void testSetRPort() throws Exception {
        assertViaRport("SIP/2.0/UDP aboutsip.com;branch=45", 890);
        assertViaRport("SIP/2.0/UDP aboutsip.com;received;branch=45", 9998);
        assertViaRport("SIP/2.0/TCP aboutsip.com;received=10.36.10.10;branch=45", 9997);
    }

    @Test
    public void testSetRPortAsFlagParameter() throws Exception {
        final ViaHeader via = ViaHeader.withHost("aboutsip.com")
                .withTransportTCP()
                .withBranch()
                .withRPortFlag().build();
        assertThat(via.getRPort(), is(-1));
        assertThat(via.hasRPort(), is(true));
    }

    @Test
    public void testSetBranch() throws Exception {
        final Buffer buf = Buffers.wrap("SIP/2.0/TCP aboutsip.com;branch=asdf;hello=world");
        ViaHeader via = ViaHeader.frame(buf);
        assertThat(via.getBranch().toString(), is("asdf"));

        via = via.copy().withBranch(Buffers.wrap("hello-world")).build();

        assertThat(via.getBranch().toString(), is("hello-world"));
        assertThat(via.toString(), is("Via: SIP/2.0/TCP aboutsip.com;branch=hello-world;hello=world"));
        assertThat(via.getValue().toString(), is("SIP/2.0/TCP aboutsip.com;branch=hello-world;hello=world"));
    }

    @Test
    public void testSetParam() throws Exception {
        ViaHeader via = ViaHeader.frame(Buffers.wrap("SIP/2.0/TCP aboutsip.com;branch=3;hello=world"));
        assertThat(via.getParameter("hello").toString(), is("world"));

        ViaHeader.Builder builder = via.copy();
        builder.withParameter(Buffers.wrap("hello"), Buffers.wrap("fup")).build();
        builder.withParameter(Buffers.wrap("apa"), Buffers.wrap("monkey"));

        via = builder.build();
        assertThat(via.getParameter("hello").toString(), is("fup"));
        assertThat(via.getParameter("apa").toString(), is("monkey"));

        assertThat(via.toString().contains("hello=fup"), is(true));
        assertThat(via.toString().contains("apa=monkey"), is(true));
        final Buffer copy = Buffers.createBuffer(1000);
        via.getBytes(copy);
        assertThat(copy.toString().contains("hello=fup"), is(true));
        assertThat(copy.toString().contains("apa=monkey"), is(true));

    }

    private void assertViaRport(final String toParse, final int port) throws Exception {
        final ViaHeader via = ViaHeader.frame(Buffers.wrap(toParse)).copy().withRPort(port).build();
        assertThat(via.getRPort(), is(port));
        assertThat(via.toString().contains("rport=" + Buffers.wrap(port).toString()), is(true));
    }



    private void assertViaReceived(final String toParse, final String received) throws Exception {
        final Buffer buffer = Buffers.wrap(received);
        final ViaHeader via = ViaHeader.frame(Buffers.wrap(toParse)).copy().withReceived(received).build();

        assertThat(via.getReceived().toString(), is(received.toString()));
        assertThat(via.toString().contains("received=" + buffer.toString()), is(true));

    }

    private void assertVia(final String toParse, final String expectedTransport, final String expectedHost,
            final int expectedPort, final String expectedBranch) throws Exception {
        final ViaHeader via = ViaHeader.frame(Buffers.wrap(toParse));
        assertTransport(via, expectedTransport);
        assertThat(via.getBranch().toString(), is(expectedBranch));
        assertThat(via.getPort(), is(expectedPort));

        final Buffer copy = Buffers.createBuffer(512);
        via.getBytes(copy);
        assertThat(copy.toString(), is("Via: " + toParse));
    }

    private void assertTransport(final ViaHeader via, final String expectedTransport) {
        assertThat(via.getTransport().toString(), is(expectedTransport));
        if (expectedTransport.equals("UDP")) {
            assertThat(via.isUDP(), is(true));
            assertThat(via.isTCP(), is(false));
            assertThat(via.isTLS(), is(false));
            assertThat(via.isSCTP(), is(false));
        } else if (expectedTransport.equals("TCP")) {
            assertThat(via.isUDP(), is(false));
            assertThat(via.isTCP(), is(true));
            assertThat(via.isTLS(), is(false));
            assertThat(via.isSCTP(), is(false));
        } else if (expectedTransport.equals("TLS")) {
            assertThat(via.isUDP(), is(false));
            assertThat(via.isTCP(), is(false));
            assertThat(via.isTLS(), is(true));
            assertThat(via.isSCTP(), is(false));
        } else if (expectedTransport.equals("SCTP")) {
            assertThat(via.isUDP(), is(false));
            assertThat(via.isTCP(), is(false));
            assertThat(via.isTLS(), is(false));
            assertThat(via.isSCTP(), is(true));
        } else {
            assertThat(via.isUDP(), is(false));
            assertThat(via.isTCP(), is(false));
            assertThat(via.isTLS(), is(false));
            assertThat(via.isSCTP(), is(false));
        }
    }

}
