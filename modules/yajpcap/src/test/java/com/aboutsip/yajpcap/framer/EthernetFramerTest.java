/**
 * 
 */
package com.aboutsip.yajpcap.framer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.yajpcap.YajTestBase;
import com.aboutsip.yajpcap.frame.EthernetFrame;

/**
 * @author jonas@jonasborjesson.com
 */
public class EthernetFramerTest extends YajTestBase {

    /**
     * @throws java.lang.Exception
     */
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    /**
     * @throws java.lang.Exception
     */
    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }


    @Test
    public void testEthernetFramer() throws IOException {
        final EthernetFramer framer = new EthernetFramer(this.framerManager);
        final EthernetFrame frame = (EthernetFrame) framer.frame(this.ethernetFrameBuffer);
        assertThat(frame, is(not((EthernetFrame) null)));
        assertThat(frame.getRawDestinationMacAddress(), is(not((Buffer) null)));
        assertThat(frame.getRawSourceMacAddress(), is(not((Buffer) null)));
        assertThat(frame.getSourceMacAddress(), is("00:00:00:00:00:00"));
        assertThat(frame.getDestinationMacAddress(), is("00:00:00:00:00:00"));
    }

}
