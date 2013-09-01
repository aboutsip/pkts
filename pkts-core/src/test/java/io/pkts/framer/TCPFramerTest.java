/**
 * 
 */
package io.pkts.framer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import io.pkts.FragmentedTCPData;
import io.pkts.RawData;
import io.pkts.PktsTestBase;
import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.IPPacket;
import io.pkts.packet.TCPPacket;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public class TCPFramerTest extends PktsTestBase {
    private TCPFramer framer;

    /**
     * {@inheritDoc}
     */
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.framer = new TCPFramer();
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
    public void testFinFlag() throws Exception {
        assertThat(getFrame(FragmentedTCPData.segmentOne).isFIN(), is(false));
        assertThat(getFrame(FragmentedTCPData.segmentTwo).isFIN(), is(false));
        assertThat(getFrame(FragmentedTCPData.segmentThree).isFIN(), is(false));
    }

    @Test
    public void testSynFlag() throws Exception {
        assertThat(getFrame(FragmentedTCPData.segmentOne).isSYN(), is(false));
        assertThat(getFrame(FragmentedTCPData.segmentTwo).isSYN(), is(false));
        assertThat(getFrame(FragmentedTCPData.segmentThree).isSYN(), is(false));
        assertThat(getFrame(RawData.tcpSyn).isSYN(), is(true));
        assertThat(getFrame(RawData.tcpSynAck).isSYN(), is(true));
    }

    @Test
    public void testRstFlag() throws Exception {
        assertThat(getFrame(FragmentedTCPData.segmentOne).isRST(), is(false));
        assertThat(getFrame(FragmentedTCPData.segmentTwo).isRST(), is(false));
        assertThat(getFrame(FragmentedTCPData.segmentThree).isRST(), is(false));
    }

    @Test
    public void testPSHFlag() throws Exception {
        assertThat(getFrame(FragmentedTCPData.segmentOne).isPSH(), is(false));
        assertThat(getFrame(FragmentedTCPData.segmentTwo).isPSH(), is(false));
        assertThat(getFrame(FragmentedTCPData.segmentThree).isPSH(), is(true));
    }

    @Test
    public void testAckFlag() throws Exception {
        assertThat(getFrame(FragmentedTCPData.segmentOne).isACK(), is(true));
        assertThat(getFrame(FragmentedTCPData.segmentTwo).isACK(), is(true));
        assertThat(getFrame(FragmentedTCPData.segmentThree).isACK(), is(true));
        assertThat(getFrame(RawData.tcpSynAck).isACK(), is(true));
    }

    @Test
    public void testUrgFlag() throws Exception {
        assertThat(getFrame(FragmentedTCPData.segmentOne).isURG(), is(false));
        assertThat(getFrame(FragmentedTCPData.segmentTwo).isURG(), is(false));
        assertThat(getFrame(FragmentedTCPData.segmentThree).isURG(), is(false));
    }

    @Test
    public void testEceFlag() throws Exception {
        assertThat(getFrame(FragmentedTCPData.segmentOne).isECE(), is(false));
        assertThat(getFrame(FragmentedTCPData.segmentTwo).isECE(), is(false));
        assertThat(getFrame(FragmentedTCPData.segmentThree).isECE(), is(false));
    }

    @Test
    public void testCwrFlag() throws Exception {
        assertThat(getFrame(FragmentedTCPData.segmentOne).isCWR(), is(false));
        assertThat(getFrame(FragmentedTCPData.segmentTwo).isCWR(), is(false));
        assertThat(getFrame(FragmentedTCPData.segmentThree).isCWR(), is(false));
    }

    private TCPPacket getFrame(final byte[] data) throws Exception {
        // final IPPacket ipFrame = mock(IPFrame.class);
        // final IPPacket ip = mock(IPPacket.class);
        // when(ipFrame.parse()).thenReturn(ip);

        final Buffer buf = Buffers.wrap(data);
        final Buffer tcp = buf.slice(34, buf.capacity());
        return this.framer.frame(mock(IPPacket.class), tcp);
    }

    /**
     * A syn tcp packet
     * 
     * @throws Exception
     */
    @Test
    public void testTcpSynPacket() throws Exception {
        final TCPPacket tcp = getFrame(RawData.tcpSyn);
        assertThat(tcp.getSourcePort(), is(59409));
        assertThat(tcp.getDestinationPort(), is(5060));
        assertThat(tcp.getHeaderLength(), is(40));
    }

    @Test
    public void testTcpFramer() throws Exception {
        final TCPPacket tcp = this.framer.frame(mock(IPPacket.class), this.tcpFrameBuffer);
        assertThat(tcp.getSourcePort(), is(5060));
        assertThat(tcp.getDestinationPort(), is(59409));
        assertThat(tcp.getHeaderLength(), is(32));
    }

}
