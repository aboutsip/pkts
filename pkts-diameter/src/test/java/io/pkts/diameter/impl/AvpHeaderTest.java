package io.pkts.diameter.impl;

import io.pkts.diameter.DiameterTestBase;
import io.pkts.diameter.avp.Avp;
import io.pkts.diameter.avp.AvpHeader;
import io.pkts.diameter.avp.OriginHost;
import io.pkts.diameter.avp.OriginRealm;
import io.pkts.diameter.avp.RawAvp;
import io.pkts.diameter.avp.type.DiameterIdentity;
import io.pkts.diameter.avp.type.Grouped;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

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
            final RawAvp avp = raw.getAvp();
            raw.assertHeader(avp.getHeader());
        }
    }

    @Test
    public void testOriginHost() throws Exception {
        final RawAvp raw = RawAvp.frame(loadBuffer("AVP_Origin_Host.raw"));
        final Avp avp = raw.parse();
        assertThat(avp instanceof OriginHost, is(true));
        assertThat(avp.getCode(), is(264L));
        assertThat(raw.toOriginHost() instanceof OriginHost, is(true));
        assertThat(raw.isOriginHost(), is(true));

        final DiameterIdentity identity = (DiameterIdentity) avp.getValue();
        assertThat(identity.asString(), is("mme.epc.mnc001.mcc001.3gppnetwork.org"));
    }

    @Test
    public void testOriginRealm() throws Exception {
        final RawAvp raw = RawAvp.frame(loadBuffer("AVP_Origin_Realm.raw"));
        final Avp avp = raw.parse();
        assertThat(avp instanceof OriginRealm, is(true));
        assertThat(raw.toOriginRealm() instanceof OriginRealm, is(true));
        assertThat(raw.isOriginRealm(), is(true));

        final OriginRealm originRealm = raw.toOriginRealm();
        final DiameterIdentity identity = originRealm.getValue();
        assertThat(identity.asString(), is("epc.mnc001.mcc001.3gppnetwork.org"));
    }

    @Test
    public void testGroupedAvp() throws Exception {
        final RawAvp raw = RawAvp.frame(loadBuffer("AVP_Vendor_Specific_Application.raw"));
        final Avp<Grouped> avp = raw.parse();
        assertThat(avp.getCode(), is(260L));
        final Grouped grouped = avp.getValue();
        assertThat(grouped, notNullValue());
    }

}
