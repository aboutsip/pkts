/**
 * 
 */
package io.pkts.framer;

import io.pkts.PktsTestBase;

import io.pkts.buffer.Buffer;
import io.pkts.packet.PCapPacket;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
            frame.setByte(12, t.b1);
            frame.setByte(13, t.b2);
            PCapPacket parent = mock(PCapPacket.class);
            this.framer.frame(parent, frame);
        }
    }
}
