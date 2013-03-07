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

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.aboutsip.streams.FragmentListener;
import com.aboutsip.streams.SipStatistics;
import com.aboutsip.streams.SipStream;
import com.aboutsip.streams.SipStream.CallState;
import com.aboutsip.streams.Stream;
import com.aboutsip.streams.StreamHandler;
import com.aboutsip.streams.StreamId;
import com.aboutsip.streams.StreamListener;
import com.aboutsip.streams.impl.DefaultStreamHandler;
import com.aboutsip.yajpcap.Pcap;
import com.aboutsip.yajpcap.PcapOutputStream;
import com.aboutsip.yajpcap.frame.IPFrame;
import com.aboutsip.yajpcap.packet.sip.SipMessage;
import com.aboutsip.yajpcap.packet.sip.impl.SipParseException;

/**
 * Simple class that takes one or more pcaps and separates out all SIP dialogs
 * from each other and writes them their own pcap files.
 * 
 * This class also serves as an example of how to use the yajpcap library.
 * 
 * @author jonas@jonasborjesson.com
 */
public final class SipSplitter implements StreamListener<SipMessage>, FragmentListener {

    private final List<SipStream> streams = new ArrayList<SipStream>();

    public int count;

    public int endCount;

    public int fragmented;

    public int rejected;

    public int completed;

    public int failed;

    public int cancelled;

    public int calls;

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

        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.WARN);
        final SipSplitter splitter = new SipSplitter();

        final String filename = "/home/jonas/development/private/aboutsip/modules/yajpcap/src/test/resources/com/aboutsip/yajpcap/sipp.pcap";


        final long start = System.currentTimeMillis();

        final InputStream stream = new FileInputStream(filename);
        final Pcap pcap = Pcap.openStream(stream);
        final StreamHandler streamHandler = new DefaultStreamHandler();
        streamHandler.setFragmentListener(splitter);
        streamHandler.addStreamListener(splitter);
        pcap.loop(streamHandler);
        pcap.close();

        final long stop = System.currentTimeMillis();
        System.out.println("Processing time(s): " + ((stop - start) / 1000.0));
        // System.out.println("Fragmented pkts: " + ((DefaultStreamHandler) streamHandler).getNoFragmentedPackets());
        final SipStatistics stats = streamHandler.getSipStatistics();
        System.out.println("Start: " + splitter.count);
        System.out.println("End  : " + splitter.endCount);
        System.out.println("Calls  : " + splitter.calls);
        System.out.println("Fragmented  : " + splitter.fragmented);
        System.out.println(stats.dumpInfo());

        System.out.println("Rejected  : " + splitter.rejected);
        System.out.println("Completed  : " + splitter.completed);
        System.out.println("Failed  : " + splitter.failed);
        System.out.println("Cancelled  : " + splitter.cancelled);
        final int[] responses = stats.totalResponses();
        int count = 0;
        for (int i = 200; i < responses.length; ++i) {
            count += responses[i];
        }
        System.out.println(" total bad responses" + count);
        // splitter.saveAll(pcap, null);
    }

    @Override
    public void startStream(final Stream<SipMessage> stream, final SipMessage message) {
        try {
            if (message.isInfo() || message.isMessage() || message.isOptions()) {
                System.out.println("Strange...");
                System.out.println(message);
            }
        } catch (final SipParseException e) {
            e.printStackTrace();
        }
        ++this.count;
    }

    @Override
    public void packetReceived(final Stream<SipMessage> stream, final SipMessage packet) {
        // TODO Auto-generated method stub
    }

    @Override
    public void endStream(final Stream<SipMessage> stream) {
        ++this.endCount;
        final SipStream.CallState state = ((SipStream) stream).getCallState();
        // System.out.println(state);
        if (state == CallState.REJECTED) {
            ++this.rejected;
        } else if (state == CallState.COMPLETED) {
            ++this.completed;
        } else if (state == CallState.FAILED) {
            ++this.failed;
        } else if (state == CallState.CANCELLED) {
            ++this.cancelled;
        }
        final SipMessage msg = stream.getPackets().next();
        try {
            if (msg.isRequest() && msg.isInvite()) {
                ++this.calls;
            }
        } catch (final SipParseException e) {
            e.printStackTrace();
        }
        this.streams.add((SipStream) stream);
    }

    @Override
    public IPFrame handleFragment(final IPFrame ipFrame) {
        ++this.fragmented;
        return null;
    }

}
