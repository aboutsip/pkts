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
import com.aboutsip.yajpcap.frame.IPv4Frame;
import com.aboutsip.yajpcap.framer.IPv4Framer;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public class IPFramerTest extends YajTestBase {

    /**
     * The default buffer containing our IP frame
     */
    // private Buffer defaultIPFrame;

    /**
     * {@inheritDoc}
     */
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        // parse the default ethernet frame and grab the
        // payload in that frame which contains our IP frame
        // final EthernetFramer ethernetFramer = new
        // EthernetFramer(this.framerManager);
        // final EthernetFrame frame = (EthernetFrame)
        // ethernetFramer.frame(this.defaultFrame);
        // this.defaultIPFrame = frame.getData();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * The basic test for framing an IP packet
     */
    @Test
    public void testIPFramer() throws Exception {
        final IPv4Framer framer = new IPv4Framer(this.framerManager);
        final IPv4Frame frame = (IPv4Frame) framer.frame(this.ipv4FrameBuffer);
        assertThat(frame.getDestinationIp(), is("127.0.0.1"));
        assertThat(frame.getSourceIp(), is("127.0.0.1"));
    }


}
