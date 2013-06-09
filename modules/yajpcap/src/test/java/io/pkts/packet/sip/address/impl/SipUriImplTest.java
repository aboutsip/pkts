/**
 * 
 */
package io.pkts.packet.sip.address.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.address.SipURI;
import io.pkts.packet.sip.address.impl.SipURIImpl;

import org.junit.Before;
import org.junit.Test;


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
        assertSipUri("sip:alice@example.com:5090", "alice", "example.com", 5090);
        assertSipUri("sip:alice@example.com", "alice", "example.com", -1);
        assertSipUri("sip:example.com", "", "example.com", -1);
        assertSipUri("sip:example.com:4;transport=udp", "", "example.com", 4);
        assertSipUri("sip:alice@example.com;transport=udp", "alice", "example.com", -1);
        assertSipUri("sip:a@example.com;transport=udp", "a", "example.com", -1);
        assertSipUri("sip:alice@example.com:5555?hello=world", "alice", "example.com", 5555);
    }

    /**
     * Make sure that we can set the port as expected
     * 
     * @throws Exception
     */
    @Test
    public void testSetPort() throws Exception {
        assertSetPort("sip:alice@example.com", 9999, "sip:alice@example.com:9999");
        assertSetPort("sip:alice@example.com:8888", 7777, "sip:alice@example.com:7777");
        assertSetPort("sip:alice@example.com:7", 8, "sip:alice@example.com:8");
        assertSetPort("sip:alice@example.com:7;transport=udp", 8, "sip:alice@example.com:8;transport=udp");
        assertSetPort("sip:alice@example.com;transport=tcp&hello=world", 9999,
                "sip:alice@example.com:9999;transport=tcp&hello=world");
    }

    private void assertSetPort(final String toParse, final int port, final String expected) throws Exception {
        final SipURI uri = SipURIImpl.frame(Buffers.wrap(toParse));
        uri.setPort(port);
        assertThat(uri.getPort(), is(port));
        assertThat(uri.toString(), is(expected));

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
