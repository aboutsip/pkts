/**
 * 
 */
package io.pkts.packet.sip.header.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import io.pkts.buffer.Buffer;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.header.FromHeader;
import io.pkts.packet.sip.header.FromHeader.Builder;

import org.junit.Test;


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


    @Test
    public void testCreateToHeader() {
        final Builder builder = FromHeader.with();
        builder.host("hello.com");
        final FromHeader to = builder.build();
        assertThat(to.toString(), is("From: <sip:hello.com>"));
        assertThat(to.getAddress().getURI().toString(), is("sip:hello.com"));
    }

}
