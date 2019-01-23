/**
 * 
 */
package io.pkts.framer;

import io.pkts.RawData;
import io.pkts.buffer.Buffers;
import io.pkts.packet.IPv4Packet;
import io.pkts.packet.MACPacket;
import io.pkts.packet.Packet;
import io.pkts.protocol.Protocol;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.pkts.PktsTestBase;
import io.pkts.buffer.Buffer;
import io.pkts.packet.PCapPacket;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author jonas@jonasborjesson.com
 */
public class EthernetFramerTest extends PktsTestBase {

    private EthernetFramer framer;

    /**
     * @throws java.lang.Exception
     */
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.framer = new EthernetFramer();
    }

    /**
     * @throws java.lang.Exception
     */
    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEthernetFramerNoParent() throws Exception {
        this.framer.frame(null, this.ethernetFrameBuffer);
    }

    @Test
    public void testAcceptsAllEtherTypes() throws Exception {
        for (EthernetFramer.EtherType t : EthernetFramer.EtherType.values()) {
            Buffer frame = this.ethernetFrameBuffer.slice();
            frame.setByte(12, t.getB1());
            frame.setByte(13, t.getB2());
            PCapPacket parent = mock(PCapPacket.class);
            this.framer.frame(parent, frame);
        }
    }

    @Test
    public void testVlanFrame() throws Exception {
        final EthernetFramer framer = new EthernetFramer();
        Buffer frameBuffer = Buffers.wrap(RawData.rawEthernetVlanFrame);
        final MACPacket frame = framer.frame(mock(PCapPacket.class), frameBuffer);
        // ensure that this frame is indeed a VLAN frame
        assertThat(EthernetFramer.getEtherType(frameBuffer.getByte(12), frameBuffer.getByte(13)),
                is(equalTo(EthernetFramer.EtherType.Dot1Q)));

        // then ensure that we can traverse the frame
        assertThat(frame.getNextPacket().getProtocol(), is(equalTo(Protocol.IPv4)));
    }
}
