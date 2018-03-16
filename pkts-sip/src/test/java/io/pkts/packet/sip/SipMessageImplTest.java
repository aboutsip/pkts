/**
 * 
 */
package io.pkts.packet.sip;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import io.pkts.PktsTestBase;
import io.pkts.RawData;
import io.pkts.buffer.Buffer;
import io.pkts.packet.sip.header.CSeqHeader;
import io.pkts.packet.sip.header.ContentTypeHeader;
import io.pkts.packet.sip.header.SipHeader;
import io.pkts.packet.sip.header.ViaHeader;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

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
     * <p>
     * Reported as: Allow: ACK, CANCEL, BYE ... parsed as VIA header
     * </p>
     *
     * <p>
     * However, the example used to by filer to reprdouce the bug isn't correct.
     * Here are the errors in that example:
     * </p>
     *
     * <ol>
     * <li>In RFC 3251 (unlike 2543), lines MUST terminate with CRLF. The example only had LF.
     * This is a change from 2543. See the BNF in 3261 and they also have a statement about
     * this difference between the two versions on Section 28.1 page 256, which says:
     *     "In RFC 2543, lines in a message could be terminated with CR, LF,
     *      or CRLF.  This specification only allows CRLF."</li>
     * <li><Missed CRLF on the Content-Length line</li>
     * <li>Must have double CRLF between headers and body of message. In the example
     *   provided, the last header in the message was Content-Length and there was
     *   no extra CRLF between it and the body.</li>
     * </ol>
     *
     * <p>
     * One the above mis-formatting was corrected, the example works just fine.
     * See unit test below.
     * </p>
     *
     * @throws Exception
     */
    @Test
    public void testIssue84() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("INVITE sip:bob@192.168.1.100 SIP/2.0\r\n");
        sb.append("Via: SIP/2.0/UDP 192.168.1.201:2048;branch=z9hG4bK-16gcnwrd28r3;rport\r\n");
        sb.append("From: \"Alice\" <sip:alice@192.168.1.100>;tag=te94a023hw\r\n");
        sb.append("To: <sip:bob@192.168.1.100>\r\n");
        sb.append("Call-ID: 313438313130343734353532333433-xehwxtcark7e\r\n");
        sb.append("CSeq: 1 INVITE\r\n");
        sb.append("Max-Forwards: 70\r\n");
        sb.append("User-Agent: snom300/8.7.5.17\r\n");
        sb.append("Contact: <sip:alice@192.168.1.201:2048;line=8lif2g5m>;reg-id=1\r\n");
        sb.append("X-Serialnumber: 00041325476C\r\n");
        sb.append("P-Key-Flags: keys=\"3\"\r\n");
        sb.append("Accept: application/sdp\r\n");
        sb.append("Allow: INVITE, ACK, CANCEL, BYE, REFER, OPTIONS, NOTIFY, SUBSCRIBE, PRACK, MESSAGE, INFO, UPDATE\r\n");
        sb.append("Allow-Events: talk, hold, refer, call-info\r\n");
        sb.append("Supported: timer, 100rel, replaces, from-change\r\n");
        sb.append("Session-Expires: 3600\r\n");
        sb.append("Min-SE: 90\r\n");
        sb.append("Content-Type: application/sdp\r\n");
        sb.append("Content-Length: 403\r\n\r\n"); // missed here. Also need to be double line
        sb.append("v=0\r\n");
        sb.append("o=root 722847273 722847273 IN IP4 192.168.1.201\r\n");
        sb.append("s=call\r\n");
        sb.append("c=IN IP4 192.168.1.201\r\n");
        sb.append("t=0 0\r\n");
        sb.append("m=audio 61856 RTP/AVP 9 0 8 3 99 112 18 101\r\n");
        sb.append("a=rtpmap:9 G722/8000\r\n");
        sb.append("a=rtpmap:0 PCMU/8000\r\n");
        sb.append("a=rtpmap:8 PCMA/8000\r\n");
        sb.append("a=rtpmap:3 GSM/8000\r\n");
        sb.append("a=rtpmap:99 G726-32/8000\r\n");
        sb.append("a=rtpmap:112 AAL2-G726-32/8000\r\n");
        sb.append("a=rtpmap:18 G729/8000\r\n");
        sb.append("a=fmtp:18 annexb=no\r\n");
        sb.append("a=rtpmap:101 telephone-event/8000\r\n");
        sb.append("a=fmtp:101 0-15\r\n");
        sb.append("a=ptime:20\r\n");
        sb.append("a=sendrecv\r\n");

        final SipMessage msg = SipMessage.frame(sb.toString());
        final List<ViaHeader> vias = msg.getViaHeaders();
        assertThat(vias.size(), is(1));
        assertThat(vias.get(0).getValue().toString(), is("SIP/2.0/UDP 192.168.1.201:2048;branch=z9hG4bK-16gcnwrd28r3;rport"));

        // Note that the Allow header does not allow multiple values
        // on a single line and as such, there is only one Allow header
        // with one long value.
        final List<SipHeader> allow = msg.getHeaders("Allow");
        assertThat(allow.size(), is(1));
        assertThat(allow.get(0).getValue().toString(), is("INVITE, ACK, CANCEL, BYE, REFER, OPTIONS, NOTIFY, SUBSCRIBE, PRACK, MESSAGE, INFO, UPDATE"));

        // Allow-Events (RFC3265) doesn't actually allow multiple values on a single
        // line either but currently there is a bug that doesn't take this into account.
        // Filing a new bug on this.
        // final List<SipHeader> allowEvents = msg.getHeaders("Allow-Events");
        // assertThat(allowEvents.size(), is(1));
        // assertThat(allowEvents.get(0).getValue().toString(), is("talk, hold, refer, call-info"));
    }

    /**
     * Issue 65 is about accessing the ContentType header and after doing so, it will not be included
     * in the "toString" of the message again. Since in pkts.io 3.x, everything is immutable this *should*
     * be impossible so the bug is most likely for the older versions of pkts.io.
     *
     * Conclusion: at least 3.x doesn't seem to have this bug. Looking at the suggested fix
     * from the reporter it seems that that fix went into the 1.0.6 version.
     *
     * @throws Exception
     */
    @Test
    public void testIssueNo65() throws Exception {
        assertContentTypeHeader(parseMessage(RawData.sipInvite), "application", "sdp");
        assertContentTypeHeader(parseMessage(RawData.twoHundredOkFourViaOnOneLine), "application", "sdp");
    }

    private void assertContentTypeHeader(final SipMessage msg, final String type, String subType) {
        final ContentTypeHeader hdr = msg.getContentTypeHeader();
        assertThat(hdr.getContentType().toString(), is(type));
        assertThat(hdr.getContentSubType().toString(), is(subType));
        assertThat(msg.toBuffer().toString().contains("Content-Type: " + type + "/" + subType), is(true));

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
