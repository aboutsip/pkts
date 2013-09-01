/**
 * 
 */
package io.pkts.framer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import io.pkts.PktsTestBase;
import io.pkts.packet.IPPacket;
import io.pkts.packet.UDPPacket;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public class UDPFramerTest extends PktsTestBase {

    /**
     * {@inheritDoc}
     */
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testUdpFramer() throws Exception {
        final UDPFramer framer = new UDPFramer();
        final UDPPacket udp = framer.frame(mock(IPPacket.class), this.udpFrameBuffer);
        assertThat(udp.getSourcePort(), is(5060));
        assertThat(udp.getDestinationPort(), is(5090));
    }

}
