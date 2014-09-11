/**
 * 
 */
package io.pkts.packet.sip.header.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import io.pkts.buffer.Buffer;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.header.ToHeader;
import io.pkts.packet.sip.header.ToHeader.Builder;

import org.junit.Test;


/**
 * Really the same as the other {@link ToHeaderImplTest} but here we are using the factory method
 * from the {@link ToHeader} directly instead.
 * 
 * @author jonas@jonasborjesson.com
 */
public class ToHeaderTest extends AddressParameterHeadersTestBase {

    @Override
    public AddressParametersHeaderImpl frameHeader(final Buffer buffer) throws SipParseException {
        return (ToHeaderImpl) ToHeader.create(buffer);
    }

    @Test
    public void testCreateToHeader() {
        final Builder builder = ToHeader.with();
        builder.host("hello.com");
        final ToHeader to = builder.build();
        assertThat(to.toString(), is("To: <sip:hello.com>"));
        assertThat(to.getAddress().getURI().toString(), is("sip:hello.com"));
    }

}
