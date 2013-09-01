/**
 * 
 */
package io.pkts.framer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import io.pkts.PktsTestBase;
import io.pkts.buffer.Buffer;
import io.pkts.packet.PCapPacket;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author jonas@jonasborjesson.com
 */
public class PcapFramerTest extends PktsTestBase {

    private PcapFramer framer;

    /**
     * @throws java.lang.Exception
     */
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.framer = new PcapFramer(this.defaultPcapHeader, this.framerManager);
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
        PCapPacket frame = null;
        for (int i = 6; i < 30; ++i) {
            frame = this.framer.frame(null, this.pcapStream);
            assertNotNull(frame);
        }

        // the last frame is supposed to 340 according to wireshark
        assertThat(340, is(frame.getPayload().capacity()));

        // we have read all the 30 frames so trying to frame
        // another one shouldn't work. Hence, we should be getting
        // back a null frame, indicating that the fun is over
        frame = this.framer.frame(null, this.pcapStream);
        assertThat(frame, is((PCapPacket) null));

    }

    private void verifyNextFrame(final Buffer in, final int expectedLength)
            throws IOException {
        final PCapPacket frame = this.framer.frame(null, in);
        final Buffer payload = frame.getPayload();
        assertThat(expectedLength, is(payload.capacity()));
    }

}
