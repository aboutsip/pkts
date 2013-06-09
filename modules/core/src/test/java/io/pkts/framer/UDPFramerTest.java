/**
 * 
 */
package io.pkts.framer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import io.pkts.YajTestBase;
import io.pkts.frame.IPFrame;
import io.pkts.frame.UDPFrame;
import io.pkts.framer.UDPFramer;
import io.pkts.packet.IPPacket;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * @author jonas@jonasborjesson.com
 * 
 */
public class UDPFramerTest extends YajTestBase {

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
        final IPFrame ipFrame = mock(IPFrame.class);
        final IPPacket ip = mock(IPPacket.class);
        when(ipFrame.parse()).thenReturn(ip);

        final UDPFramer framer = new UDPFramer(this.framerManager);
        final UDPFrame frame = framer.frame(ipFrame, this.udpFrameBuffer);
        assertThat(frame.getSourcePort(), is(5060));
        assertThat(frame.getDestinationPort(), is(5090));
    }

}
