package io.pkts.diameter.impl;

import io.pkts.diameter.DiameterHeader;
import io.pkts.diameter.DiameterTestBase;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author jonas@jonasborjesson.com
 */
public class DiameterHeaderTest extends DiameterTestBase {

    /**
     * Ensure that we can parse and read the diameter headers. We'll check all the values that are part
     * of header.
     *
     * @throws Exception
     */
    @Test
    public void testParseDiameterHeader() throws Exception {
        for (final RawDiameterMessageHolder raw : RAW_DIAMETER_MESSAGES) {
            final DiameterHeader header = raw.getHeader();
            raw.assertHeader(header);
            assertTrue("Expected the header for resource " + raw.resource + " to be validated to true", header.validate());
        }
    }

}
