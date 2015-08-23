/**
 * 
 */
package io.pkts.packet.sip.header.impl;

import io.pkts.buffer.Buffer;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.header.AddressParametersHeader;
import io.pkts.packet.sip.header.ToHeader;


/**
 * Really the same as the other {@link ToHeaderImplTest} but here we are using the factory method
 * from the {@link ToHeader} directly instead.
 * 
 * @author jonas@jonasborjesson.com
 */
public class ToHeaderTest extends AddressParameterHeadersTestBase {

    @Override
    public AddressParametersHeaderImpl frameHeader(final Buffer buffer) throws SipParseException {
        return (ToHeaderImpl) ToHeader.frame(buffer);
    }

    @Override
    public AddressParametersHeader.Builder withHost(final String host) {
        return ToHeader.withHost(host);
    }

    /**
     * Ensure we can create a from header correctly and that it is immutable.
     */
    /*
    @Test
    public void testCreateToHeader() throws Exception {
        final ToHeader to = ToHeader.withHost("hello.com").build();
        assertThat(to.toString(), is("To: sip:hello.com"));
        assertThat(to.getAddress().getURI().toString(), is("sip:hello.com"));

        final ToHeader t2 = to.copy().uriParameter("foo", "woo").build();
        assertThat(t2.toString(), is("To: <sip:hello.com;foo=woo>"));

        final ToHeader t3 = to.copy().withParameter("nisse", "kalle").withPort(9999).build();
        assertThat(t3.toString(), is("To: sip:hello.com:9999;nisse=kalle"));

        assertThat(to.toString(), not(t2.toString()));
        assertThat(to.toString(), not(t3.toString()));
        assertThat(t2.toString(), not(t3.toString()));
    }
    */

}
