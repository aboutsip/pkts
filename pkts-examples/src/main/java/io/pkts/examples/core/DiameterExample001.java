/**
 *
 */
package io.pkts.examples.core;

import io.pkts.PacketHandler;
import io.pkts.Pcap;
import io.pkts.diameter.DiameterHeader;
import io.pkts.framer.FramingException;
import io.pkts.packet.Packet;
import io.pkts.packet.diameter.DiameterPacket;
import io.pkts.protocol.Protocol;

import java.io.File;
import java.io.IOException;

/**
 * A very simple example that just loads a pcap and prints out the content of
 * all UDP packets.
 *
 * @author jonas@jonasborjesson.com
 */
public class DiameterExample001 {

    public static void main(final String[] args) throws IOException, FramingException {

        // Step 1 - The first argument we assume is our file to read.
        final File inputFile = new File(args[0]);

        // Step 2 - obtain a new Pcap instance by supplying an InputStream that points
        //          to a source that contains your captured traffic. Typically you may
        //          have stored that traffic in a file so there are a few convenience
        //          methods for those cases, such as just supplying the name of the
        //          file as shown below.
        final Pcap pcap = Pcap.openStream(inputFile);

        // Step 3 - Once you have obtained an instance, you want to start
        //          looping over the content of the pcap. Do this by calling
        //          the loop function and supply a PacketHandler, which is a
        //          simple interface with only a single method - nextPacket
        pcap.loop(new PacketHandler() {
            @Override
            public boolean nextPacket(final Packet packet) throws IOException {

                // Step 4 - For every new packet the PacketHandler will be
                //          called and you can examine this packet in a few
                //          different ways. You can e.g. check whether the
                //          packet contains a particular protocol, such as UDP
                //          and since we are interested in Diameter, we'll check
                //          for Diameter.
                if (packet.hasProtocol(Protocol.DIAMETER)) {

                    // Step 5 - Now that we know that the packet contains
                    //          a Diameter packet we get ask to get the Diameter packet
                    //          and once we have it we can examine it in anyway we'd like.
                    final DiameterPacket diameter = (DiameterPacket) packet.getPacket(Protocol.DIAMETER);
                    final DiameterHeader header = diameter.getHeader();
                    System.out.println(header);
                }

                return true;
            }
        });
    }

}
