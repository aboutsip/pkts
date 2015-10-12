/**
 * 
 */
package io.pkts.packet.sip.header.impl;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipParseException;
import io.pkts.packet.sip.address.Address;
import io.pkts.packet.sip.header.AddressParametersHeader;
import io.pkts.packet.sip.header.FromHeader;
import io.pkts.packet.sip.header.ToHeader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;


/**
 * The test base for testing all headers that are considered to be
 * {@link AddressParametersHeaderImpl}s, which include headers such as
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
    public abstract AddressParametersHeaderImpl frameHeader(final Buffer buffer) throws SipParseException;

    /**
     * To be overriden by subclasses.
     *
     * @param host
     * @return
     */
    public abstract AddressParametersHeader.Builder withHost(String host);

    private void assertGetBytes(final String expected, final AddressParametersHeaderImpl header) {
        final Buffer copy = Buffers.createBuffer(100);
        header.getBytes(copy);
        assertThat(copy.toString(), is(header.getName().toString() + ": " + expected));
    }

    /**
     * Ensure we can create an {@link AddressParametersHeader}-subclass correctly using builders and the
     * copy-constructor.
     */
    @Test
    public void testCreateHeaderUsingBuilder() throws Exception {
        final AddressParametersHeader to = withHost("hello.com").build();
        final String method = to.getName().toString();
        assertThat(to.toString(), is(method + ": sip:hello.com"));
        assertThat(to.getAddress().getURI().toString(), is("sip:hello.com"));

        final AddressParametersHeader t2 = to.copy().withUriParameter("foo", "woo").build();
        assertThat(t2.toString(), is(method + ": <sip:hello.com;foo=woo>"));

        final AddressParametersHeader t3 = to.copy().withParameter("nisse", "kalle").withPort(9999).build();
        assertThat(t3.toString(), is(method + ": sip:hello.com:9999;nisse=kalle"));

        assertThat(to.toString(), not(t2.toString()));
        assertThat(to.toString(), not(t3.toString()));
        assertThat(t2.toString(), not(t3.toString()));
    }

    /**
     * Make sure that the parameters of the header is correctly recognized.
     * 
     * @throws Exception
     */
    @Test
    public void testParameters() throws Exception {
        String s = "sip:alice@example.com;hello=world;apa=monkey";
        AddressParametersHeaderImpl to = frameHeader(Buffers.wrap(s));
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
        AddressParametersHeaderImpl to = frameHeader(Buffers.wrap("sip:alice@example.com;hello=world;apa=monkey"));
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

    @Test
    public void testCopy() throws Exception {
        final AddressParametersHeaderImpl h1 = frameHeader(Buffers.wrap("sip:alice@example.com;hello=world"));
        final AddressParametersHeader h2 = h1.copy().withHost("pkts.io").build();
        assertThat(h1.getValue().toString(), is("sip:alice@example.com;hello=world"));
        assertThat(h2.getValue().toString(), is("sip:alice@pkts.io;hello=world"));

        final AddressParametersHeader h3 = h1.copy().withUriParameter("apa", "monkey").withPort(7654)
                .withDisplayName("Apa").withNoParameters().withParameter("nisse", "kalle").build();
        assertThat(h3.getValue().toString(), is("Apa <sip:alice@example.com:7654;apa=monkey>;nisse=kalle"));

        // since everything is immutable, nothing of the other
        // headers should have been affected when building new headers.
        assertThat(h1.getValue(), not(h2.getValue()));
        assertThat(h1.getValue(), not(h3.getValue()));
        assertThat(h2.getValue(), not(h3.getValue()));
    }

    /**
     * Make sure clone works.
     * 
     * @throws Exception
     */
    @Test
    public void testClone() throws Exception {
        final AddressParametersHeaderImpl header = frameHeader(Buffers.wrap("sip:alice@example.com"));
        final AddressParametersHeaderImpl clone = (AddressParametersHeaderImpl) header.clone();
        assertThat(header.toString(), is(clone.toString()));
        Address a1 = header.getAddress();
        Address a2 = clone.getAddress();

        assertThat(a1.toString(), is(a2.toString()));

        a1 = a1.copy().withPort(876).build();
        a2 = a2.copy().withPort(7777).build();

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
        AddressParametersHeaderImpl to = frameHeader(Buffers.wrap("sip:alice@example.com"));
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

    @Test
    public void testEmptyDisplayName() throws Exception {
        final AddressParametersHeaderImpl to = frameHeader(Buffers.wrap("\"\" <sip:alice@example.com>;tag=asdf-asdf-asdf"));
        assertThat(to.getAddress().getDisplayName().toString(), is(""));
        assertThat(to.getParameter("lr"), is((Buffer)null));
        assertThat(to.getParameter("tag").toString(), is("asdf-asdf-asdf"));
    }

}
