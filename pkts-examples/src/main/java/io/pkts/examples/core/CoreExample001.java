/**
 * 
 */
package io.pkts.examples.core;

import io.pkts.FrameHandler;
import io.pkts.Pcap;
import io.pkts.frame.Frame;
import io.pkts.protocol.Protocol;

import java.io.IOException;

/**
 * A very simple example that just loads a pcap and prints out the content of
 * all UDP packets.
 * 
 * @author jonas@jonasborjesson.com
 */
public class CoreExample001 {

    public static void main(final String[] args) throws IOException {

        // Step 1 - obtain a new Pcap instance by supplying an InputStream that points
        //          to a source that contains your captured traffic. Typically you may
        //          have stored that traffic in a file so there are a few convenience
        //          methods for those cases, such as just supplying the name of the
        //          file as shown below.
        final Pcap pcap = Pcap.openStream("my_traffic.pcap");

        // Step 2 - Once you have obtained an instance, you want to start 
        //          looping over the content of the pcap. Do this by calling
        //          the loop function and supply a FrameHandler, which is a
        //          simple interface with only a single method - nextFrame
        pcap.loop(new FrameHandler() {
            @Override
            public void nextFrame(final Frame frame) throws IOException {

                // Step 3 - For every new frame the FrameHandler will be 
                //          called and you can examine this frame in a few
                //          different ways. You can e.g. check whether the
                //          frame has a particular protocol, such as UDP.
                if (frame.hasProtocol(Protocol.UDP)) {

                    // Step 4 - Now that we know that the raw frame contains
                    //          a UDP packet we get ask to get the UDP frame
                    //          and once we have it we can just get its
                    //          payload and print it, which is what we are
                    //          doing below.
                    System.out.println(frame.getFrame(Protocol.UDP).getPayload());
                }
            }
        });
    }

}
