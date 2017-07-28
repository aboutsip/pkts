/**
 * 
 */
package io.pkts.streams.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import io.pkts.Pcap;
import io.pkts.packet.Packet;
import io.pkts.packet.rtp.RtpPacket;
import io.pkts.packet.sip.SipPacket;
import io.pkts.packet.sip.SipPacketParseException;
import io.pkts.streams.SipStream;
import io.pkts.streams.Stream;
import io.pkts.streams.StreamHandler;
import io.pkts.streams.StreamListener;
import io.pkts.streams.StreamsTestBase;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public class DefaultStreamHandlerTest extends StreamsTestBase {

    /**
     * @throws java.lang.Exception
     */
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testRtpStream() throws Exception {
        final Pcap pcap = Pcap.openStream(StreamsTestBase.class.getResourceAsStream("sip_rtp.pcap"));
        final StreamHandler streamHandler = new DefaultStreamHandler();
        final RtpCounter streamCounter = new RtpCounter();
        streamHandler.addStreamListener(streamCounter);
        pcap.loop(streamHandler);
        pcap.close();
        assertThat(streamCounter.startCount, is(1));
        assertThat(streamCounter.packetCount, is(501));
        assertThat(streamCounter.endCount, is(0)); // because we do not detect end events
    }

    /**
     * Simple test so that we do not blow up on RTP and RTCP when we scan for SIP traffic (which we
     * did for a while)
     * 
     * @throws Exception
     */
    @Test
    public void testSipStreamContainingRTPandRTCP() throws Exception {
        final Pcap pcap = Pcap.openStream(StreamsTestBase.class.getResourceAsStream("sip_rtp.pcap"));
        final StreamHandler streamHandler = new DefaultStreamHandler();
        final StreamCounter streamCounter = new StreamCounter();
        streamHandler.addStreamListener(streamCounter);
        pcap.loop(streamHandler);
        pcap.close();
    }

    /**
     * The sipp.pcap contains very simple and basic SIP INVITE scenario. It is
     * perfect in that sense that there are no missing packets and all INVITE
     * scenarios actually start with the INVITE.
     * 
     * @throws Exception
     */
    @Test
    public void testBasicSipStreamDetection() throws Exception {

        final Pcap pcap = Pcap.openStream(StreamsTestBase.class.getResourceAsStream("sipp.pcap"));
        final StreamHandler streamHandler = new DefaultStreamHandler();
        final StreamCounter streamCounter = new StreamCounter();
        streamHandler.addStreamListener(streamCounter);
        pcap.loop(streamHandler);
        pcap.close();
        assertThat(streamCounter.startCount, is(5));
        assertThat(streamCounter.packetCount, is(30));
        assertThat(streamCounter.endCount, is(5));

        // the following values have been verified using wireshark
        assertStream(streamCounter.streams.get(0), 408, 1002719);
        assertStream(streamCounter.streams.get(1), 400, 1003484);
        assertStream(streamCounter.streams.get(2), 315, 1002358);
        assertStream(streamCounter.streams.get(3), 369, 1001320);
        assertStream(streamCounter.streams.get(4), 470, 1002963);
    }

    /**
     * Make sure that the {@link SipStream} has the expected PDD etc.
     * 
     * @param stream
     * @param pdd
     *            the expected PDD
     * @param duration
     *            the expected duration
     * @throws SipPacketParseException
     */
    private void assertStream(final Stream<SipPacket> stream, final long pdd, final long duration)
            throws SipPacketParseException {
        final SipStream sipStream = (SipStream) stream;
        assertThat(sipStream.getPostDialDelay(), is(pdd));
        assertThat(sipStream.getDuration(), is(duration));
    }

    /**
     * We are doing some reflection magic in order to determine the type of the
     * supplied stream and if the user hasn't parameterized the
     * {@link StreamListener} then we cannot operate so complain.
     * 
     * @throws Exception
     */
    @SuppressWarnings({
        "unchecked", "rawtypes" })
    @Test(expected = IllegalArgumentException.class)
    public void testNotParamterizedStreamListener() throws Exception {
        final StreamHandler streamHandler = new DefaultStreamHandler();
        streamHandler.addStreamListener(new StreamListener() {

            @Override
            public void startStream(final Stream stream, final Packet packet) {
                // TODO Auto-generated method stub

            }

            @Override
            public void packetReceived(final Stream stream, final Packet packet) {
                // TODO Auto-generated method stub

            }

            @Override
            public void endStream(final Stream stream) {
                // TODO Auto-generated method stub

            }
        });
    }

    /**
     * Basic {@link StreamListener} that just counts the number of start, stop and packet events we
     * receive.
     * 
     */
    public static class RtpCounter implements StreamListener<RtpPacket> {

        public List<Stream<RtpPacket>> streams = new ArrayList<Stream<RtpPacket>>();

        public int startCount;
        public int packetCount;
        public int endCount;

        @Override
        public void startStream(final Stream<RtpPacket> stream, final RtpPacket packet) {
            System.out.println("Start Stream: " + stream.getStreamIdentifier());
            this.streams.add(stream);
            ++this.startCount;
            ++this.packetCount;
        }

        @Override
        public void packetReceived(final Stream<RtpPacket> stream, final RtpPacket packet) {
            ++this.packetCount;
        }

        @Override
        public void endStream(final Stream<RtpPacket> stream) {
            ++this.endCount;
        }
    }

    /**
     * Basic {@link StreamListener} that just counts the number of start, stop
     * and packet events we receive.
     * 
     */
    public static class StreamCounter implements StreamListener<SipPacket> {

        public List<Stream<SipPacket>> streams = new ArrayList<Stream<SipPacket>>();

        public int startCount;
        public int packetCount;
        public int endCount;

        public List<Stream<SipPacket>> getStreams() {
            return this.streams;
        }

        public Stream<SipPacket> getFirstStream() {
            return this.streams.get(0);
        }

        @Override
        public void startStream(final Stream<SipPacket> stream, final SipPacket packet) {
            this.streams.add(stream);
            ++this.startCount;
            ++this.packetCount;
        }

        @Override
        public void packetReceived(final Stream<SipPacket> stream, final SipPacket packet) {
            ++this.packetCount;
        }

        @Override
        public void endStream(final Stream<SipPacket> stream) {
            ++this.endCount;
        }

    }

}
