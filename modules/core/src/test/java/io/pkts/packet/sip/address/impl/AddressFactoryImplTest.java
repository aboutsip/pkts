/**
 * 
 */
package io.pkts.packet.sip.address.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.address.AddressFactory;
import io.pkts.packet.sip.address.SipURI;
import io.pkts.packet.sip.address.impl.AddressFactoryImpl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * @author jonas@jonasborjesson.com
 * 
 */
public class AddressFactoryImplTest {

    private final AddressFactory factory = new AddressFactoryImpl();

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

    @Test
    public void testCreateSipURI() {
        final SipURI sipURI = this.factory.createSipURI(Buffers.wrap("alice"), Buffers.wrap("example.com"));
        assertThat(sipURI.getUser().toString(), is("alice"));
        assertThat(sipURI.getHost().toString(), is("example.com"));
        assertThat(sipURI.getPort(), is(-1));
        sipURI.setPort(5080);
        assertThat(sipURI.getPort(), is(5080));
    }

}
