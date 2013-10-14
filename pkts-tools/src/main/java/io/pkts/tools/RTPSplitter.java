package io.pkts.tools;

import io.pkts.PacketHandler;
import io.pkts.Pcap;
import io.pkts.PcapOutputStream;
import io.pkts.packet.Packet;
import io.pkts.packet.PacketParseException;
import io.pkts.packet.rtp.RtpPacket;
import io.pkts.packet.sip.SipPacket;
import io.pkts.protocol.Protocol;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RTPSplitter {

    /**
     * 
     * @param args
     */
    public static void main(final String[] args) throws Exception {
        /*
         * if (args.length < 1) { System.err.println("ERROR: missing pcap");
         * System.err.println("Usage: rtptools <pcap-file>"); System.exit(1); }
         */

        final String filename = "/home/jonas/development/private/aboutsip/countdown.pcap";
        // final String filename = "cool_0.pcap";

        final InputStream stream = new FileInputStream(filename);
        final Pcap pcap = Pcap.openStream(stream);
        final FrameHandlerImpl handler = new FrameHandlerImpl();
        pcap.loop(handler);
        pcap.close();

        handler.saveSipFlows(pcap, "siptraffic_again");
        handler.saveAllRtpStreams(pcap, "rtpstream_again");
    }

    private static class FrameHandlerImpl implements PacketHandler {

        private final Map<String, RtpStream> streams = new HashMap<String, RtpStream>();

        private final Map<String, SipFlow> sipFlows = new HashMap<String, SipFlow>();

        @Override
        public void nextPacket(final Packet frame) {
            try {
                if (frame.hasProtocol(Protocol.SIP)) {
                    processSipFrame((SipPacket) frame.getPacket(Protocol.SIP));
                } else if (frame.hasProtocol(Protocol.RTP)) {
                    processRtpFrame((RtpPacket) frame.getPacket(Protocol.RTP));
                }
            } catch (final IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (final PacketParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        public void saveSipFlows(final Pcap pcap, final String prefix) throws IOException {
            int count = 0;
            for (final SipFlow stream : this.sipFlows.values()) {
                final PcapOutputStream out = pcap.createOutputStream(new BufferedOutputStream(new FileOutputStream(
                        prefix + "_" + count + ".pcap")));
                stream.saveStream(out);
                ++count;
            }
        }

        public void saveAllRtpStreams(final Pcap pcap, final String prefix) throws IOException {
            int count = 0;
            for (final RtpStream stream : this.streams.values()) {
                final PcapOutputStream out = pcap.createOutputStream(new BufferedOutputStream(new FileOutputStream(
                        prefix + "_" + count + ".pcap")));
                stream.saveStream(out);
                ++count;
            }
        }

        private void processSipFrame(final SipPacket msg) throws PacketParseException {
            final String callId = msg.getCallIDHeader().getValue().toString();
            SipFlow flow = this.sipFlows.get(callId);
            if (flow == null) {
                flow = new SipFlow();
                this.sipFlows.put(callId, flow);
            }
            flow.addPacket(msg);
        }

        private void processRtpFrame(final RtpPacket rtp) throws PacketParseException {
            System.out.println(rtp);
            final String key = rtp.getSourceIP() + rtp.getSourcePort();
            RtpStream stream = this.streams.get(key);
            if (stream == null) {
                stream = new RtpStream();
                this.streams.put(key, stream);
            }

            stream.addPacket(rtp);
        }
    }

    private static class SipFlow {
        private final List<SipPacket> stream = new ArrayList<SipPacket>();

        public SipFlow() {
            // left empty intentionally
        }

        public void addPacket(final SipPacket pkt) {
            this.stream.add(pkt);
        }

        public void saveStream(final PcapOutputStream out) throws IOException {
            for (final SipPacket pkt : this.stream) {
                out.write(pkt);
            }
            out.flush();
            out.close();
        }

    }

    private static class RtpStream {

        private final List<RtpPacket> stream = new ArrayList<RtpPacket>();

        public RtpStream() {
            // left empty intentionally
        }

        public void addPacket(final RtpPacket frame) {
            this.stream.add(frame);
        }

        public void saveStream(final PcapOutputStream out) throws IOException {
            for (final RtpPacket rtp : this.stream) {
                out.write(rtp);
            }
            out.flush();
            out.close();
            // final byte[] s = byteStream.toByteArray();
            // System.out.println(HexDump.dumpHexString(s));
        }

    }
}
