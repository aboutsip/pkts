package io.pkts.packet.diameter.impl;

import org.junit.Test;

import io.pkts.packet.diameter.AvpHeader;
import io.pkts.packet.diameter.DiameterTestBase;

/**
 * Tests for verifying the {@link AvpHeader}.
 *
 * @author jonas@jonasborjesson.com
 */
public class AvpHeaderTest extends DiameterTestBase {

    @Test
    public void testAvpHeader() throws Exception {

        ensureAvpHeader("001_diameter_auth_info_request.raw", 344);
    }

    private static void ensureAvpHeader(final String resource, final int code) throws Exception {
        final AvpHeader header = AvpHeader.frame(loadBuffer(resource));
    }
}
