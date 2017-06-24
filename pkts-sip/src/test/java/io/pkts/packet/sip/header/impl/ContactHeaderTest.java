/**
 * 
 */
package io.pkts.packet.sip.header.impl;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.address.Address;
import io.pkts.packet.sip.address.SipURI;
import io.pkts.packet.sip.header.AddressParametersHeader;
import io.pkts.packet.sip.header.ContactHeader;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * @author jonas@jonasborjesson.com
 *
 */
public class ContactHeaderTest extends AddressParameterHeadersTestBase {

    @Test
    public void testBriaContactHeader() {
        final Buffer buffer =
                Buffers.wrap("<sip:hello@10.0.1.5:51945;ob>;reg-id=1;+sip.instance=\"<urn:uuid:D5E3DFFEFC3E4B69BCDFCC5DAC7BDEA9326B2EB8>\"");
        final ContactHeader contact = ContactHeader.frame(buffer);
        final Address address = contact.getAddress();
        assertThat(address.getDisplayName(), is(Buffers.EMPTY_BUFFER));
        final SipURI uri = (SipURI) address.getURI();
        assertThat(uri.getUser().get(), is(Buffers.wrap("hello")));
        assertThat(uri.getHost().toString(), is("10.0.1.5"));
        assertThat(uri.getPort(), is(51945));
        assertThat(uri.getParameter("ob").get(), is(Buffers.EMPTY_BUFFER));


        // note, asking for a parameter on the actual header is NOT the same
        // as doing it on the actual URI. Now you are asking for a header parameter
        // instead... in this case, it doesn't exist anyway but whatever...
        assertThat(contact.getParameter("expires"), is((Buffer) null));
        assertThat(contact.getParameter("+sip.instance").toString(),
                is("<urn:uuid:D5E3DFFEFC3E4B69BCDFCC5DAC7BDEA9326B2EB8>"));

    }

    @Test
    public void testBuildContact() throws Exception {
        final SipURI uri = SipURI.frame(Buffers.wrap("sip:hello@10.0.1.5:51945;ob")).copy().withParameter("expires", 600).build();
        final ContactHeader contact = ContactHeader.withSipURI(uri).build();
        contact.toString();
        assertThat(contact.toString(), is("Contact: <sip:hello@10.0.1.5:51945;ob;expires=600>"));
    }

    @Override
    public AddressParametersHeaderImpl frameHeader(Buffer buffer) throws SipParseException {
        return (ContactHeaderImpl)ContactHeader.frame(buffer);
    }

    @Override
    public AddressParametersHeader.Builder withHost(String host) {
        return ContactHeader.withHost(host);
    }
}
