package com.aboutsip.yajpcap.packet.sip.header.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.buffer.Buffers;
import com.aboutsip.yajpcap.packet.sip.header.ViaHeader;

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
