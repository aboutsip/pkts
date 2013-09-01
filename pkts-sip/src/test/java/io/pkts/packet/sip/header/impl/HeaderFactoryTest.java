/**
 * 
 */
package io.pkts.packet.sip.header.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import io.pkts.packet.sip.header.HeaderFactory;
import io.pkts.packet.sip.header.ViaHeader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public class HeaderFactoryTest {

    private HeaderFactory factory;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        this.factory = new HeaderFactoryImpl();
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testCreateViaHeader() {
        final String branch = "asdf-asdf-asdf";
        final ViaHeader via = this.factory.createViaHeader("127.0.0.1", 5088, "TCP", branch);
        assertThat(via.getHost().toString(), is("127.0.0.1"));
        assertThat(via.getPort(), is(5088));
        assertThat(via.getBranch().toString(), is(branch));
        assertThat(via.toString(), is("Via: SIP/2.0/TCP 127.0.0.1:5088;branch=asdf-asdf-asdf"));
    }

}
