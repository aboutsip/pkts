package io.pkts.packet.diameter.impl;

import org.junit.Test;

import io.pkts.packet.diameter.DiameterHeader;
import io.pkts.packet.diameter.DiameterTestBase;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author jonas@jonasborjesson.com
 */
public class DiameterHeaderTest extends DiameterTestBase {

    @Test
    public void testHeader() throws Exception {

        // all values have been verified with Wireshark
        ensureHeader("001_diameter_auth_info_request.raw", 344);
        ensureHeader("002_diameter_auth_info_answer.raw", 832);
        ensureHeader("003_diameter_update_location_request.raw", 456);
        ensureHeader("004_diameter_update_location_answer.raw", 1024);
        ensureHeader("005_diameter_credit_control_request.raw", 664);
        ensureHeader("006_diameter_credit_control_answer.raw", 252);
        ensureHeader("007_diameter_notify_request.raw", 316);
        ensureHeader("008_diameter_notify_answer.raw", 228);
        ensureHeader("009_diameter_purge_ue_request.raw", 284);
        ensureHeader("010_diameter_credit_control_request.raw", 420);
        ensureHeader("011_diameter_purge_ue_answer.raw", 244);
        ensureHeader("012_diameter_credit_control_answer.raw", 220);
        ensureHeader("013_diameter_device_watchdog_request.raw", 112);
        ensureHeader("014_diameter_device_watchdog_answer.raw", 124);
    }

    private static void ensureHeader(final String resource, int expectedLength) throws Exception {
        final DiameterHeader header = DiameterHeader.frame(loadBuffer(resource));
        assertThat(header.getVersion(), is(1));
        assertThat(header.getLength(), is(expectedLength));
    }

}
