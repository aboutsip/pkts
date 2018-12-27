package io.pkts.diameter.impl;

import io.pkts.diameter.DiameterTestBase;
import io.pkts.diameter.avp.Avp;
import io.pkts.diameter.avp.AvpHeader;
import io.pkts.diameter.avp.FramedAvp;
import io.pkts.diameter.avp.OriginHost;
import io.pkts.diameter.avp.OriginRealm;
import io.pkts.diameter.avp.ResultCode.ResultCodeEnum;
import io.pkts.diameter.avp.VendorSpecificApplicationId2;
import io.pkts.diameter.avp.type.DiameterIdentity;
import io.pkts.diameter.avp.type.Enumerated;
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
            final FramedAvp avp = raw.getAvp();
            raw.assertHeader(avp.getHeader());
        }
    }

    @Test
    public void testOriginHost() throws Exception {
        final FramedAvp raw = FramedAvp.frame(loadBuffer("AVP_Origin_Host.raw"));
        final Avp avp = raw.parse();
        assertThat(avp instanceof OriginHost, is(true));
        assertThat(avp.getCode(), is(264L));
        assertThat((OriginHost) avp instanceof OriginHost, is(true));

        final DiameterIdentity identity = (DiameterIdentity) avp.getValue();
        assertThat(identity.asString(), is("mme.epc.mnc001.mcc001.3gppnetwork.org"));
    }

    @Test
    public void testOriginRealm() throws Exception {
        final FramedAvp raw = FramedAvp.frame(loadBuffer("AVP_Origin_Realm.raw"));
        final Avp avp = raw.parse();
        assertThat(avp instanceof OriginRealm, is(true));
        assertThat((OriginRealm) avp instanceof OriginRealm, is(true));

        final OriginRealm originRealm = (OriginRealm) avp;
        final DiameterIdentity identity = originRealm.getValue();
        assertThat(identity.asString(), is("epc.mnc001.mcc001.3gppnetwork.org"));
    }

    @Test
    public void testResultCode() throws Exception {
        final FramedAvp raw = FramedAvp.frame(loadBuffer("AVP_Result_Code.raw"));
        // final Avp<Enumerated<ResultCodeEnum>> avp = raw.parse();
        final Avp avp = raw.parse();
        assertThat(avp.isEnumerated(), is(true));
        assertThat(avp.getCode(), is(268L));
        // assertThat(avp.getValue().getValue(), is(2001));
        final Avp<Enumerated<ResultCodeEnum>> result = avp.toEnumerated();
        final ResultCodeEnum rse = result.getValue().getAsEnum().get();
        assertThat(rse, is(ResultCodeEnum.DIAMETER_SUCCESS));
    }

    @Test
    public void testGroupedAvp() throws Exception {
        final FramedAvp raw = FramedAvp.frame(loadBuffer("AVP_Vendor_Specific_Application.raw"));
        final Avp<Grouped> avp = raw.parse();
        assertThat(avp.getCode(), is(260L));
        final Grouped grouped = avp.getValue();

        // Test to get the "raw" un-parsed AVPs...
        // It is important that doing it the "raw" way doesn't then
        // affect to parse it later on, which is what would happen if
        // you don't watch out for how the internal buffers are handled.
        // I.e., if you share the same reader-index then reading from the
        // buffer below will then effect subsequent reading from the AVP.
        final FramedAvp vendorId = grouped.getFramedAvp(266).get();
        final FramedAvp authAppId = grouped.getFramedAvp(258).get();
        assertThat(vendorId.getData().readUnsignedInt(), is(10415L));
        assertThat(authAppId.getData().readUnsignedInt(), is(16777251L));

        // now test to use the convenience methods that will parse it all out
        // and also return the correct types
        final VendorSpecificApplicationId2 vsaid = (VendorSpecificApplicationId2) avp;
        assertThat(vsaid.getVendorId().getValue().getValue(), is(10415L));
        assertThat(vsaid.getAuthApplicationId().get().getValue().getValue(), is(16777251L));

        assertThat(grouped, notNullValue());
    }

}
