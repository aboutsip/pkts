/**
 * 
 */
package io.pkts.frame;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import io.pkts.PktsTestBase;
import io.pkts.framer.EthernetFramer;
import io.pkts.framer.PcapFramer;
import io.pkts.packet.MACPacket;
import io.pkts.packet.PCapPacket;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author jonas
 * 
 */
public class EthernetFrameTest extends PktsTestBase {

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
        final PcapFramer pcapFramer = new PcapFramer(this.defaultPcapHeader, this.framerManager);
        final PCapPacket pcap = pcapFramer.frame(null, this.pcapStream);

        final EthernetFramer framer = new EthernetFramer();
        final MACPacket packet = framer.frame(pcap, pcap.getPayload());
        assertThat(packet.getSourceMacAddress(), is("00:00:00:00:00:00"));
        assertThat(packet.getDestinationMacAddress(), is("00:00:00:00:00:00"));
        assertThat(packet.getArrivalTime(), is(1340495109792862L));
    }

}
