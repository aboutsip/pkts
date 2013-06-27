/**
 * 
 */
package io.pkts.packet.sip;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import io.pkts.YajTestBase;
import io.pkts.buffer.Buffer;
import io.pkts.buffer.Buffers;
import io.pkts.packet.sip.SipMessage;
import io.pkts.packet.sip.SipRequest;
import io.pkts.packet.sip.header.HeaderFactory;
import io.pkts.packet.sip.header.ViaHeader;
import io.pkts.packet.sip.header.impl.HeaderFactoryImpl;
import io.pkts.protocol.Protocol;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * @author jonas@jonasborjesson.com
 */
public class SipMessageImplTest extends YajTestBase {

    private final HeaderFactory headerFactory = new HeaderFactoryImpl();

    /*
     * (non-Javadoc)
     * 
     * @see com.aboutsip.yajpcap.YajTestBase#setUp()
     */
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.aboutsip.yajpcap.YajTestBase#tearDown()
     */
    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Make sure that all our Via-headers can be modified the way we need it.
     */
    @Test
    public void testViaHeaderManipulation() throws Exception {
        final SipRequest request = (SipRequest) loadStream("sipp.pcap").get(0).getFrame(Protocol.SIP).parse();
        final ViaHeader topMostVia = request.getViaHeader();
        assertThat(topMostVia.getBranch().toString(), is("z9hG4bK-16732-1-0"));
        assertThat(topMostVia.getHost().toString(), is("127.0.1.1"));
        assertThat(topMostVia.getPort(), is(5060));

        assertTopMostVia(request, "192.168.0.100", 6789, "TCP");
        assertTopMostVia(request, "192.168.0.101", 1111, "UDP");
        final String str = request.toString();
        assertThat(str.contains("Via: SIP/2.0/TCP 192.168.0.100:6789"), is(true));
        assertThat(str.contains("Via: SIP/2.0/UDP 192.168.0.101:1111"), is(true));
    }

    private void assertTopMostVia(final SipMessage msg, final String host, final int port, final String transport)
            throws Exception {
        final ViaHeader via = this.headerFactory.createViaHeader(Buffers.wrap(host), port, Buffers.wrap(transport),
                null);
        msg.addHeaderFirst(via);
        final ViaHeader topMostVia = msg.getViaHeader();
        assertThat(topMostVia.getBranch(), is((Buffer) null));
        assertThat(topMostVia.getHost().toString(), is(host));
        assertThat(topMostVia.getPort(), is(port));
        assertThat(topMostVia.getTransport().toString(), is(transport));

    }

}
