package io.pkts.framer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import io.pkts.RawData;
import io.pkts.YajTestBase;
import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.frame.Frame;
import io.pkts.frame.Layer1Frame;
import io.pkts.frame.SllFrame;
import io.pkts.frame.EthernetFrame.EtherType;
import io.pkts.framer.SllFramer;
import io.pkts.packet.MACPacket;
import io.pkts.packet.Packet;
import io.pkts.packet.sip.SipMessage;
import io.pkts.protocol.Protocol;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class SllFramerTest extends YajTestBase {

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
     * boxes. Make sure we can read this
     * stuff...
     * 
     * @throws Exception
     */
    @Test
    public void testFrame() throws Exception {
        final Layer1Frame parent = mock(Layer1Frame.class);
        final Packet layer1Pkt = mock(Packet.class);
        when(parent.parse()).thenReturn(layer1Pkt);
        final SllFramer framer = new SllFramer(this.framerManager);
        final Buffer buffer = Buffers.wrap(RawData.rawSLLFrame);
        assertThat(framer.accept(buffer), is(true));
        final SllFrame frame = (SllFrame) framer.frame(parent, buffer);
        assertThat(frame, not((SllFrame) null));
        assertThat(frame.getType(), is(EtherType.IPv4));

        final MACPacket pkt = frame.parse();
        assertThat(pkt.getDestinationMacAddress(), is("12:31:38:1B:7B:73"));
        assertThat(pkt.getSourceMacAddress(), is("12:31:38:1B:7B:73"));

        final Frame ipFrame = frame.getNextFrame();
        assertThat(ipFrame.getProtocol(), is(Protocol.IPv4));
        final Frame sipFrame = frame.getFrame(Protocol.SIP);
        assertThat(sipFrame, not((Frame) null));
        assertThat(sipFrame.getProtocol(), is(Protocol.SIP));
        final SipMessage msg = (SipMessage) sipFrame.parse();
        assertThat(msg.getMethod().toString(), is("INVITE"));
    }

}
