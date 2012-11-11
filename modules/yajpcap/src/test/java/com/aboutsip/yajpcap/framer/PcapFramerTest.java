/**
 * 
 */
package com.aboutsip.yajpcap.framer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.aboutsip.buffer.Buffer;
import com.aboutsip.yajpcap.YajTestBase;
import com.aboutsip.yajpcap.frame.PcapFrame;

/**
 * @author jonas@jonasborjesson.com
 */
public class PcapFramerTest extends YajTestBase {

    private PcapFramer framer;

    /**
     * @throws java.lang.Exception
     */
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.framer = new PcapFramer(this.defaultByteOrder, this.framerManager);
    }

    /**
     * @throws java.lang.Exception
     */
    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testPcapFramer() throws Exception {
        // length verified using wireshark. Remember, the first frame
        // has been framed and verified by the test base
        verifyNextFrame(this.pcapStream, 348);
        verifyNextFrame(this.pcapStream, 507);
        verifyNextFrame(this.pcapStream, 398);
        verifyNextFrame(this.pcapStream, 547);
        verifyNextFrame(this.pcapStream, 348);

        // there are a total of 30 frames in this pcap.
        PcapFrame frame = null;
        for (int i = 6; i < 30; ++i) {
            frame = this.framer.frame(null, this.pcapStream);
            assertNotNull(frame);
        }

        // the last frame is supposed to 340 according to wireshark
        assertThat(340, is((frame.getPayload().capacity())));

        // we have read all the 30 frames so trying to frame
        // another one shouldn't work. Hence, we should be getting
        // back a null frame, indicating that the fun is over
        frame = this.framer.frame(null, this.pcapStream);
        assertThat(frame, is((PcapFrame) null));

    }

    private void verifyNextFrame(final Buffer in, final int expectedLength)
            throws IOException {
        final PcapFrame frame = this.framer.frame(null, in);
        final Buffer payload = frame.getPayload();
        assertThat(expectedLength, is((payload.capacity())));
    }

}
