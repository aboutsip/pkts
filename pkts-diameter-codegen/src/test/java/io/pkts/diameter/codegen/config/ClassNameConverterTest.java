package io.pkts.diameter.codegen.config;

import io.pkts.diameter.codegen.CodeGenTestBase;
import io.pkts.diameter.codegen.primitives.AvpPrimitive;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class ClassNameConverterTest extends CodeGenTestBase {

    private ClassNameConverter converter;

    @Override
    @Before
    public void setUp() {
        super.setUp();
        converter = ClassNameConverter.defaultConverter();
    }


    @Test
    public void testConvertElement() throws Exception {
        final AvpPrimitive avp = load("avp001.xml").getAvp("User-Name").get();
        final String className = converter.convert(avp);
        assertThat(className, is("UserName"));
    }

    /**
     * Java class names cannot start with a digit and there are a lot of 3GPP names so we'll
     * change that to TGPP.
     *
     * @throws Exception
     */
    @Test
    public void test3GPPConversion() throws Exception {
        assertThat(converter.convert("3GPP-CG-IPv6-Address"), is("TgppCgIpv6Address"));
        assertThat(converter.convert("3GPP-SGSN-IPv6-Address"), is("TgppSgsnIpv6Address"));
        assertThat(converter.convert("3GPP-IMEISV"), is("TgppImeisv"));
        assertThat(converter.convert("3GPP-CAMEL-Charging-Info"), is("TgppCamelChargingInfo"));
        assertThat(converter.convert("3GPP2-QoS-Information"), is("Tgpp2QosInformation"));
        assertThat(converter.convert("3GPP2-BSID"), is("Tgpp2Bsid"));
    }

    /**
     * Test illegal start characters for which we currently do not have a translation for.
     *
     * @throws Exception
     */
    @Test
    public void testIllegalStartChars() throws Exception {
        for (int i = 0; i < 10; ++i) {
            if (i == 3) {
                // there is a known translation for 3
                continue;
            }
            ensureIllegal(i + "Will-Not-Work");
        }
    }

    private void ensureIllegal(final String name) {
        try {
            converter.convert(name);
            fail("Expected an IllegalArgumentException because there is no known translation for the name " + name);
        } catch (final IllegalArgumentException e) {
            // expected.
            assertThat(e.getMessage().startsWith("A Java class name cannot start with"), is(true));
        }
    }

    @Test
    public void testConvertClassName() throws Exception {
        assertThat(converter.convert("User_Name"), is("UserName"));
        assertThat(converter.convert("Inovar-SIP-Response-Code"), is("InovarSipResponseCode"));
        assertThat(converter.convert("Inovar-ISUP-Cause"), is("InovarIsupCause"));
        assertThat(converter.convert("Inovar-PS-Registration-Status"), is("InovarPsRegistrationStatus"));
        assertThat(converter.convert("P2PSMS-Information"), is("P2psmsInformation"));
        assertThat(converter.convert("SMSC-Address-Huawei"), is("SmscAddressHuawei"));
        assertThat(converter.convert("SM-Id"), is("SmId"));
        assertThat(converter.convert("SM-Length"), is("SmLength"));
        assertThat(converter.convert("MO-MSC-Address"), is("MoMscAddress"));
        assertThat(converter.convert("MT-MSC-Address"), is("MtMscAddress"));
        assertThat(converter.convert("Fee-Type"), is("FeeType"));
        assertThat(converter.convert("Status-Report-Requested"), is("StatusReportRequested"));
        assertThat(converter.convert("Send-Result"), is("SendResult"));
        assertThat(converter.convert("Idle-To-Connected-Transition-Count"), is("IdleToConnectedTransitionCount"));
        assertThat(converter.convert("Connected-Duration"), is("ConnectedDuration"));
        assertThat(converter.convert("Charging-Gateway-Function-Host"), is("ChargingGatewayFunctionHost"));
        assertThat(converter.convert("Charging-Group-ID"), is("ChargingGroupId"));
        assertThat(converter.convert("Self-Activation-Status"), is("SelfActivationStatus"));
        assertThat(converter.convert("Origination-Timestamp"), is("OriginationTimestamp"));
        assertThat(converter.convert("Max-Wait-Time"), is("MaxWaitTime"));
    }
}
