/**
 * 
 */
package io.pkts.packet.sip;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import io.pkts.PktsTestBase;
import io.pkts.RawData;
import io.pkts.buffer.Buffer;
import io.pkts.packet.sip.header.HeaderFactory;
import io.pkts.packet.sip.header.MaxForwardsHeader;
import io.pkts.packet.sip.header.ViaHeader;
import io.pkts.packet.sip.header.impl.HeaderFactoryImpl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public class SipMessageFactoryImplTest extends PktsTestBase {

    private final HeaderFactory headerFactory = new HeaderFactoryImpl();

    /**
     * @throws java.lang.Exception
     */
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    /**
     * @throws java.lang.Exception
     */
    @Override
    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testCreateResponseBasedOnRequest() throws Exception {
        final SipRequest req = (SipRequest) parseMessage(RawData.sipInvite);
        final SipResponse resp = req.createResponse(200);
        assertThat(resp.getStatus(), is(200));
        assertThat(resp.getFromHeader().getValue().toString(), is("sipp <sip:sipp@127.0.1.1:5060>;tag=16732SIPpTag001"));
        assertThat(resp.getToHeader().getValue().toString(), is("sut <sip:service@127.0.0.1:5090>"));
        assertThat(resp.getCallIDHeader().getValue().toString(), is("1-16732@127.0.1.1"));
        assertThat(resp.getViaHeader().getValue().toString(), is("SIP/2.0/UDP 127.0.1.1:5060;branch=z9hG4bK-16732-1-0"));
        assertThat(resp.getHeader(MaxForwardsHeader.NAME).getValue().toString(), is("70"));

        final Buffer buffer = resp.toBuffer();
        System.out.println(buffer);
    }

    @Test
    public void testCreateRequestBasedOnOtherRequest() throws Exception {
        final SipRequest original = (SipRequest) parseMessage(RawData.sipInvite);
        final SipRequest request = original.clone();
        final ViaHeader topMostVia = request.getViaHeader();
        assertThat(topMostVia.getBranch().toString(), is("z9hG4bK-16732-1-0"));

        final ViaHeader via = ViaHeader.with().host("127.0.0.1").port(9898).transportUDP().build();
        request.addHeaderFirst(via);
        System.err.println(request.toString());
    }

}
