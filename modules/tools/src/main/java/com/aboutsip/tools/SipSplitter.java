/**
 * 
 */
package com.aboutsip.tools;

import java.io.FileInputStream;
import java.io.InputStream;

import com.aboutsip.streams.Stream;
import com.aboutsip.streams.StreamHandler;
import com.aboutsip.streams.StreamListener;
import com.aboutsip.streams.impl.DefaultStreamHandler;
import com.aboutsip.yajpcap.Pcap;
import com.aboutsip.yajpcap.packet.sip.SipMessage;

/**
 * Simple class that takes one or more pcaps and separates out all SIP dialogs
 * from each other and writes them their own pcap files.
 * 
 * This class also serves as an example of how to use the yajpcap library.
 * 
 * @author jonas@jonasborjesson.com
 */
public final class SipSplitter implements StreamListener<SipMessage> {

    public int count;

    public int endCount;

    /**
     * 
     */
    public SipSplitter() {
        // TODO Auto-generated constructor stub
    }

    public static void main(final String[] args) throws Exception {
        final SipSplitter splitter = new SipSplitter();

        final String filename = "/home/jonas/development/private/aboutsip/modules/yajpcap/src/test/resources/com/aboutsip/yajpcap/sipp.pcap";
        final InputStream stream = new FileInputStream(filename);
        final Pcap pcap = Pcap.openStream(stream);
        final StreamHandler streamHandler = new DefaultStreamHandler();
        streamHandler.addStreamListener(splitter);
        pcap.loop(streamHandler);
        pcap.close();
        System.out.println("Start: " + splitter.count);
        System.out.println("End  : " + splitter.endCount);
    }

    @Override
    public void startStream(final Stream<SipMessage> stream) {
        ++this.count;
    }

    @Override
    public void packetReceived(final Stream<SipMessage> stream, final SipMessage packet) {
        // TODO Auto-generated method stub
    }

    @Override
    public void endStream(final Stream<SipMessage> stream) {
        ++this.endCount;
    }

}
