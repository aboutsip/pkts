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
import com.aboutsip.yajpcap.packet.sip.header.Parameters;

/**
 * @author jonas@jonasborjesson.com
 */
public class ParametersTest {

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
     * 
     */
    @Test
    public void testParseParameters() throws Exception {
        final Buffer name = Buffers.wrap("Whatever");
        final Buffer value = Buffers.wrap("hello;foo=boo");
        final Parameters params = new ParametersImpl(name, value) {
        };

        assertThat(params.getParameter("foo").toString(), is("boo"));
    }

}
