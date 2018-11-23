package io.pkts.packet.diameter.impl;

import io.pkts.packet.diameter.Avp;
import io.pkts.packet.diameter.AvpHeader;
import io.pkts.packet.diameter.DiameterTestBase;
import org.junit.Test;

/**
 * Tests for verifying the {@link AvpHeader}.
 *
 * @author jonas@jonasborjesson.com
 */
public class AvpHeaderTest extends DiameterTestBase {

    @Test
    public void testAvpHeader() throws Exception {
        for (final RawAvpHolder raw : RAW_AVPS) {
            final AvpHeader header = raw.getHeader();
            raw.assertHeader(header);
        }
    }

    @Test
    public void testBasicAvp() throws Exception {
        for (final RawAvpHolder raw : RAW_AVPS) {
            Avp avp = raw.getAvp();
            raw.assertHeader(avp.getHeader());
        }

    }

}
