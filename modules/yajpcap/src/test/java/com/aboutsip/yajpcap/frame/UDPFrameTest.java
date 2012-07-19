/**
 * 
 */
package com.aboutsip.yajpcap.frame;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.aboutsip.yajpcap.YajTestBase;
import com.aboutsip.yajpcap.framer.UDPFramer;
import com.aboutsip.yajpcap.packet.TransportPacket;

/**
 * @author jonas@jonasborjesson.com
 */
public class UDPFrameTest extends YajTestBase {

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
        final UDPFramer framer = new UDPFramer(this.framerManager);
        final UDPFrame frame = framer.frame(this.udpFrameBuffer);
        final TransportPacket p = frame.parse();
        assertThat(p.getSourcePort(), is(5060));
        assertThat(p.getDestinationPort(), is(5090));
        assertThat(p.isUDP(), is(true));
    }

}
