package io.pkts.packet.sip.header.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.header.ViaHeader;
import io.pkts.packet.sip.header.impl.ViaHeaderImpl;

import org.junit.Before;
import org.junit.Test;


/**
 * 
 * @author jonas@jonasborjesson.com
 */
public class ViaHeaderImplTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testViaHeader() throws Exception {
        assertVia("SIP/2.0/UDP aboutsip.com;branch=45", "UDP", "aboutsip.com", -1, "45");
        assertVia("SIP/2.0/APA aboutsip.com;branch=45", "APA", "aboutsip.com", -1, "45");
        assertVia("SIP/2.0/UDP aboutsip.com:5060;branch=45", "UDP", "aboutsip.com", 5060, "45");
        assertVia("SIP/2.0/UDP aboutsip.com:9;branch=45", "UDP", "aboutsip.com", 9, "45");
        assertVia("SIP/2.0/UDP aboutsip.com:9999;branch=z9klj-kljfljk-kjkjkj-ouklj", "UDP", "aboutsip.com", 9999,
                "z9klj-kljfljk-kjkjkj-ouklj");
        assertVia("SIP/2.0/UDP aboutsip.com:9;branch=45;foo=boo;rport", "UDP", "aboutsip.com", 9, "45");
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
    public void testSetParam() throws Exception {
        final ViaHeader via = ViaHeaderImpl.frame(Buffers.wrap("SIP/2.0/TCP aboutsip.com;branch=3;hello=world"));
        assertThat(via.getParameter("hello").toString(), is("world"));
        via.setParameter(Buffers.wrap("hello"), Buffers.wrap("fup"));
        assertThat(via.getParameter("hello").toString(), is("fup"));

        via.setParameter(Buffers.wrap("apa"), Buffers.wrap("monkey"));
        assertThat(via.getParameter("apa").toString(), is("monkey"));

        assertThat(via.toString().contains("hello=fup"), is(true));
        assertThat(via.toString().contains("apa=monkey"), is(true));
        final Buffer copy = Buffers.createBuffer(1000);
        via.getBytes(copy);
        assertThat(copy.toString().contains("hello=fup"), is(true));
        assertThat(copy.toString().contains("apa=monkey"), is(true));

    }

    private void assertViaRport(final String toParse, final int port) throws Exception {
        final ViaHeader via = ViaHeaderImpl.frame(Buffers.wrap(toParse));
        via.setRPort(port);
        assertThat(via.getRPort(), is(port));
        assertThat(via.toString().contains("rport=" + Buffers.wrap(port).toString()), is(true));
    }

    private void assertViaReceived(final String toParse, final String received) throws Exception {
        final Buffer buffer = Buffers.wrap(received);
        final ViaHeader via = ViaHeaderImpl.frame(Buffers.wrap(toParse));
        via.setReceived(buffer);
        assertThat(via.getReceived().toString(), is(received.toString()));
        assertThat(via.toString().contains("received=" + buffer.toString()), is(true));

    }

    private void assertVia(final String toParse, final String expectedTransport, final String expectedHost,
            final int expectedPort, final String expectedBranch) throws Exception {
        final ViaHeader via = ViaHeaderImpl.frame(Buffers.wrap(toParse));
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
