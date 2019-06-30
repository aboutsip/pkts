package io.pkts.gtp;

import io.pkts.gtp.control.InfoElement;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class GtpMessageTest extends GtpTestBase {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testParseGtpv2Message() {
        final GtpMessage msg = GtpMessage.frame(GtpRawData.createSessionRequest);
        assertThat(msg.getHeader().getLength(), is(251));

        final List<? extends InfoElement> ie = msg.getInfoElements();
        assertThat(ie.size(), is(16));
        assertInfoElement(ie.get(0), 1, 8);
        assertInfoElement(ie.get(1), 75, 8);
        assertInfoElement(ie.get(2), 86, 13);
        assertInfoElement(ie.get(3), 83, 3);
        assertInfoElement(ie.get(4), 82, 1);
        assertInfoElement(ie.get(5), 87, 9);
        assertInfoElement(ie.get(6), 71, 39);
        assertInfoElement(ie.get(7), 128, 1);
        assertInfoElement(ie.get(8), 99, 1);
        assertInfoElement(ie.get(9), 79, 5);
        assertInfoElement(ie.get(10), 127, 1);
        assertInfoElement(ie.get(11), 72, 8);
        assertInfoElement(ie.get(12), 78, 35);
        assertInfoElement(ie.get(13), 93, 44);
        assertInfoElement(ie.get(14), 3, 1);
        assertInfoElement(ie.get(15), 114, 2);
    }

    private void assertInfoElement(final InfoElement ie, final int expectedType, final int expectedLength) {
        assertThat(ie.getTypeAsDecimal(), is(expectedType));
        assertThat(ie.getLength(), is(expectedLength));
    }


}