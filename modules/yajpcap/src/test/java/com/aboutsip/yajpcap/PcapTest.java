package com.aboutsip.yajpcap;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.aboutsip.yajpcap.frame.Frame;
import com.aboutsip.yajpcap.frame.SipFrame;
import com.aboutsip.yajpcap.packet.PacketParseException;
import com.aboutsip.yajpcap.packet.sip.SipMessage;
import com.aboutsip.yajpcap.protocol.Protocol;

public class PcapTest extends YajTestBase {

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

    @Test
    public void testLoop() throws Exception {
        // there are 30 packets in this capture.
        final InputStream stream = YajTestBase.class.getResourceAsStream("sipp.pcap");
        final Pcap pcap = Pcap.openStream(stream);
        final FrameHandlerImpl handler = new FrameHandlerImpl();
        pcap.loop(handler);
        pcap.close();
        assertThat(handler.count, is(30));
    }

    /**
     * Test the writing abilities of the library.
     * 
     * This test will load a sipp capture and then only save INVITE and BYE
     * requests from that capture. To verify that we indeed have saved
     * everything correctly we will save the output to a memory buffer from
     * which we will then read it back and count what is in the stream.
     * 
     * @throws Exception
     */
    @Test
    public void testWriteFramesBackToStream() throws Exception {
        InputStream stream = YajTestBase.class.getResourceAsStream("sipp.pcap");
        Pcap pcap = Pcap.openStream(stream);
        // final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream("cool.pcap"));
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final PcapOutputStream pcapStream = pcap.createOutputStream(out);
        final TestWriteStreamHandler handler = new TestWriteStreamHandler(pcapStream);
        pcap.loop(handler);
        pcap.close();
        out.flush();
        out.close();

        stream = new ByteArrayInputStream(out.toByteArray());
        pcap = Pcap.openStream(stream);
        final MethodCalculator calculator = new MethodCalculator();
        pcap.loop(calculator);
        pcap.close();

        // should only be 10 sip packets in total. 5 invites and 5 byes
        assertThat(calculator.total, is(10));
        assertThat(calculator.invite, is(5));
        assertThat(calculator.bye, is(5));
        assertThat(calculator.ack, is(0)); // i guess un-necessary check...
        assertThat(calculator.cancel, is(0)); // i guess un-necessary check...
    }

    private static class MethodCalculator implements FrameHandler {
        public int total;
        public int invite;
        public int bye;
        public int ack;
        public int cancel;

        @Override
        public void nextFrame(final Frame frame) {
            try {
                final SipFrame sipFrame = (SipFrame) frame.getFrame(Protocol.SIP);
                final SipMessage msg = sipFrame.parse();
                ++this.total;
                if (msg.isRequest()) {
                    if (msg.isInvite()) {
                        ++this.invite;
                    } else if (msg.isBye()) {
                        ++this.bye;
                    } else if (msg.isAck()) {
                        ++this.ack;
                    } else if (msg.isCancel()) {
                        ++this.cancel;
                    }
                }
            } catch (final IOException e) {
                fail("Got an IOException in my test " + e.getMessage());
            } catch (final PacketParseException e) {
                fail("Got a PacketParseException in my test " + e.getMessage());
            }

        }

    }

    private static class TestWriteStreamHandler implements FrameHandler {

        private final PcapOutputStream out;
        private final int count = 0;

        public TestWriteStreamHandler(final PcapOutputStream out) {
            this.out = out;
        }

        @Override
        public void nextFrame(final Frame frame) {
            try {

                // only write out INVITE and BYE requests
                final SipFrame sipFrame = (SipFrame) frame.getFrame(Protocol.SIP);
                final SipMessage msg = sipFrame.parse();
                if (msg.isRequest() && "INVITE".equals(msg.getMethod().toString())) {
                    this.out.write(sipFrame);
                } else if (msg.isRequest() && "BYE".equals(msg.getMethod().toString())) {
                    this.out.write(sipFrame);
                }
            } catch (final IOException e) {
                fail("Got an IOException in my test " + e.getMessage());
            } catch (final PacketParseException e) {
                fail("Got a PacketParseException in my test " + e.getMessage());
            }
        }
    }

    private static class FrameHandlerImpl implements FrameHandler {
        public int count;

        @Override
        public void nextFrame(final Frame frame) {
            ++this.count;
        }
    }

}
