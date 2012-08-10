package com.aboutsip.yajpcap.protocol;


/**
 * @author jonas@jonasborjesson.com
 */
public enum Protocol {
    ICMP("icmp"), IGMP("igmp"), TCP("tcp"), UDP("udp"), SCTP("sctp"), SIP("sip"), SDP("sdp"), ETHERNET_II("eth"), SLL("sll"), IPv4(
            "ip"), PCAP("pcap"), UNKNOWN("unknown");

    private final String name;

    private Protocol(final String name) {
        this.name = name;
    }

    /**
     * The short name of this protocol. Similar to what Wireshark shows in its
     * short description of all the known protocols within its "super" frame.
     * E.g., if you "click" on the Pcap Frame it will have a field called
     * "protocols in frame" and will display something like
     * "eth:ip:udp:sip:sdp", this function will return a short name like that.
     * 
     * @return
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get a protocol based on it's defined byte code. This is only true for
     * some protocols
     * 
     * For a full list: http://en.wikipedia.org/wiki/List_of_IP_protocol_numbers
     * 
     * @param code
     * @return
     */
    public static Protocol valueOf(final byte code) {
        switch (code) {
        case (byte) 0x01:
            return ICMP;
        case (byte) 0x02:
            return IGMP;
        case (byte) 0x06:
            return TCP;
        case (byte) 0x11:
            return UDP;
        case (byte) 0x84:
            return SCTP;
        default:
            return null;
        }
    }
}
