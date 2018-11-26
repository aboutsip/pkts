package io.pkts.diameter.impl;

import io.pkts.diameter.Avp;
import io.pkts.diameter.AvpHeader;
import io.pkts.diameter.DiameterTestBase;
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
            final Avp avp = raw.getAvp();
            raw.assertHeader(avp.getHeader());
        }

    }

}
