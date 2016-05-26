package io.pkts.framer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import io.pkts.RawData;
import io.pkts.PktsTestBase;
import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.MACPacket;
import io.pkts.packet.PCapPacket;
import io.pkts.packet.Packet;
import io.pkts.packet.sip.SipPacket;
import io.pkts.protocol.Protocol;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SllFramerTest extends PktsTestBase {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * The SLL framer is a fake frame kind of for when capturing on any on linux
     * boxes. Make sure we can read this stuff...
     * 
     * @throws Exception
     */
    @Test
    public void testFrame() throws Exception {

        final SllFramer framer = new SllFramer();
        final Buffer buffer = Buffers.wrap(RawData.rawSLLFrame);
        assertThat(framer.accept(buffer), is(true));

        final MACPacket pkt = framer.frame(mock(PCapPacket.class), buffer);
        assertThat(pkt.getSourceMacAddress(), is("12:31:38:1B:7B:73"));
        assertThat(pkt.getDestinationMacAddress(), is("00:04:00:01:00:06"));

        final Packet ipFrame = pkt.getNextPacket();
        assertThat(ipFrame.getProtocol(), is(Protocol.IPv4));
        final Packet sipFrame = pkt.getPacket(Protocol.SIP);
        assertThat(sipFrame, not((Packet) null));
        assertThat(sipFrame.getProtocol(), is(Protocol.SIP));
        final SipPacket msg = (SipPacket) sipFrame;
        assertThat(msg.getMethod().toString(), is("INVITE"));
    }

}
