package io.pkts.gtp;

import io.pkts.gtp.control.Gtp1Header;
import io.pkts.gtp.control.Gtp2Header;
import io.snice.buffer.Buffer;
import org.junit.Test;

import java.util.function.Supplier;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author jonas@jonasborjesson.com
 */
public class GtpHeaderTest extends GtpTestBase {

    private final Teid emptyTeid = Teid.of(Buffer.of((byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00));

    @Test
    public void testFrameGtpV2Header() throws Exception {
        Gtp2Header header = GtpHeader.frame(GtpRawData.deleteBearerRequestGtpv2).toGtp2Header();
        assertThat(header.getVersion(), is(2));
        assertThat(header.getLength(), is(13));
        assertThat(header.getMessageTypeDecimal(), is(99));

        // Values from wireshark...
        Teid teid = Teid.of(Buffer.of((byte) 0xa5, (byte) 0xd2, (byte) 0x68, (byte) 0xf0));
        assertThat(header.getTeid().get(), is(teid));


        header = GtpHeader.frame(GtpRawData.deleteBearerResponseGtpv2).toGtp2Header();
        assertThat(header.getVersion(), is(2));
        assertThat(header.getLength(), is(55));
        assertThat(header.getMessageTypeDecimal(), is(100));

        teid = Teid.of(Buffer.of((byte) 0x57, (byte) 0xb5, (byte) 0x01, (byte) 0xf8));
        assertThat(header.getTeid().get(), is(teid));
    }

    @Test
    public void testFrameGtpV1Header() throws Exception {
        final GtpHeader header = GtpHeader.frame(GtpRawData.createPdpContextRequest);
        assertThat(header.getVersion(), is(1));
        assertThat(header.getLength(), is(180));
        assertThat(header.getMessageTypeDecimal(), is(16));

        assertThat(header.toGtp1Header().getTeid(), is(emptyTeid));

    }

    /**
     * Normally, one would use the {@link GtpHeader#frame(Buffer)} method that would
     * figure out whether it is version 1 or 2 and it would then call the correct framer.
     * However, nothing prevents a user to just call e.g {@link Gtp1Header#frame(Buffer)}
     * but pass in data for GTPv2 and then we should detect and complain.
     */
    @Test
    public void testWrongVersion() {
        ensureBlowsUp(1, 2, () -> Gtp1Header.frame(GtpRawData.deleteBearerResponseGtpv2));
        ensureBlowsUp(2, 1, () -> Gtp2Header.frame(GtpRawData.createPdpContextRequest));
    }

    private void ensureBlowsUp(final int expected, final int actual, final Supplier<GtpHeader> fn) {
        try {
            fn.get();
            fail("Expected to blow up due to wrong GTP version");
        } catch (final GtpVersionException e) {
            assertThat(e.getExpectedVersion(), is(expected));
            assertThat(e.getActualVersion(), is(actual));
        }
    }
}
