/**
 * 
 */
package com.aboutsip.yajpcap.packet.sip.header.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.buffer.Buffers;
import com.aboutsip.yajpcap.packet.sip.SipParseException;
import com.aboutsip.yajpcap.packet.sip.address.Address;
import com.aboutsip.yajpcap.packet.sip.address.SipURI;
import com.aboutsip.yajpcap.packet.sip.header.FromHeader;
import com.aboutsip.yajpcap.packet.sip.header.ToHeader;

/**
 * The test base for testing all headers that are considered to be
 * {@link AddressParametersHeader}s, which include headers such as
 * {@link ToHeader}, {@link FromHeader} etc.
 * 
 * @author jonas@jonasborjesson.com
 */
public abstract class AddressParameterHeadersTestBase {

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
     * To be overridden by subclasses.
     * 
     * @param buffer
     * @return
     */
    public abstract AddressParametersHeader frameHeader(final Buffer buffer) throws SipParseException;

    private void assertGetBytes(final String expected, final AddressParametersHeader header) {
        final Buffer copy = Buffers.createBuffer(100);
        header.getBytes(copy);
        assertThat(copy.toString(), is(header.getName().toString() + ": " + expected));
    }

    /**
     * Make sure that the parameters of the header is correctly recognized.
     * 
     * @throws Exception
     */
    @Test
    public void testParameters() throws Exception {
        String s = "sip:alice@example.com;hello=world;apa=monkey";
        AddressParametersHeader to = frameHeader(Buffers.wrap(s));
        assertThat(to.getParameter("hello").toString(), is("world"));
        assertThat(to.getParameter("apa").toString(), is("monkey"));
        assertGetBytes(s, to);

        // make sure we can fetch them again...
        assertThat(to.getParameter("apa").toString(), is("monkey"));
        assertThat(to.getParameter("hello").toString(), is("world"));

        // check the flag parameter loose route...
        s = "sip:alice@example.com;tag=asdf;lr";
        to = frameHeader(Buffers.wrap(s));
        assertThat(to.getParameter("lr").isEmpty(), is(true));
        assertThat(to.getParameter("lr").toString(), is(""));
        assertGetBytes(s, to);

        s = "sip:alice@example.com;lr";
        to = frameHeader(Buffers.wrap(s));
        assertThat(to.getParameter("lr").isEmpty(), is(true));
        assertGetBytes(s, to);

        s = "sip:alice@example.com;fup;lr;apa";
        to = frameHeader(Buffers.wrap(s));
        assertThat(to.getParameter("lr").isEmpty(), is(true));
        assertThat(to.getParameter("fup").isEmpty(), is(true));
        assertThat(to.getParameter("apa").isEmpty(), is(true));
        assertGetBytes(s, to);

        s = "Alice <sip:alice@example.com;fup>;lr;apa;a=b";
        to = frameHeader(Buffers.wrap(s));
        assertThat(to.getParameter("lr").isEmpty(), is(true));
        assertThat(to.getParameter("apa").isEmpty(), is(true));
        assertThat(to.getParameter("a").toString(), is("b"));
        assertGetBytes(s, to);

        // fup is now a uri parameter...
        assertThat(to.getParameter("fup"), is((Buffer) null));
    }

    /**
     * Make sure that the getTag on the from and to headers are recognized
     * correctly.
     * 
     * @throws Exception
     */
    public void testGetTag() throws Exception {
        AddressParametersHeader to = frameHeader(Buffers.wrap("sip:alice@example.com;hello=world;apa=monkey"));
        if (to instanceof ToHeader) {
            assertThat(to.getName().toString(), is("To"));
            assertThat(((ToHeader) to).getTag(), is((Buffer) null));
        } else if (to instanceof FromHeader) {
            assertThat(to.getName().toString(), is("To"));
            assertThat(((FromHeader) to).getTag(), is((Buffer) null));
        }

        // check the tag...
        to = frameHeader(Buffers.wrap("sip:alice@example.com;hello=world;tag=asdf;apa=monkey"));
        if (to instanceof ToHeader) {
            assertThat(((ToHeader) to).getTag().toString(), is("asdf"));
        } else if (to instanceof FromHeader) {
            assertThat(((FromHeader) to).getTag().toString(), is("asdf"));
        }

        to = frameHeader(Buffers.wrap("sip:alice@example.com;tag=asdf;lr"));
        if (to instanceof ToHeader) {
            assertThat(((ToHeader) to).getTag().toString(), is("asdf"));
        } else if (to instanceof FromHeader) {
            assertThat(((ToHeader) to).getTag().toString(), is("asdf"));
        }
    }

    /**
     * Make sure clone works.
     * 
     * @throws Exception
     */
    @Test
    public void testClone() throws Exception {
        final AddressParametersHeader header = frameHeader(Buffers.wrap("sip:alice@example.com"));
        final AddressParametersHeader clone = (AddressParametersHeader) header.clone();
        assertThat(header.toString(), is(clone.toString()));
        final Address a1 = header.getAddress();
        final Address a2 = clone.getAddress();

        assertThat(a1.toString(), is(a2.toString()));
        ((SipURI) a1.getURI()).setPort(876);
        ((SipURI) a2.getURI()).setPort(7777);

        assertThat(a1.toString().contains("876"), is(true));
        assertThat(a2.toString().contains("7777"), is(true));
    }

    /**
     * Test to make sure we can get out the address portion of the To-header.
     * 
     * @throws Exception
     */
    @Test
    public void testGetAddress() throws Exception {
        AddressParametersHeader to = frameHeader(Buffers.wrap("sip:alice@example.com"));
        assertThat(to.getAddress(), not((Address) null));
        assertThat(to.getAddress().getDisplayName().isEmpty(), is(true));

        to = frameHeader(Buffers.wrap("<sip:alice@example.com>"));
        assertThat(to.getAddress().getDisplayName().isEmpty(), is(true));

        to = frameHeader(Buffers.wrap("alice <sip:alice@example.com>;hello=world;lr;apa=monkey"));
        assertThat(to.getAddress().getDisplayName().toString(), is("alice"));
        assertThat(to.getParameter("lr").isEmpty(), is(true));
        assertThat(to.getParameter("hello").toString(), is("world"));
        assertThat(to.getParameter("apa").toString(), is("monkey"));
    }

}
