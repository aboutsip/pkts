/**
 * 
 */
package io.pkts.framer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import io.pkts.RawData;
import io.pkts.PktsTestBase;
import io.pkts.buffer.Buffers;
import io.pkts.packet.IPPacket;
import io.pkts.packet.MACPacket;
import io.pkts.packet.PCapPacket;
import io.pkts.protocol.Protocol;

import org.junit.Test;

/**
 * @author jonas@jonasborjesson.com
 */
public class IPFramerTest extends PktsTestBase {

    @Test(expected = IllegalArgumentException.class)
    public void testIPFramerNoParent() throws Exception {
        final IPv4Framer framer = new IPv4Framer();
        framer.frame(null, this.ipv4FrameBuffer);
    }

    @Test
    public void testFragmentedIpPacket() throws Exception {
        final EthernetFramer framer = new EthernetFramer();
        final MACPacket frame = framer.frame(mock(PCapPacket.class), Buffers.wrap(RawData.fragmented));
        final IPPacket ip = (IPPacket) frame.getPacket(Protocol.IPv4);
        assertThat(ip.isMoreFragmentsSet(), is(true));
    }

}
