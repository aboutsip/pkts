/**
 * 
 */
package io.pkts.examples.core;

import io.pkts.PacketHandler;
import io.pkts.Pcap;
import io.pkts.framer.FramingException;
import io.pkts.packet.Packet;
import io.pkts.protocol.Protocol;

import java.io.IOException;

/**
 * A very simple example that just loads a pcap and prints out the content of
 * all UDP packets.
 * 
 * @author jonas@jonasborjesson.com
 */
public class CoreExample001 {

    public static void main(final String[] args) throws IOException, FramingException {

        // Step 1 - obtain a new Pcap instance by supplying an InputStream that points
        //          to a source that contains your captured traffic. Typically you may
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
                //          packet contains a particular protocol, such as UDP.
                if (packet.hasProtocol(Protocol.UDP)) {

                    // Step 4 - Now that we know that the packet contains
                    //          a UDP packet we get ask to get the UDP packet
                    //          and once we have it we can just get its
                    //          payload and print it, which is what we are
                    //          doing below.
                    System.out.println(packet.getPacket(Protocol.UDP).getPayload());
                }

                return true;
            }
        });
    }

}
