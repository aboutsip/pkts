package io.pkts.packet.diameter;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import org.junit.Test;

import static io.pkts.packet.diameter.impl.DiameterParser.couldBeDiameterMessage;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 *
 * @author jonas@jonasborjesson.com
 */
public class DiameterTest extends DiameterTestBase {

    /**
     * Test some basic parsing of diameter messages. We'll just check that we have the right amount of AVPs
     * etc.
     *
     * @throws Exception
     */
    @Test
    public void testParseDiameterMessage() throws Exception {
        for (final RawDiameterMessageHolder raw : RAW_DIAMETER_MESSAGES) {
            final DiameterMessage msg = DiameterMessage.frame(raw.load());
            raw.assertHeader(msg.getHeader());
            assertThat(msg.getAllAvps().size(), is(raw.avpCount));
        }
    }

    @Test
    public void testNoDiameterMessage() throws Exception {
        assertThat(couldBeDiameterMessage(Buffers.EMPTY_BUFFER), is(false));
        for (int i = 1; i < 20; ++i) {
            final Buffer buffer = Buffers.wrap(new byte[i]);
            assertThat(couldBeDiameterMessage(buffer), is(false));
        }
    }
}
