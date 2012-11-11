/**
 * 
 */
package com.aboutsip.yajpcap.framer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.aboutsip.yajpcap.YajTestBase;

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

    @Test(expected = IllegalArgumentException.class)
    public void testIPFramerNoParent() throws Exception {
        final IPv4Framer framer = new IPv4Framer(this.framerManager);
        framer.frame(null, this.ipv4FrameBuffer);
    }


}
