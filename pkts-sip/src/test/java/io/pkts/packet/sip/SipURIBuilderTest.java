/**
 * 
 */
package io.pkts.packet.sip;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import io.pkts.packet.sip.address.SipURI;
import io.pkts.packet.sip.address.SipURIBuilder;

import org.junit.Test;

/**
 * @author jonas@jonasborjesson.com
 */
public class SipURIBuilderTest {

    private final String host = "example.com";

    @Test
    public void testBasicBuild() throws Exception {
        SipURI sipURI = SipURIBuilder.with().host(this.host).build();
        assertThat(sipURI.getHost().toString(), is(this.host));
        assertThat(sipURI.getUser().isEmpty(), is(true));
        assertThat(sipURI.toBuffer().toString(), is("sip:example.com"));

        sipURI = SipURIBuilder.with().port(5098).user("nisse").host(this.host).build();
        assertThat(sipURI.getUser().toString(), is("nisse"));
        assertThat(sipURI.getPort(), is(5098));
        assertThat(sipURI.toBuffer().toString(), is("sip:nisse@example.com:5098"));

        sipURI = SipURIBuilder.with().port(5060).user("nisse").useUDP().host(this.host).build();
        assertThat(sipURI.toBuffer().toString(), is("sip:nisse@example.com:5060;transport=udp"));

        sipURI.setPort(-1);
        assertThat(sipURI.toBuffer().toString(), is("sip:nisse@example.com;transport=udp"));
    }

    @Test
    public void testSips() throws Exception {
        final SipURI sipURI = SipURIBuilder.with().secure().host(this.host).build();
        assertThat(sipURI.toBuffer().toString(), is("sips:example.com"));
    }

    @Test
    public void testMessingWithParameters() throws Exception {
        final SipURI sipURI = SipURIBuilder.with().host(this.host).setParameter("hello", "world").build();
        assertThat(sipURI.toBuffer().toString(), is("sip:example.com;hello=world"));
        sipURI.setParameter("hello", "world2");
        sipURI.setParameter("foo", "boo");
        sipURI.setParameter("lr", null);
        assertThat(sipURI.toBuffer().toString(), is("sip:example.com;hello=world2;foo=boo;lr"));

    }

}
