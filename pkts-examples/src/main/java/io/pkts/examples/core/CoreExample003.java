/**
 *
 */
package io.pkts.examples.core;

import io.pkts.PacketHandler;
import io.pkts.Pcap;
import io.pkts.framer.FramingException;
import io.pkts.packet.IPPacket;
import io.pkts.packet.Packet;
import io.pkts.packet.TransportPacket;
import io.pkts.protocol.Protocol;

import java.io.IOException;
import java.util.Optional;

/**
 * A very simple example that just loads a pcap and prints out information about
 * either the TCP or UDP packets.
 *
 * @author jonas@jonasborjesson.com
 */
public class CoreExample003 {

    public static void main(final String[] args) throws IOException, FramingException {

        // Step 1 - obtain a new Pcap instance by supplying an InputStream that points
        //          to a source that contains your captured traffic. Typically, you may
        //          have stored that traffic in a file so there are a few convenience
        //          methods for those cases, such as just supplying the name of the
        //          file as shown below.
        final Pcap pcap = Pcap.openStream("my_traffic.pcap");

        // Step 2 - Once you have obtained an instance, you want to start
        //          looping over the content of the pcap. Do this by calling
        //          the loop function and supply a PacketHandler, which is a
        //          simple interface with only a single method - nextPacket
        pcap.loop(new PacketHandler() {
            @Override
            public boolean nextPacket(final Packet packet) throws IOException {

                // Step 3 - For every new packet the PacketHandler will be
                //          called and you can examine this packet in a few
                //          different ways. You can e.g. check whether the
                //          packet contains a particular protocol, such as UDP
                //          or TCP. In this example, we are interested in the
                //          information regarding remote IP:port etc and that
                //          information exists in the IP packet + the Transport
                //          Packet (so UDP or TCP - both transport protocols)
                getTransportPacket(packet).ifPresent(transportPacket -> {
                    final int sourcePort = transportPacket.getSourcePort();
                    final int destPort = transportPacket.getDestinationPort();

                    // Here we just simply assume that the payload is plain text (UTF8)
                    // but if it isn't, you need to handle the payload differently.
                    // E.g., if it is still in plain text but ASCII only then do
                    // final String ascii = new String(transportPacket.getPayload().getArray(), StandardCharsets.US_ASCII);
                    final String payload = transportPacket.getPayload().toString();

                    // For IP addresses, those are present in the IP packet, which
                    // for the transport layer will always be the parent packet so we
                    // can just ask for it.
                    final IPPacket ip = transportPacket.getParentPacket();
                    final String destIp = ip.getDestinationIP();
                    final String sourceIp = ip.getSourceIP();

                    System.out.println(destIp + ":" + sourcePort + " -> " + sourceIp + ":" + sourcePort);
                    System.out.println(payload);
                });

                // Signal to the pcap loop that you're still "happy" and want to continue
                // parsing the remainder of the pcap.
                return true;
            }
        });
    }

    private static Optional<TransportPacket> getTransportPacket(final Packet packet) throws IOException {
        if (packet.hasProtocol(Protocol.TCP)) {
            // TCP is a transport protocol and therefore, it extends the base packet
            // TransportPacket. You could also convert it into a TCPPacket but for
            // this example, we are not interested in TCP packet specific information.
            // Just Transport Layer in general (source ip, dest ip etc)
            return Optional.of((TransportPacket) packet.getPacket(Protocol.TCP));
        }

        if (packet.hasProtocol(Protocol.UDP)) {
            return Optional.of((TransportPacket) packet.getPacket(Protocol.UDP));
        }

        return Optional.empty();
    }

}
