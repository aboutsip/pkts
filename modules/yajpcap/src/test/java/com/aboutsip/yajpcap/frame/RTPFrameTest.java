/**
 * 
 */
package com.aboutsip.yajpcap.frame;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.buffer.Buffers;
import com.aboutsip.yajpcap.RawData;
import com.aboutsip.yajpcap.YajTestBase;
import com.aboutsip.yajpcap.framer.SllFramer;
import com.aboutsip.yajpcap.packet.Packet;
import com.aboutsip.yajpcap.packet.rtp.RtpPacket;
import com.aboutsip.yajpcap.protocol.Protocol;

/**
 * @author jonas@jonasborjesson.com
 */
public class RTPFrameTest extends YajTestBase {

    /**
     * {@inheritDoc}
     */
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test frame an RTP packet all the way through.
     * 
     * @throws Exception
     */
    @Test
    public void testFrameRTP() throws Exception {
        final Layer1Frame parent = mock(Layer1Frame.class);
        final Packet packet = mock(Packet.class);
        when(parent.parse()).thenReturn(packet);

        final SllFramer framer = new SllFramer(this.framerManager);
        final Buffer buffer = Buffers.wrap(RawData.rtp);
        final Frame frame = framer.frame(parent, buffer);

        final Frame rtpFrame = frame.getFrame(Protocol.RTP);
        assertThat(rtpFrame, not((Frame) null));
        final RtpPacket rtp = (RtpPacket) rtpFrame.parse();
        assertThat(rtp.getVersion(), is(2));
        assertThat(rtp.hasExtensions(), is(false));
        assertThat(rtp.hasPadding(), is(false));
        assertThat(rtp.hasMarker(), is(false));
        assertThat(rtp.getSeqNumber(), is(20937));
        assertThat(rtp.getTimestamp(), is(8396320L));
    }

    @Ignore
    @Test
    public void testPlaySinusWave() throws Exception {

        final AudioFormat af = new AudioFormat(sampleRate, 16, 1, true, true);
        try {
            final SourceDataLine line = AudioSystem.getSourceDataLine(af);
            line.open(af);
            line.start();
            //play Frequency = 200 Hz for 1 seconds
            play(line, generateSineWavefreq(440, 10));
            line.drain();
            line.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }

    }

    private static int sampleRate = 8000;

    private static byte[] generateSineWavefreq(final int frequencyOfSignal, final int seconds) {
        // total samples = (duration in second) * (samples per second)
        final byte[] sin = new byte[seconds * sampleRate];
        final double samplingInterval = (sampleRate / frequencyOfSignal);
        System.out.println("Sampling Frequency  : "+sampleRate);
        System.out.println("Frequency of Signal : "+frequencyOfSignal);
        System.out.println("Sampling Interval   : "+samplingInterval);
        for (int i = 0; i < sin.length; i++) {
            final double angle = (2.0 * Math.PI * i) / samplingInterval;
            sin[i] = (byte) (Math.sin(angle) * 127);
            //System.out.println("" + sin[i]);
        }
        return sin;
    }

    private static void play(final SourceDataLine line, final byte[] array) {
        final int length = (sampleRate * array.length) / 1000;
        line.write(array, 0, array.length);
    }

}
