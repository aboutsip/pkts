/**
 * 
 */
package io.pkts.packet.sip;

import io.pkts.PktsTestBase;
import io.pkts.RawData;
import io.pkts.buffer.Buffer;
import io.pkts.packet.sip.header.CSeqHeader;
import io.pkts.packet.sip.header.ViaHeader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

/**
 * @author jonas@jonasborjesson.com
 */
public class SipMessageImplTest extends PktsTestBase {

    /**
     * Some default Via values we will be using for testing
     */
    private final String branchBase = "z9hG4bK-aed415f7-";
    private final String via1 = "SIP/2.0/UDP 127.0.0.1:5060;branch=" + this.branchBase + "%s;X-Conn=HPAAAAAB";
    private final String via2 = "SIP/2.0/UDP 127.0.0.1:5080;branch=" + this.branchBase + "%s;received=127.0.0.1";
    private final String via3 = "SIP/2.0/UDP 127.0.0.1:5090;branch=" + this.branchBase + "%s;apa=foo";

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
     * Ensure that we properly compare sip requests and responses.
     *
     * @throws Exception
     */
    @Test
    public void testEquality() throws Exception {
        final SipRequest req1a = (SipRequest) parseMessage(RawData.sipInvite);
        final SipRequest req1b = (SipRequest) parseMessage(RawData.sipInvite);

        assertThat(req1a, is(req1a));
        assertThat(req1a, is(req1b));
        assertThat(req1b, is(req1a));

        final SipResponse res1a = req1a.createResponse(100).build();
        final SipResponse res1b = req1a.createResponse(100).build();

        assertThat(res1a, is(res1a));
        assertThat(res1a, is(res1b));
        assertThat(res1b, is(res1a));

        // a 200 isn't the same as a 100 of course
        final SipResponse res1c = req1a.createResponse(200).build();
        assertThat(res1c, not(res1a));

        // a request and response are never equal
        assertThat(req1a, not(res1a));
        assertThat(req1a, not(res1c));

        // a BYE request isn't the same as a INVITE request (duh)
        final SipRequest req2a = (SipRequest) parseMessage(RawData.sipBye);
        assertThat(req2a, is(req2a));
        assertThat(req2a, not(req1a));
    }


    /**
     * Helper method for building up a bunch of Via headers in a response. If
     * the supplied list of via headers start with the header name (Via:) then
     * they are added as their own headers. If they do not start with "Via" then
     * they are folded into the previous one.
     * 
     * @param vias
     * @return
     * @throws Exception
     */
    protected SipResponse createResponseWithViaHeaders(final String... vias) throws Exception {
        final StringBuilder sb = new StringBuilder();
        sb.append("SIP/2.0 180 Ringing\r\n");
        StringBuilder currentVia = new StringBuilder();
        int count = 0;
        for (String via : vias) {
            via = String.format(via, count++);
            if (via.startsWith("Via:")) {
                if (currentVia.length() != 0) {
                    sb.append(currentVia.toString()).append("\r\n");
                }
                currentVia = new StringBuilder(via);
            } else {
                if (currentVia.length() == 0) {
                    currentVia.append("Via: ");
                } else {
                    currentVia.append(", ");
                }
                currentVia.append(via);
            }
        }

        sb.append("From: sipp <sip:sipp@127.0.0.1:5080>;tag=4948SIPpTag001\r\n");
        sb.append("To: sut <sip:service@127.0.0.1:5070>;tag=4945SIPpTag011\r\n");

        // even though I guess normally Via-headers would always come in a row I don't think
        // it is technically illegal to have one a little "after" the other. So, by adding
        // it here we will split the Via-headers apart and I think we should be able to
        // deal with it no matter what...
        sb.append(currentVia.toString()).append("\r\n");
        sb.append("Call-ID: 1-4948@127.0.0.1\r\n");
        sb.append("CSeq: 1 INVITE\r\n");
        sb.append("Contact: <sip:127.0.0.1:5090;transport=UDP>\r\n");
        sb.append("Content-Length: 0\r\n\r\n");
        return parseMessage(sb.toString()).toResponse();
    }

    /**
     * Make sure that we can parse out headers (in this case Via headers) when
     * there are multiple of them on a single line etc.
     * 
     * @throws Exception
     */
    @Test
    public void testViaHeaders() throws Exception {
        assertViaHeaders(this.via1);
        assertViaHeaders(this.via1, this.via2);
        assertViaHeaders(this.via1, this.via2, this.via2);
        assertViaHeaders(this.via1, "Via: " + this.via2, this.via2);
        assertViaHeaders(this.via1, this.via2, "Via: " + this.via3);
        assertViaHeaders(this.via1, this.via2, this.via3);
        assertViaHeaders("Via: " + this.via1, "Via: " + this.via2, "Via: " + this.via3);
    }

    private void assertViaHeaders(final String... vias) throws Exception {
        final String[] formatedViaValues = new String[vias.length];
        for (int i = 0; i < vias.length; ++i) {
            formatedViaValues[i] = String.format(vias[i], i);
            if (formatedViaValues[i].startsWith("Via: ")) {
                formatedViaValues[i] = formatedViaValues[i].substring("Via: ".length());
            }
        }

        SipResponse response = createResponseWithViaHeaders(vias);

        // this should always return the top-most via header which will
        // always have the branchBase-0 as its branch since that is how we generate
        // the branches...
        final ViaHeader via = response.getViaHeader();
        assertThat(via.getBranch().toString(), is(this.branchBase + 0));
        assertThat(via.getValue().toString(), is(formatedViaValues[0]));

        // make sure that the other Via-header is still in the msg
        assertViasInMessageDump(response, formatedViaValues);

        // do it again but ask for all the Via headers up front since
        // the parsing may be slightly different (which it is)
        response = createResponseWithViaHeaders(vias);
        final List<ViaHeader> viaHeaders = response.getViaHeaders();
        assertThat(viaHeaders.size(), is(vias.length));
        for (int i = 0; i < viaHeaders.size(); ++i) {
            assertThat(viaHeaders.get(i).getBranch().toString().contains(this.branchBase + i), is(true));
        }
    }

    /**
     * Helper method for ensuring that all the via headers exists in the
     * SipMessage.
     * 
     * @param msg
     * @param formatedVias
     *            note that these Via's have been formated already
     */
    private void assertViasInMessageDump(final SipMessage msg, final String[] formatedVias) {
        final String msgDump = msg.toString();
        for (final String formatedVia : formatedVias) {
            assertThat(msgDump.contains(formatedVia), is(true));
        }
    }

    @Test
    public void testCSeqHeader() throws Exception {
        final SipRequest request = (SipRequest) parseMessage(RawData.sipInvite);
        final CSeqHeader cseq = request.getCSeqHeader();
        assertThat(cseq.getSeqNumber(), is(1L));
        assertThat(cseq.getMethod().toString(), is("INVITE"));
    }

    private void assertTopMostVia(final SipMessage msg, final String host, final int port, final String transport)
            throws Exception {

        final ViaHeader via =
                ViaHeader.withHost(host).withPort(port).withBranch(ViaHeader.generateBranch()).withTransport(transport).build();
        final ViaHeader topMostVia = msg.getViaHeader();
        assertThat(topMostVia.getBranch(), not((Buffer) null));
        assertThat(topMostVia.getHost().toString(), is(host));
        assertThat(topMostVia.getPort(), is(port));
        assertThat(topMostVia.getTransport().toString(), is(transport));
    }

}
