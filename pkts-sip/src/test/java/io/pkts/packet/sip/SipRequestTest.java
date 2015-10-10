/**
 * 
 */
package io.pkts.packet.sip;

import io.pkts.PktsTestBase;
import io.pkts.RawData;
import io.pkts.packet.sip.address.SipURI;
import io.pkts.packet.sip.header.CSeqHeader;
import io.pkts.packet.sip.header.CallIdHeader;
import io.pkts.packet.sip.header.ContactHeader;
import io.pkts.packet.sip.header.FromHeader;
import io.pkts.packet.sip.header.MaxForwardsHeader;
import io.pkts.packet.sip.header.ViaHeader;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

/**
 * @author jonas@jonasborjesson.com
 */
public class SipRequestTest extends PktsTestBase {

    private FromHeader from;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.from = FromHeader.builder().withUser("bob").withHost("somewhere.com").build();
    }

    /**
     * Simple test for making sure that the payload makes it into the toString
     * stuff.
     *
     * @throws Exception
     */
    @Test
    public void testToString() throws Exception {
        final SipRequest req = (SipRequest) parseMessage(RawData.sipInvite);
        assertThat(req.toString().contains("o=user1 53655765 2353687637 IN IP4 127.0.1.1"), is(true));
    }

    @Test
    public void testCreateResponse() throws Exception {
        assertReasonPhrase(100, "Trying");
        assertReasonPhrase(180, "Ringing");
        assertReasonPhrase(200, "OK");
        assertReasonPhrase(202, "Accepted");
        assertReasonPhrase(302, "Moved Temporarily");
        assertReasonPhrase(400, "Bad Request");
        assertReasonPhrase(500, "Server Internal Error");
        assertReasonPhrase(600, "Busy Everywhere");
        assertReasonPhrase(603, "Decline");
    }

    /**
     * Test to create a new INVITE request and check all the headers that are supposed to be created
     * by default when not specified indeed are created with the correct values.
     * 
     * @throws Exception
     */
    @Test
    public void testCreateInvite() throws Exception {
        final SipRequest invite = SipRequest.invite("sip:alice@example.com").withFromHeader(this.from).build();
        assertThat(invite.getToHeader().toString(), is("To: sip:alice@example.com"));

        final CSeqHeader cseq = invite.getCSeqHeader();
        assertThat(cseq.getSeqNumber(), is(0L));
        assertThat(cseq.getMethod().toString(), is("INVITE"));

        final CallIdHeader callId = invite.getCallIDHeader();
        assertThat(callId, not((CallIdHeader) null));

        final MaxForwardsHeader max = invite.getMaxForwards();
        assertThat(max.getMaxForwards(), is(70));

        assertThat(invite.getFromHeader().toString(), is("From: sip:bob@somewhere.com"));
    }

    /**
     * Although not mandatory from the builder's perspective, having a request without a
     * {@link ContactHeader} is pretty much useless so make sure that we can add that as well.
     * 
     * @throws Exception
     */
    @Test
    public void testCreateInviteWithContactHeader() throws Exception {
        final ContactHeader contact = ContactHeader.with().withHost("12.13.14.15").withPort(1234).transportTCP().build();
        final SipRequest invite = SipRequest.invite("sip:alice@example.com").withFromHeader(this.from).withContactHeader(contact).build();
        final SipURI contactURI = (SipURI) invite.getContactHeader().getAddress().getURI();
        assertThat(contactURI.getPort(), is(1234));
        assertThat(contactURI.getHost().toString(), is("12.13.14.15"));
        assertThat(contact.getValue().toString(), is("<sip:12.13.14.15:1234;transport=tcp>"));
    }

    @Test
    public void testCreateInviteWithViaHeaders() throws Exception {
        final ViaHeader via =
                ViaHeader.withHost("127.0.0.1").withPort(9898).withTransportUdp().withBranch(ViaHeader.generateBranch()).build();
        SipRequest invite = SipRequest.invite("sip:alice@example.com").withFromHeader(this.from).withViaHeader(via).build();

        // since there is only one via header, getting the "top-most" via header should
        // be the same as getting the first via off of the list.
        assertThat(invite.getViaHeaders().size(), is(1));
        assertThat(
                invite.getViaHeaders().get(0).toString()
                .startsWith("Via: SIP/2.0/UDP 127.0.0.1:9898;branch=z9hG4bK"), is(true));
        assertThat(invite.getViaHeader().toString().startsWith("Via: SIP/2.0/UDP 127.0.0.1:9898;branch=z9hG4bK"),
                is(true));

        // two via headers
        final ViaHeader via2 =
                ViaHeader.withHost("192.168.0.100").withTransportTCP().withBranch(ViaHeader.generateBranch()).build();
        invite = SipRequest.invite("sip:alice@example.com").withFromHeader(this.from).withViaHeaders(via, via2).build();
        assertThat(invite.getViaHeaders().size(), is(2));

        // the top-most via should be the one we added first.
        assertThat(invite.getViaHeader().toString().startsWith("Via: SIP/2.0/UDP 127.0.0.1:9898;branch=z9hG4bK"),
                is(true));

        assertThat(
                invite.getViaHeaders().get(1).toString().startsWith("Via: SIP/2.0/TCP 192.168.0.100;branch=z9hG4bK"),
                is(true));
    }

}
