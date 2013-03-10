/**
 * 
 */
package com.aboutsip.yajpcap.framer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.aboutsip.buffer.Buffers;
import com.aboutsip.yajpcap.RawData;
import com.aboutsip.yajpcap.YajTestBase;
import com.aboutsip.yajpcap.frame.Frame;
import com.aboutsip.yajpcap.frame.IPFrame;
import com.aboutsip.yajpcap.frame.Layer1Frame;
import com.aboutsip.yajpcap.packet.Packet;
import com.aboutsip.yajpcap.protocol.Protocol;

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

    @Test
    public void testFragmentedIpPacket() throws Exception {
        final Layer1Frame parent = mock(Layer1Frame.class);
        final Packet layer1Pkt = mock(Packet.class);
        when(parent.parse()).thenReturn(layer1Pkt);
        final EthernetFramer framer = new EthernetFramer(this.framerManager);
        final Frame frame = framer.frame(parent, Buffers.wrap(RawData.fragmented));
        final IPFrame ipFrame = (IPFrame) frame.getFrame(Protocol.IPv4);
        System.out.println(ipFrame);
    }

}
