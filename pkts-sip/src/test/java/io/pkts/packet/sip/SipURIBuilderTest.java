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

    @Test
    public void testBasicBuild() throws Exception {
        final String host = "example.com";
        SipURI sipURI = SipURIBuilder.with().host(host).build();
        assertThat(sipURI.getHost().toString(), is(host));
        assertThat(sipURI.getUser().isEmpty(), is(true));
        assertThat(sipURI.toBuffer().toString(), is("sip:example.com"));

        sipURI = SipURIBuilder.with().port(5098).user("nisse").host(host).build();
        assertThat(sipURI.getUser().toString(), is("nisse"));
        assertThat(sipURI.getPort(), is(5098));
        assertThat(sipURI.toBuffer().toString(), is("sip:nisse@example.com:5098"));

        // if we specify 5060 for sip uri then we won't include the port
        sipURI = SipURIBuilder.with().port(5060).user("nisse").useUDP().host(host).build();
        assertThat(sipURI.toBuffer().toString(), is("sip:nisse@example.com:5060;transport=udp"));

        sipURI.setPort(-1);
        assertThat(sipURI.toBuffer().toString(), is("sip:nisse@example.com;transport=udp"));

        // final SipURI sipURI = this.factory.createSipURI(Buffers.wrap("alice"),
        // Buffers.wrap("example.com"));
        // assertThat(sipURI.getUser().toString(), is("alice"));
        // assertThat(sipURI.getHost().toString(), is("example.com"));
        // assertThat(sipURI.getPort(), is(-1));
        // sipURI.setPort(5080);
        // assertThat(sipURI.getPort(), is(5080));
    }

}
