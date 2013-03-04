/**
 * 
 */
package com.aboutsip.tools;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.aboutsip.streams.SipStream;
import com.aboutsip.streams.Stream;
import com.aboutsip.streams.StreamHandler;
import com.aboutsip.streams.StreamId;
import com.aboutsip.streams.StreamListener;
import com.aboutsip.streams.impl.DefaultStreamHandler;
import com.aboutsip.yajpcap.Pcap;
import com.aboutsip.yajpcap.PcapOutputStream;
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

    private final List<SipStream> streams = new ArrayList<SipStream>();

    public int count;

    public int endCount;

    /**
     * 
     */
    public SipSplitter() {
        // TODO Auto-generated constructor stub
    }

    public void saveAll(final Pcap pcap, final String directory) throws Exception {
        final String dir = (directory == null) || directory.isEmpty() ? "." : directory;
        for (final SipStream stream : this.streams) {
            final StreamId id = stream.getStreamIdentifier();
            final PcapOutputStream out = pcap.createOutputStream(new FileOutputStream(dir + "/" + id + ".pcap"));
            try {
                stream.write(out);
            } catch (final IOException e) {
                e.printStackTrace();
            } finally {
                out.flush();
                out.close();
            }
        }
    }

    public static void main(final String[] args) throws Exception {
        final SipSplitter splitter = new SipSplitter();

        final String filename = "/home/jonas/development/private/aboutsip/modules/yajpcap/src/test/resources/com/aboutsip/yajpcap/sipp.pcap";


        final long start = System.currentTimeMillis();
        final InputStream stream = new FileInputStream(filename);
        final Pcap pcap = Pcap.openStream(stream);
        final StreamHandler streamHandler = new DefaultStreamHandler();
        streamHandler.addStreamListener(splitter);
        pcap.loop(streamHandler);
        pcap.close();
        final long stop = System.currentTimeMillis();
        System.out.println("Processing time(s): " + ((stop - start) / 1000));
        // System.out.println("Fragmented pkts: " + ((DefaultStreamHandler) streamHandler).getNoFragmentedPackets());
        System.out.println("Start: " + splitter.count);
        System.out.println("End  : " + splitter.endCount);
        // splitter.saveAll(pcap, null);
    }

    @Override
    public void startStream(final Stream<SipMessage> stream, final SipMessage message) {
        ++this.count;
    }

    @Override
    public void packetReceived(final Stream<SipMessage> stream, final SipMessage packet) {
        // TODO Auto-generated method stub
    }

    @Override
    public void endStream(final Stream<SipMessage> stream) {
        ++this.endCount;
        this.streams.add((SipStream) stream);
    }

}
