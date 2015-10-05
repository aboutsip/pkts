/**
 * 
 */
package io.pkts.packet.sip.address.impl;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.address.Address;
import io.pkts.packet.sip.address.SipURI;
import io.pkts.packet.sip.address.URI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;


/**
 * @author jonas@jonasborjesson.com
 */
public class AddressImplTest {

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Make sure that when we parse something that we actually spit out the
     * EXACT same format again. E.g., the following address
     * "<sip:alice@whatever.com>" does not actually need the angle-brackets so
     * we could spit it out as "sip:alice@whatever.com" but we should really
     * honor what the user gave us to begin with.
     * 
     * @throws Exception
     */
    @Test
    public void testToString() throws Exception {
        assertAddressToStringIsTheSame("<sip:alice@whatever.com>");
        assertAddressToStringIsTheSame("sip:alice@whatever.com");
        assertAddressToStringIsTheSame("alice <sip:alice@whatever.com>");
        assertAddressToStringIsTheSame("\"alice\" <sip:alice@whatever.com>");
        assertAddressToStringIsTheSame("\"alice smith\" <sip:alice@whatever.com>");
        assertAddressToStringIsTheSame("\"alice smith\" <sip:alice@whatever.com;foo=boo>");
    }

    @Test
    public void testCopyConstruct() throws Exception {
        assertAddressToCopyToStringIsTheSame("sip:alice@whatever.com");
        assertAddressToCopyToStringIsTheSame("sip:alice@whatever.com", "sip:alice@whatever.com");
        assertAddressToCopyToStringIsTheSame("alice <sip:alice@whatever.com>");
        assertAddressToCopyToStringIsTheSame("\"alice\" <sip:alice@whatever.com>", "alice <sip:alice@whatever.com>");
        assertAddressToCopyToStringIsTheSame("\"alice smith\" <sip:alice@whatever.com>");
        assertAddressToCopyToStringIsTheSame("\"alice smith\" <sip:alice@whatever.com;foo=boo>");
    }

    /**
     * Ensure that if we take an address, parse it to create a new {@link Address} object, then do
     * a {@link Address#copy()} on it, build it and then toString that we do get the same again.
     * @param address
     */
    private void assertAddressToCopyToStringIsTheSame(final String address, final String expected) throws Exception {
        final Address a = Address.frame(address).copy().build();
        assertThat(a.toString(), is(expected));
    }

    private void assertAddressToCopyToStringIsTheSame(final String address) throws Exception {
        assertAddressToCopyToStringIsTheSame(address, address);
    }

    /**
     * Ensure that we can change the URI and when doing so, the original should never be affected
     * since it is after all an immutable class.
     *
     * @throws Exception
     */
    @Test
    public void testChangeURI() throws Exception {
        final Address a1 = Address.frame("hello <sip:alice@example.com>");
        final Address a2 = a1.copy().withPort(876).build();
        assertThat(a2.toString().contains("876"), is(true));
        assertThat(a1.toString().contains("876"), is(false));

        assertThat(a1.getURI().toSipURI().getPort(), is(-1));
        assertThat(a2.getURI().toSipURI().getPort(), is(876));

        final Address a3 = a2.copy().withDisplayName("alice").withHost("pkts.io").build();
        assertThat(a3.toString(), is("alice <sip:alice@pkts.io:876>"));
        assertThat(a1.getURI().toSipURI().getHost().toString(), is("example.com"));
        assertThat(a3.getURI().toSipURI().getHost().toString(), is("pkts.io"));
    }

    private void assertAddressToStringIsTheSame(final String address) throws Exception {
        assertThat(Address.frame(Buffers.wrap(address)).toString(), is(address));
    }

    /**
     * Make sure that we can correctly frame an address
     * 
     * @throws Exception
     */
    @Test
    public void testFraming() throws Exception {
        Address address = Address.frame(Buffers.wrap("hello <sip:alice@example.com>"));
        assertThat(address.getDisplayName().toString(), is("hello"));
        URI uri = address.getURI();
        assertThat(uri.isSipURI(), is(true));
        assertThat(uri.getScheme().toString(), is("sip"));

        final SipURI sipURI = (SipURI) uri;
        assertThat(sipURI.toBuffer().toString(), is("sip:alice@example.com"));
        assertThat(sipURI.toString(), is("sip:alice@example.com"));
        assertThat(sipURI.getUser().toString(), is("alice"));
        assertThat(sipURI.getHost().toString(), is("example.com"));

        // no display name
        address = Address.frame(Buffers.wrap("<sip:alice@example.com>"));
        assertThat(address.getDisplayName().isEmpty(), is(true));
        assertThat(address.getURI().toString(), is("sip:alice@example.com"));

        address = Address.frame(Buffers.wrap("sip:alice@example.com"));
        assertThat(address.getDisplayName().isEmpty(), is(true));
        uri = address.getURI();
        assertThat(uri.isSipURI(), is(true));
        assertThat(uri.getScheme().toString(), is("sip"));
        assertThat(uri.toString(), is("sip:alice@example.com"));

        Buffer buffer = Buffers.wrap("sip:alice@example.com");
        address = Address.frame(buffer);
        assertThat(address.getURI().toString(), is("sip:alice@example.com"));
        assertThat(buffer.toString(), is(""));
        assertThat(buffer.isEmpty(), is(true));

        buffer = Buffers.wrap("sip:example.com");
        address = Address.frame(buffer);
        assertThat(address.getURI().toString(), is("sip:example.com"));

        // Empty display name
        buffer = Buffers.wrap("\"\" <sip:alice@example.com>");
        address = Address.frame(buffer);
        assertThat(address.getDisplayName().isEmpty(), is(true));
        assertThat(address.getURI().toString(), is("sip:alice@example.com"));
    }

    /**
     * Make sure that we correctly include the uri parameters when present.
     * 
     * @throws Exception
     */
    @Test
    public void testFramingWithUriParameters() throws Exception {
        Address address = Address.frame(Buffers.wrap("hello <sip:alice@example.com;transport=tcp>"));
        assertThat(address.getDisplayName().toString(), is("hello"));
        final URI uri = address.getURI();
        assertThat(uri.isSipURI(), is(true));
        assertThat(uri.getScheme().toString(), is("sip"));

        final SipURI sipURI = (SipURI) uri;
        assertThat(sipURI.toBuffer().toString(), is("sip:alice@example.com;transport=tcp"));

        address = Address.frame(Buffers.wrap("<sip:alice@example.com;apa>"));
        assertThat(address.getURI().toString(), is("sip:alice@example.com;apa"));
    }

    /**
     * Make sure that if there are header parameters present that those do not
     * get consumed when we frame the address.
     * 
     * @throws Exception
     */
    @Test
    public void testFramingWithHeaderParameters() throws Exception {
        Buffer buffer = Buffers.wrap("hello <sip:alice@example.com;transport=tcp>;expires=0;lr;foo=woo");
        Address address = Address.frame(buffer);
        assertThat(address.getURI().toString(), is("sip:alice@example.com;transport=tcp"));
        assertThat(buffer.toString(), is(";expires=0;lr;foo=woo"));

        buffer = Buffers.wrap("<sip:alice@example.com;transport=tcp>;expires=0;lr;foo=woo");
        address = Address.frame(buffer);
        assertThat(address.getURI().toString(), is("sip:alice@example.com;transport=tcp"));
        assertThat(buffer.toString(), is(";expires=0;lr;foo=woo"));

        // now the tricky part, nop <> even though it is technically forbidden
        // (I think)
        // Note that the transport=tcp is not a header parameter because it is
        // not
        // protected by the < > construct.
        buffer = Buffers.wrap("sip:alice@example.com;transport=tcp;expires=0;lr;foo=woo");
        address = Address.frame(buffer);
        assertThat(address.getURI().toString(), is("sip:alice@example.com"));
        assertThat(buffer.toString(), is(";transport=tcp;expires=0;lr;foo=woo"));

    }

}
