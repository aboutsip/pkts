/**
 * 
 */
package io.pkts.packet.sip;

import io.pkts.packet.sip.address.SipURI;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * @author jonas@jonasborjesson.com
 */
public class SipURIBuilderTest {

    private final String host = "example.com";

    @Test
    public void testBasicBuild() throws Exception {
        SipURI sipURI = SipURI.withHost(this.host).build();
        assertThat(sipURI.getHost().toString(), is(this.host));
        assertThat(sipURI.getUser().isPresent(), is(false));
        assertThat(sipURI.toBuffer().toString(), is("sip:example.com"));

        sipURI = SipURI.withUser("nisse").withPort(5098).withHost(this.host).build();
        assertThat(sipURI.getUser().get().toString(), is("nisse"));
        assertThat(sipURI.getPort(), is(5098));
        assertThat(sipURI.toBuffer().toString(), is("sip:nisse@example.com:5098"));

        sipURI = SipURI.withUser("nisse").withPort(5060).useUDP().withHost(this.host).build();
        assertThat(sipURI.toBuffer().toString(), is("sip:nisse@example.com:5060;transport=udp"));

        sipURI = sipURI.copy().withPort(-1).build();
        assertThat(sipURI.toBuffer().toString(), is("sip:nisse@example.com;transport=udp"));
    }

    @Test
    public void testSips() throws Exception {
        final SipURI sipURI = SipURI.withHost(this.host).secure().build();
        assertThat(sipURI.toBuffer().toString(), is("sips:example.com"));
    }

    @Test
    public void testMessingWithParameters() throws Exception {
        SipURI sipURI = SipURI.withHost(this.host).withParameter("hello", "world").build();
        assertThat(sipURI.toBuffer().toString(), is("sip:example.com;hello=world"));
        final SipURI.Builder builder = sipURI.copy();
        builder.withParameter("hello", "world2");
        builder.withParameter("foo", "boo");
        builder.withParameter("lr", null);
        sipURI = builder.build();
        assertThat(sipURI.toBuffer().toString(), is("sip:example.com;hello=world2;foo=boo;lr"));

    }

}
