/**
 * 
 */
package com.aboutsip.yajpcap.framer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.aboutsip.yajpcap.YajTestBase;
import com.aboutsip.yajpcap.frame.UDPFrame;
import com.aboutsip.yajpcap.framer.UDPFramer;

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
        final UDPFramer framer = new UDPFramer(this.framerManager);
        final UDPFrame frame = (UDPFrame) framer.frame(this.udpFrameBuffer);
        assertThat(frame.getSourcePort(), is(5060));
        assertThat(frame.getDestinationPort(), is(5090));
    }

}
