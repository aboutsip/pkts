package io.pkts.examples.core;

import io.pkts.PacketHandler;
import io.pkts.Pcap;
import io.pkts.PcapOutputStream;
import io.pkts.framer.FramingException;
import io.pkts.packet.Packet;
import io.pkts.packet.UDPPacket;
import io.pkts.packet.sip.SipPacket;
import io.pkts.protocol.Protocol;
import io.pkts.sdp.RTPInfo;
import io.pkts.sdp.SDP;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This is an example that takes a pcap and extracts outs a particular SIP call along
 * with the potential RTP and writes it to a new pcap.
 *
 * This is very useful if you run a full tcpdump on your production machines and then
 * would like to extract out individual calls. This is also possible to do with wireshark
 * but it has a tendency to crash for larger files. Plus, who wants to do it manually anyway...
 *
 * @author jonas@jonasborjesson.com
 */
public class CoreExample002 {

    public static void main(final String[] args) throws IOException, FramingException {

        // Step 1 - accept two inputs, first one being the pcap from which we will read
        //          and the second argument being the SIP Call-ID we are looking for.
        final File inputFile = new File(args[0]);
        final String callId = args[1];

        // Step 2 - obtain a new Pcap instance by supplying an InputStream that points
        //          to a source that contains your captured traffic. Typically you may
        //          have stored that traffic in a file so there are a few convenience
        //          methods for those cases, such as just supplying the name of the
        //          file as shown below.
        final Pcap pcap = Pcap.openStream(inputFile);

        // Step 3 - Create a new Pcap output stream, to which we will be writing all
        //          those packets we wish to save.
        final File outputFile = new File(inputFile.getParentFile(), callId + ".pcap");
        final PcapOutputStream out = pcap.createOutputStream(new FileOutputStream(outputFile));

        // Step 4 - Since we also want to save any potential RTP for this call
        //          we need to process the SDP in the SIP traffic and check
        //          which ip:port RTP will be coming from for this particular call.
        //          Later on, we will use this information to check if a particular UDP
        //          packet is to/from this IP:port and if so, we will save that packet.
        final List<RTPInfo> rtpInfo = new ArrayList<>();

        // Step 5 - Once you have obtained an instance, you want to start
        //          looping over the content of the pcap. Do this by calling
        //          the loop function and supply a PacketHandler, which is a
        //          simple interface with only a single method - nextPacket
        pcap.loop(new PacketHandler() {
            @Override
            public boolean nextPacket(final Packet packet) throws IOException {

                // Step 6 - In this example we assume SIP & RTP is both over UDP. For
                //          RTP that will most likely always be true (even though you
                //          can technically push it over TCP as well) but certainly not
                //          always true for SIP. However, this is just an example so we
                //          will keep this shortcut for illustrative purposes :-)
                if (packet.hasProtocol(Protocol.UDP)) {
                    final UDPPacket udp = (UDPPacket) packet.getPacket(Protocol.UDP);

                    // Step 7 - If the packet is SIP and matches the call-id we are looking
                    //          for then just save it to the output stream.
                    if (packet.hasProtocol(Protocol.SIP)) {
                        final SipPacket sip = (SipPacket) packet.getPacket(Protocol.SIP);
                        if (callId.equals(sip.getCallIDHeader().getValue().toString())) {
                            out.write(sip);

                            // Step 8 - if the message also has content (and we blindly assume it
                            //          is SDP, which we obviously should check) then extract out
                            //          the RTP information and save that so we later can check if
                            //          a packet is to/from this IP:port
                            if (sip.hasContent()) {
                                final SDP sdp = (SDP) sip.getContent();
                                sdp.getRTPInfo().forEach(info -> rtpInfo.add(info));
                            }

                        }
                    } else if (rtpInfo.stream().filter(info -> (info.getAddress().equals(udp.getParentPacket().getDestinationIP()) && info.getMediaPort() == udp.getDestinationPort())
                                    || (info.getAddress().equals(udp.getParentPacket().getSourceIP()) && info.getMediaPort() == udp.getSourcePort())
                    ).findFirst().isPresent()) {
                        // Step 9 - If the incoming UDP packet is from any known address
                        //          as listed in an SDP then just save it.
                        out.write(packet);
                    }
                }

                return true;
            }
        });

        out.flush();
        out.close();
    }
}
