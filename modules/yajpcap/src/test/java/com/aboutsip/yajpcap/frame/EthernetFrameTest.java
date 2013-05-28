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
import com.aboutsip.yajpcap.framer.EthernetFramer;
import com.aboutsip.yajpcap.framer.Layer1Framer;
import com.aboutsip.yajpcap.framer.PcapFramer;
import com.aboutsip.yajpcap.packet.MACPacket;

/**
 * @author jonas
 * 
 */
public class EthernetFrameTest extends YajTestBase {

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
    }

    /**
     * Make sure we can parse a Ethernet packet, which is a pretty simple thing
     * and is pretty much already done by the frame itself. However, make sure
     * we haven't screwed this up
     * 
     * @throws Exception
     */
    @Test
    public void testParsePacket() throws Exception {
        final Layer1Framer pcapFramer = new PcapFramer(this.defaultPcapHeader, this.framerManager);
        final Layer1Frame pcapFrame = pcapFramer.frame(null, this.pcapStream);

        final EthernetFramer framer = new EthernetFramer(this.framerManager);
        final EthernetFrame frame = framer.frame(pcapFrame, pcapFrame.getPayload());

        final MACPacket packet = frame.parse();
        assertThat(packet.getSourceMacAddress(), is("00:00:00:00:00:00"));
        assertThat(packet.getDestinationMacAddress(), is("00:00:00:00:00:00"));
        assertThat(packet.getArrivalTime(), is(1340495109792862L));
    }

}
