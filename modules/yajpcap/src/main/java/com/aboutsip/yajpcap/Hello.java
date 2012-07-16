package com.aboutsip.yajpcap;

import java.io.FileInputStream;

public class Hello {

    public static void main(final String[] args) throws Exception {
        final String filename = "/home/jonas/development/private/yajpcap/modules/yajpcap/src/test/resources/com/jonasborjesson/yajpcap/framer/sipp.pcap";
        // final BufferedInputStream in = new BufferedInputStream(new
        // FileInputStream(filename));
        // final PcapGlobalHeader header = PcapGlobalHeader.parse(in);
        // System.out.println(header);

        final Pcap pcap = Pcap.openStream(new FileInputStream(filename));
        // pcap.loop();


    }
}

