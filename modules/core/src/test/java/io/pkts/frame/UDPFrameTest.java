/**
 * 
 */
package io.pkts.frame;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import io.pkts.YajTestBase;
import io.pkts.frame.EthernetFrame;
import io.pkts.frame.IPv4Frame;
import io.pkts.frame.Layer1Frame;
import io.pkts.frame.UDPFrame;
import io.pkts.framer.EthernetFramer;
import io.pkts.framer.IPv4Framer;
import io.pkts.framer.Layer1Framer;
import io.pkts.framer.PcapFramer;
import io.pkts.framer.UDPFramer;
import io.pkts.packet.TransportPacket;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


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
        final Layer1Framer pcapFramer = new PcapFramer(this.defaultPcapHeader, this.framerManager);
        final Layer1Frame pcapFrame = pcapFramer.frame(null, this.pcapStream);

        final EthernetFramer ethFramer = new EthernetFramer(this.framerManager);
        final EthernetFrame ethFrame = ethFramer.frame(pcapFrame, pcapFrame.getPayload());

        final IPv4Framer ipv4Framer = new IPv4Framer(this.framerManager);
        final IPv4Frame ipv4Frame = ipv4Framer.frame(ethFrame, this.ipv4FrameBuffer);

        final UDPFramer framer = new UDPFramer(this.framerManager);
        final UDPFrame frame = framer.frame(ipv4Frame, this.udpFrameBuffer);
        final TransportPacket p = frame.parse();
        assertThat(p.getSourcePort(), is(5060));
        assertThat(p.getDestinationPort(), is(5090));

        assertThat(p.getDestinationIP(), is("127.0.0.1"));
        assertThat(p.getSourceIP(), is("127.0.0.1"));
        assertThat(p.getSourceMacAddress(), is("00:00:00:00:00:00"));
        assertThat(p.getDestinationMacAddress(), is("00:00:00:00:00:00"));
        assertThat(p.getArrivalTime(), is(1340495109792862L));
    }

}
