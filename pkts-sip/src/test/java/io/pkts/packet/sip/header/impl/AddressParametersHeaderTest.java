/**
 * 
 */
package io.pkts.packet.sip.header.impl;

import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.address.Address;
import io.pkts.packet.sip.address.SipURI;
import io.pkts.packet.sip.header.AddressParametersHeader;
import io.pkts.packet.sip.header.AddressParametersHeader.Builder;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

/**
 * @author jonas
 *
 */
public class AddressParametersHeaderTest {


    @Test
    public void testCreateGenericAddressParametersHeader() throws Exception {
        final Builder<AddressParametersHeader> builder = AddressParametersHeader.with(Buffers.wrap("Hello"));
        builder.withHost("example.com");
        final AddressParametersHeader header = builder.build();

        assertThat(header, not((AddressParametersHeader) null));
        final Address address = header.getAddress();
        assertThat(address.getDisplayName().isEmpty(), is(true));
        final SipURI uri = (SipURI) address.getURI();
        assertThat(uri.getHost().toString(), is("example.com"));
    }


}
