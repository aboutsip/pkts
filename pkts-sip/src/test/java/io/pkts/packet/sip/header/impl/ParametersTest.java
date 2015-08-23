/**
 * 
 */
package io.pkts.packet.sip.header.impl;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.header.Parameters;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;


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
        final Parameters params = new ParametersImpl(Buffers.wrap("not-used-just-yet"), name, value) {
        };

        assertThat(params.getParameter("foo").toString(), is("boo"));
        params.setParameter(Buffers.wrap("monkey"), Buffers.wrap("apa"));
        assertThat(params.getParameter("monkey").toString(), is("apa"));

        assertThat(params.getParameter("hello").toString(), is(""));
    }

}
