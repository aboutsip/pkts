package com.aboutsip.tools;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aboutsip.yajpcap.FrameHandler;
import com.aboutsip.yajpcap.Pcap;
import com.aboutsip.yajpcap.PcapOutputStream;
import com.aboutsip.yajpcap.frame.Frame;
import com.aboutsip.yajpcap.frame.RtpFrame;
import com.aboutsip.yajpcap.frame.SipFrame;
import com.aboutsip.yajpcap.packet.PacketParseException;
import com.aboutsip.yajpcap.packet.rtp.RtpPacket;
import com.aboutsip.yajpcap.packet.sip.SipMessage;
import com.aboutsip.yajpcap.protocol.Protocol;

public class RTPSplitter {

    /**
     * 
     * @param args
     */
    public static void main(final String[] args) throws Exception {
        /*
        if (args.length < 1) {
            System.err.println("ERROR: missing pcap");
            System.err.println("Usage: rtptools <pcap-file>");
            System.exit(1);
        }
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

    private static class FrameHandlerImpl implements FrameHandler {

        private final Map<String, RtpStream> streams = new HashMap<String, RtpStream>();

        private final Map<String, SipFlow> sipFlows = new HashMap<String, SipFlow>();

        @Override
        public void nextFrame(final Frame frame) {
            try {
                if (frame.hasProtocol(Protocol.SIP)) {
                    processSipFrame((SipFrame) frame.getFrame(Protocol.SIP));
                } else if (frame.hasProtocol(Protocol.RTP)) {
                    processRtpFrame((RtpFrame) frame.getFrame(Protocol.RTP));
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
                final PcapOutputStream out = pcap.createOutputStream((new BufferedOutputStream(new FileOutputStream(
                        prefix + "_" + count + ".pcap"))));
                stream.saveStream(out);
                ++count;
            }
        }

        public void saveAllRtpStreams(final Pcap pcap, final String prefix) throws IOException {
            int count = 0;
            for (final RtpStream stream : this.streams.values()) {
                final PcapOutputStream out = pcap.createOutputStream((new BufferedOutputStream(new FileOutputStream(
                        prefix + "_" + count + ".pcap"))));
                stream.saveStream(out);
                ++count;
            }
        }

        private void processSipFrame(final SipFrame sipFrame) throws PacketParseException {
            final SipMessage msg = sipFrame.parse();
            final String callId = msg.getCallIDHeader().getValue().toString();
            SipFlow flow = this.sipFlows.get(callId);
            if (flow == null) {
                flow = new SipFlow();
                this.sipFlows.put(callId, flow);
            }
            flow.addPacket(sipFrame);
        }

        private void processRtpFrame(final RtpFrame rtpFrame) throws PacketParseException {
            final RtpPacket rtp = rtpFrame.parse();
            System.out.println(rtp);
            final String key = rtp.getSourceIP() + rtp.getSourcePort();
            RtpStream stream = this.streams.get(key);
            if (stream == null) {
                stream = new RtpStream();
                this.streams.put(key, stream);
            }

            stream.addPacket(rtpFrame);
        }
    }

    private static class SipFlow {
        private final List<SipFrame> stream = new ArrayList<SipFrame>();

        public SipFlow() {
            // left empty intentionally
        }

        public void addPacket(final SipFrame frame) {
            this.stream.add(frame);
        }

        public void saveStream(final PcapOutputStream out) throws IOException {
            for (final SipFrame frame : this.stream) {
                out.write(frame);
            }
            out.flush();
            out.close();
        }

    }

    private static class RtpStream {

        private final List<RtpFrame> stream = new ArrayList<RtpFrame>();

        public RtpStream() {
            // left empty intentionally
        }

        public void addPacket(final RtpFrame frame) {
            this.stream.add(frame);
        }

        public void saveStream(final PcapOutputStream out) throws IOException {
            for (final RtpFrame frame : this.stream) {
                out.write(frame);
            }
            out.flush();
            out.close();
            // final byte[] s = byteStream.toByteArray();
            // System.out.println(HexDump.dumpHexString(s));
        }

    }
}
