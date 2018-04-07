package io.pkts.packet.diameter;

import org.junit.Test;

import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;

import static io.pkts.packet.diameter.impl.DiameterParser.couldBeDiameterMessage;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 *
 * @author jonas@jonasborjesson.com
 */
public class DiameterTest {

    @Test
    public void testReadDiameterPcap() throws Exception {

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
