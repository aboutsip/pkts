/**
 * 
 */
package com.aboutsip.yajpcap.packet.sip.address.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.buffer.Buffers;
import com.aboutsip.yajpcap.packet.sip.address.SipURI;

/**
 * @author jonas@jonasborjesson.com
 */
public class SipUriImplTest {

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testFramingSipURI() throws Exception {
        assertSipUri("sip:alice@example.com", "alice", "example.com", -1);
        assertSipUri("sip:example.com", "", "example.com", -1);
        assertSipUri("sip:example.com;transport=udp", "", "example.com", -1);
        assertSipUri("sip:alice@example.com;transport=udp", "alice", "example.com", -1);
        assertSipUri("sip:a@example.com;transport=udp", "a", "example.com", -1);
        assertSipUri("sip:alice@example.com?hello=world", "alice", "example.com", -1);
    }

    /**
     * Helper method for ensuring that we parse SIP Uri's correctly
     * 
     * @param toParse
     * @param expectedUser
     * @param expectedHost
     * @param expectedPort
     * @throws Exception
     */
    private void assertSipUri(final String toParse, final String expectedUser, final String expectedHost,
            final int expectedPort) throws Exception {
        final Buffer buffer = Buffers.wrap(toParse);
        final SipURI uri = SipURIImpl.frame(buffer);
        assertThat(uri.isSipURI(), is(true));
        assertThat(uri.getUser().toString(), is(expectedUser));
        assertThat(uri.getHost().toString(), is(expectedHost));
        assertThat(uri.getPort(), is(expectedPort));
    }

}
