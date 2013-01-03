/**
 * 
 */
package com.aboutsip.yajpcap.packet.sip.address.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.buffer.Buffers;
import com.aboutsip.yajpcap.packet.sip.address.Address;
import com.aboutsip.yajpcap.packet.sip.address.SipURI;
import com.aboutsip.yajpcap.packet.sip.address.URI;

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
     * Make sure that we can correctly frame an address
     * 
     * @throws Exception
     */
    @Test
    public void testFraming() throws Exception {
        Address address = AddressImpl.parse(Buffers.wrap("hello <sip:alice@example.com>"));
        assertThat(address.getDisplayName().toString(), is("hello"));
        URI uri = address.getURI();
        assertThat(uri.isSipURI(), is(true));
        assertThat(uri.getScheme().toString(), is("sip"));

        final SipURI sipURI = (SipURI) uri;
        assertThat(sipURI.toBuffer().toString(), is("sip:alice@example.com"));
        assertThat(sipURI.toString(), is("sip:alice@example.com"));

        // no display name
        address = AddressImpl.parse(Buffers.wrap("<sip:alice@example.com>"));
        assertThat(address.getDisplayName().isEmpty(), is(true));
        assertThat(address.getURI().toString(), is("sip:alice@example.com"));

        address = AddressImpl.parse(Buffers.wrap("sip:alice@example.com"));
        assertThat(address.getDisplayName().isEmpty(), is(true));
        uri = address.getURI();
        assertThat(uri.isSipURI(), is(true));
        assertThat(uri.getScheme().toString(), is("sip"));
        assertThat(uri.toString(), is("sip:alice@example.com"));

        Buffer buffer = Buffers.wrap("sip:alice@example.com");
        address = AddressImpl.parse(buffer);
        assertThat(address.getURI().toString(), is("sip:alice@example.com"));
        assertThat(buffer.toString(), is(""));
        assertThat(buffer.isEmpty(), is(true));

        buffer = Buffers.wrap("sip:example.com");
        address = AddressImpl.parse(buffer);
        assertThat(address.getURI().toString(), is("sip:example.com"));
    }

    /**
     * Make sure that we correctly include the uri parameters when present.
     * 
     * @throws Exception
     */
    @Test
    public void testFramingWithUriParameters() throws Exception {
        Address address = AddressImpl.parse(Buffers.wrap("hello <sip:alice@example.com;transport=tcp>"));
        assertThat(address.getDisplayName().toString(), is("hello"));
        final URI uri = address.getURI();
        assertThat(uri.isSipURI(), is(true));
        assertThat(uri.getScheme().toString(), is("sip"));

        final SipURI sipURI = (SipURI) uri;
        assertThat(sipURI.toBuffer().toString(), is("sip:alice@example.com;transport=tcp"));

        address = AddressImpl.parse(Buffers.wrap("<sip:alice@example.com;apa>"));
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
        Address address = AddressImpl.parse(buffer);
        assertThat(address.getURI().toString(), is("sip:alice@example.com;transport=tcp"));
        assertThat(buffer.toString(), is(";expires=0;lr;foo=woo"));

        buffer = Buffers.wrap("<sip:alice@example.com;transport=tcp>;expires=0;lr;foo=woo");
        address = AddressImpl.parse(buffer);
        assertThat(address.getURI().toString(), is("sip:alice@example.com;transport=tcp"));
        assertThat(buffer.toString(), is(";expires=0;lr;foo=woo"));

        // now the tricky part, nop <> even though it is technically forbidden (I think)
        // Note that the transport=tcp is not a header parameter because it is not
        // protected by the < > construct.
        buffer = Buffers.wrap("sip:alice@example.com;transport=tcp;expires=0;lr;foo=woo");
        address = AddressImpl.parse(buffer);
        assertThat(address.getURI().toString(), is("sip:alice@example.com"));
        assertThat(buffer.toString(), is(";transport=tcp;expires=0;lr;foo=woo"));

    }

}
