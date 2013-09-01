/**
 * 
 */
package io.pkts.tools;

import io.pkts.Pcap;
import io.pkts.packet.IPPacket;
import io.pkts.packet.sip.SipPacket;
import io.pkts.packet.sip.SipParseException;
import io.pkts.streams.FragmentListener;
import io.pkts.streams.SipStatistics;
import io.pkts.streams.SipStream;
import io.pkts.streams.SipStream.CallState;
import io.pkts.streams.Stream;
import io.pkts.streams.StreamHandler;
import io.pkts.streams.StreamId;
import io.pkts.streams.StreamListener;
import io.pkts.streams.impl.DefaultStreamHandler;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Simple class that takes one or more pcaps and separates out all SIP dialogs
 * from each other and writes them their own pcap files.
 * 
 * This class also serves as an example of how to use the yajpcap library.
 * 
 * @author jonas@jonasborjesson.com
 */
public final class SipSplitter implements StreamListener<SipPacket>, FragmentListener {

    private final Map<StreamId, SipStream> streams = new HashMap<StreamId, SipStream>(20000);

    public int count;

    public int endCount;

    public int fragmented;

    public int rejected;

    public int completed;

    public int failed;

    public int trying;
    public int inCall;
    public int ringing;

    public int cancelled;

    public int calls;

    public long maxPDD;
    public long totalPDD;
    public long pddCount;

    public long maxCallDuration;
    public long totalCallDuration;
    public long callDurationCount;

    /**
     * 
     */
    public SipSplitter() {
        // TODO Auto-generated constructor stub
    }

    public void saveAll(final Pcap pcap, final String directory) throws Exception {
        /*
         * final String dir = (directory == null) || directory.isEmpty() ? "." :
         * directory; for (final SipStream stream : this.streams) { final
         * StreamId id = stream.getStreamIdentifier(); final PcapOutputStream
         * out = pcap.createOutputStream(new FileOutputStream(dir + "/" + id +
         * ".pcap")); try { stream.write(out); } catch (final IOException e) {
         * e.printStackTrace(); } finally { out.flush(); out.close(); } }
         */
    }

    public static void main(final String[] args) throws Exception {

        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.WARN);
        final SipSplitter splitter = new SipSplitter();

        final String filename = "/home/jonas/development/private/aboutsip/modules/yajpcap/src/test/resources/com/aboutsip/yajpcap/sipp.pcap";
        // final String filename = "/home/jonas/development/private/aboutsip/big_pcaps/openser-udp-5060_01871_20121112132549.pcap";

        final long start = System.currentTimeMillis();

        final InputStream is = new FileInputStream(filename);
        final Pcap pcap = Pcap.openStream(is);
        final StreamHandler streamHandler = new DefaultStreamHandler();
        streamHandler.setFragmentListener(splitter);
        streamHandler.addStreamListener(splitter);
        pcap.loop(streamHandler);
        pcap.close();

        final long stop = System.currentTimeMillis();
        System.out.println("Processing time(s): " + (stop - start) / 1000.0);

        // System.out.println("Fragmented pkts: " + ((DefaultStreamHandler) streamHandler).getNoFragmentedPackets());
        final SipStatistics stats = streamHandler.getSipStatistics();
        System.out.println(stats.dumpInfo());

        final Map<StreamId, ? extends Stream> unfinishedStreams = streamHandler.getStreams();
        for (final Map.Entry<StreamId, ? extends Stream> entry : unfinishedStreams.entrySet()) {
            final SipStream stream = (SipStream) entry.getValue();
            splitter.count(stream);
        }

        System.out.println("Start: " + splitter.count);
        System.out.println("End  : " + splitter.endCount);
        System.out.println("Calls  : " + splitter.calls);
        System.out.println("Fragmented  : " + splitter.fragmented);
        System.out.println("Max PDD  : " + splitter.maxPDD);
        System.out.println("Avg PDD  : " + splitter.totalPDD / (double) splitter.pddCount);
        System.out.println("Max Call Duration  : " + splitter.maxCallDuration);
        System.out.println("Avg Call Duration  : " + splitter.totalCallDuration / (double) splitter.callDurationCount);
        System.out.println("Trying  : " + splitter.trying);
        System.out.println("Ringing  : " + splitter.trying);
        System.out.println("In Call  : " + splitter.inCall);
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
    public void startStream(final Stream<SipPacket> stream, final SipPacket message) {
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
    public void packetReceived(final Stream<SipPacket> stream, final SipPacket packet) {
        // TODO Auto-generated method stub
    }

    private void checkPDD(final SipStream stream) throws SipParseException {
        final long pdd = stream.getPostDialDelay() / 1000;
        if (pdd > 40000) {
            System.out.println("PDD crazy high: " + stream.getStreamIdentifier());
        }
        if (pdd != -1) {
            this.maxPDD = Math.max(this.maxPDD, pdd);
            this.totalPDD += pdd;
            ++this.pddCount;
        }
    }

    private void checkDuration(final SipStream stream) throws SipParseException {
        final long duration = stream.getDuration() / 1000;
        if (duration != -1) {
            this.maxCallDuration = Math.max(this.maxCallDuration, duration);
            this.totalCallDuration += duration;
            ++this.callDurationCount;
        }
    }

    public void count(final SipStream stream) throws SipParseException {
        if (this.streams.containsKey(stream.getStreamIdentifier())) {
            return;
        }

        final SipStream.CallState state = stream.getCallState();
        // System.out.println(state);
        if (state == CallState.REJECTED) {
            ++this.rejected;
        } else if (state == CallState.COMPLETED) {
            ++this.completed;
        } else if (state == CallState.FAILED) {
            ++this.failed;
        } else if (state == CallState.RINGING) {
            ++this.ringing;
        } else if (state == CallState.TRYING) {
            ++this.trying;
        } else if (state == CallState.IN_CALL) {
            ++this.inCall;
        } else if (state == CallState.CANCELLED) {
            ++this.cancelled;
        }
        checkPDD(stream);
        checkDuration(stream);

        for (final SipPacket msg : stream.getPackets()) {
            try {
                if (msg.isInvite()) {
                    ++this.calls;
                    break;
                }
            } catch (final SipParseException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void endStream(final Stream<SipPacket> stream) {
        ++this.endCount;
        try {
            count((SipStream) stream);
            this.streams.put(stream.getStreamIdentifier(), (SipStream) stream);
        } catch (final SipParseException e) {
            e.toString();
        }
    }

    @Override
    public IPPacket handleFragment(final IPPacket ipPacket) {
        ++this.fragmented;
        return null;
    }

}
