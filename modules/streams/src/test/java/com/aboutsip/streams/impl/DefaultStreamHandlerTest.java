/**
 * 
 */
package com.aboutsip.streams.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.aboutsip.streams.Stream;
import com.aboutsip.streams.StreamHandler;
import com.aboutsip.streams.StreamListener;
import com.aboutsip.streams.StreamsTestBase;
import com.aboutsip.yajpcap.Pcap;
import com.aboutsip.yajpcap.packet.Packet;
import com.aboutsip.yajpcap.packet.sip.SipMessage;

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
            public void startStream(final Stream stream) {
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
     * Basic {@link StreamListener} that just counts the number of start, stop
     * and packet events we receive.
     * 
     */
    public static class StreamCounter implements StreamListener<SipMessage> {

        public int startCount;
        public int packetCount;
        public int endCount;

        @Override
        public void startStream(final Stream<SipMessage> stream) {
            ++this.startCount;
        }

        @Override
        public void packetReceived(final Stream<SipMessage> stream, final SipMessage packet) {
            ++this.packetCount;
        }

        @Override
        public void endStream(final Stream<SipMessage> stream) {
            ++this.endCount;
        }

    }

}
