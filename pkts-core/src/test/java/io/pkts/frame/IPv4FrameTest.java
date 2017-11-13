/**
 * 
 */
package io.pkts.frame;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import io.pkts.PktsTestBase;
import io.pkts.framer.IPv4Framer;
import io.pkts.packet.IPv4Packet;
import io.pkts.packet.MACPacket;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public class IPv4FrameTest extends PktsTestBase {

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
    public void testParsePacket() throws Exception {
        final IPv4Framer framer = new IPv4Framer();
        final IPv4Packet p = framer.frame(mock(MACPacket.class), this.ipv4FrameBuffer);
        assertThat(p.getDestinationIP(), is("127.0.0.1"));
        assertThat(p.getSourceIP(), is("127.0.0.1"));
    }
}
