/**
 * 
 */
package com.aboutsip.yajpcap.frame;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.aboutsip.yajpcap.YajTestBase;
import com.aboutsip.yajpcap.framer.IPv4Framer;
import com.aboutsip.yajpcap.packet.IPPacket;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public class IPFrameTest extends YajTestBase {

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
    public void testParsePacket() throws IOException {
        final IPv4Framer framer = new IPv4Framer(this.framerManager);
        final IPv4Frame frame = framer.frame(this.ipv4FrameBuffer);
        final IPPacket p = frame.parse();
        assertThat(p.getDestinationIP(), is("127.0.0.1"));
        assertThat(p.getSourceIP(), is("127.0.0.1"));
    }

}
