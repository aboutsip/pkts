/**
 * 
 */
package io.pkts.packet.sip.header.impl;

import io.pkts.buffer.Buffer;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.header.AddressParametersHeader;
import io.pkts.packet.sip.header.FromHeader;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;


/**
 * Test the {@link FromHeader}.
 * 
 * @author jonas@jonasborjesson.com
 */
public class FromHeaderTest extends AddressParameterHeadersTestBase {

    @Override
    public AddressParametersHeaderImpl frameHeader(final Buffer buffer) throws SipParseException {
        return (FromHeaderImpl) FromHeader.frame(buffer);
    }

    @Override
    public AddressParametersHeader.Builder withHost(final String host) {
        return FromHeader.withHost(host);
    }


    /**
     * Ensure we can create a from header correctly and that it is immutable.
     */
    @Test
    public void testCreateFromHeader() throws Exception {
        final FromHeader from = FromHeader.withHost("hello.com").build();
        assertThat(from.toString(), is("From: sip:hello.com"));
        assertThat(from.getAddress().getURI().toString(), is("sip:hello.com"));

        final FromHeader f2 = from.copy().withUriParameter("foo", "woo").build();
        assertThat(f2.toString(), is("From: <sip:hello.com;foo=woo>"));

        final FromHeader f3 = from.copy().withParameter("nisse", "kalle").withPort(9999).build();
        assertThat(f3.toString(), is("From: sip:hello.com:9999;nisse=kalle"));

        assertThat(from.toString(), not(f2.toString()));
        assertThat(from.toString(), not(f3.toString()));
        assertThat(f2.toString(), not(f3.toString()));
    }

}
