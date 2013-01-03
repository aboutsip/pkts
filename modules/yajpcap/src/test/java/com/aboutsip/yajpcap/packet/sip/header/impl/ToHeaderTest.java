/**
 * 
 */
package com.aboutsip.yajpcap.packet.sip.header.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.buffer.Buffers;
import com.aboutsip.yajpcap.packet.sip.address.Address;
import com.aboutsip.yajpcap.packet.sip.header.ToHeader;

/**
 * @author jonas@jonasborjesson.com
 */
public class ToHeaderTest {

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
     * Make sure that the parameters of the To-header is correctly recognized.
     * 
     * @throws Exception
     */
    @Test
    public void testParameters() throws Exception {
        ToHeader to = ToHeaderImpl.parseValue(Buffers.wrap("sip:alice@example.com;hello=world;apa=monkey"));
        assertThat(to.getName().toString(), is("To"));
        assertThat(to.getTag(), is((Buffer) null));
        assertThat(to.getParameter("hello").toString(), is("world"));
        assertThat(to.getParameter("apa").toString(), is("monkey"));

        // make sure we can fetch them again...
        assertThat(to.getParameter("apa").toString(), is("monkey"));
        assertThat(to.getParameter("hello").toString(), is("world"));
        assertThat(to.getTag(), is((Buffer) null));

        // check the tag...
        to = ToHeaderImpl.parseValue(Buffers.wrap("sip:alice@example.com;hello=world;tag=asdf;apa=monkey"));
        assertThat(to.getTag().toString(), is("asdf"));
        to = ToHeaderImpl.parseValue(Buffers.wrap("sip:alice@example.com;tag=asdf;lr"));
        assertThat(to.getTag().toString(), is("asdf"));

        // check the flag parameter loose route...
        assertThat(to.getParameter("lr").isEmpty(), is(true));
        assertThat(to.getParameter("lr").toString(), is(""));

        to = ToHeaderImpl.parseValue(Buffers.wrap("sip:alice@example.com;lr"));
        assertThat(to.getParameter("lr").isEmpty(), is(true));

        to = ToHeaderImpl.parseValue(Buffers.wrap("sip:alice@example.com;fup;lr;apa"));
        assertThat(to.getParameter("lr").isEmpty(), is(true));
        assertThat(to.getParameter("fup").isEmpty(), is(true));
        assertThat(to.getParameter("apa").isEmpty(), is(true));
    }

    /**
     * Test to make sure we can get out the address portion of the To-header.
     * 
     * @throws Exception
     */
    @Test
    public void testGetAddress() throws Exception {
        final ToHeader to = ToHeaderImpl.parseValue(Buffers.wrap("sip:alice@example.com"));
        final Address address = to.getAddress();
    }

}
