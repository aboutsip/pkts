package io.pkts.examples.siplib;

import io.pkts.packet.sip.SipMessage;
import io.pkts.packet.sip.SipRequest;
import io.pkts.packet.sip.header.ContactHeader;
import io.pkts.packet.sip.header.RecordRouteHeader;
import io.pkts.packet.sip.header.SipHeader;
import io.pkts.packet.sip.header.ViaHeader;

/**
 * @author jonas@jonasborjesson.com
 */
public class SipLibIntroExample {

    public static void introExample001() throws Exception {

        // (1)
        final String rawMessage = new StringBuilder("BYE sip:bob@siplib.io:5060 SIP/2.0\r\n")
                .append("Via: SIP/2.0/UDP 192.168.0.100:5060;rport;branch=z9hG4bK-28976-1-7\r\n")
                .append("From: alice <sip:alice@aboutsip.com>;tag=28976SIPpTag001\r\n")
                .append("To: bob <sip:bob@siplib.io>;tag=28972SIPpTag011\r\n")
                .append("Call-ID: 1-28976@127.0.1.1\r\n")
                .append("CSeq: 2 BYE\r\n")
                .append("Contact: sip:alice@192.168.0.100\r\n")
                .append("Max-Forwards: 70\r\n")
                .append("Drop-Me: Drop this header on proxy\r\n")
                .append("Subject: Example BYE Message\r\n")
                .append("Content-Length: 0\r\n")
                .append("\r\n").toString();

        // (2)
        final SipMessage bye = SipMessage.frame(rawMessage).toRequest();

        // (3)
        final SipMessage proxyBye = bye.copy()
                .onRequestURI(uri -> uri.copy().withHost("sipstack.io").build())
                .withHeader(SipHeader.create("X-SIP-Lib-Version", "2.x"))
                .onHeader(header -> {
                    if (header.isSubjectHeader()) {
                        // change
                        return header.copy().withValue("Example of Proxied BYE Message").build();
                    } else if (header.is("Drop-Me")) {
                        // drop
                        return null;
                    }
                    // keep
                    return header;
                })
                .withTopMostRecordRouteHeader(RecordRouteHeader.withHost("62.63.64.65").withTransportTCP().build())
                .withTopMostViaHeader(ViaHeader.withHost("62.63.64.65").withBranch().withTransportTCP().build())
                .onViaHeader((index, via) -> via.withReceived("54.55.56.57"))
                .build();

        System.out.println(bye);
        System.out.println(proxyBye);
    }

    public static void main(final String ... args) throws Exception {
        SipLibIntroExample.introExample001();
    }
}
