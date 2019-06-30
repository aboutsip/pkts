package io.pkts.gtp.control;

import io.pkts.gtp.GtpRawData;
import io.pkts.gtp.GtpTestBase;
import io.snice.buffer.Buffer;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TypeLengthInstanceValueTest extends GtpTestBase {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testParseImsiTLIV() {
        ensureBasicTlivProperties(GtpRawData.imsiTLIV, 1, 8);
        ensureBasicTlivProperties(GtpRawData.userLocationInfo, 86, 13);
        ensureBasicTlivProperties(GtpRawData.servingNetwork, 83, 3);
        ensureBasicTlivProperties(GtpRawData.fteid, 87, 9);
    }

    private void ensureBasicTlivProperties(final Buffer buffer, final int expectedType, final int expectedLength) {
        final TypeLengthInstanceValue tliv = TypeLengthInstanceValue.frame(buffer);
        assertThat(tliv.getTypeAsDecimal(), is(expectedType));
        assertThat(tliv.getLength(), is(expectedLength));
    }
}