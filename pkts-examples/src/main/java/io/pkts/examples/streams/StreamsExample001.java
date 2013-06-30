/**
 * 
 */
package io.pkts.examples.streams;

import io.pkts.Pcap;
import io.pkts.packet.sip.SipMessage;
import io.pkts.streams.SipStream;
import io.pkts.streams.Stream;
import io.pkts.streams.StreamHandler;
import io.pkts.streams.StreamListener;
import io.pkts.streams.impl.DefaultStreamHandler;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Simple example showing how to use streams.
 * 
 * The core pcap support provided by pkts.io is only focusing on each individual
 * packet but quite often you application may be interested in a stream of
 * packets. A stream can mean different things for different protocols. E.g. for
 * UDP, a stream in this context could be all packets sent and received from the
 * same local and remote port-pair (which is how the stream support in pkts.io
 * has defined a UDP stream).
 * 
 * For other protocols, there may be other identifiers within the protocol that
 * defines what a stream is. As an example, SIP has its own concept of how to
 * tie related SIP messages together (in SIP this is called a dialog) so for SIP
 * a stream is the same as a SIP dialog.
 * 
 * This particular example shows how to setup pkts.io and its stream support to
 * consume {@link SipStream}s.
 * 
 * @author jonas@jonasborjesson.com
 */
public final class StreamsExample001 {

    public static void main(final String[] args) throws FileNotFoundException, IOException {

        // Step 1 - Open the pcap containing our traffic.
        final Pcap pcap = Pcap.openStream("my_traffic.pcap");

        // Step 2 - Instead of implementing our own FrameListener we will be
        //          using a StreamHandler provided for us by the io.pkts.streams
        //          library. It has a DefaultStreamHandler (which obviously
        //          implements the FrameHandler) that will detect new streams
        //          and call a StreamListener when appropriate.
        final StreamHandler streamHandler = new DefaultStreamHandler();

        // Step 3 - In this simple example we will just supply a very basic
        //          StreamListener for SipMessages only. All we will do is
        //          print to std out when a new event occurs for a stream.
        streamHandler.addStreamListener(new StreamListener<SipMessage>() {

            @Override
            public void startStream(final Stream<SipMessage> stream, final SipMessage packet) {
                System.out.println("New SIP stream detected. Stream id: " + stream.getStreamIdentifier());
                System.out.println("SipMessage was: " + packet.getInitialLine());
            }

            @Override
            public void packetReceived(final Stream<SipMessage> stream, final SipMessage packet) {
                System.out.println("Received a new SIP message for stream: " + stream.getStreamIdentifier());
                System.out.println("SipMessage was: " + packet.getInitialLine());
            }

            @Override
            public void endStream(final Stream<SipMessage> stream) {
                System.out.println("The stream ended. Stream id: " + stream.getStreamIdentifier());
            }
        });

        // Step 4 - Call the loop function as usual but pass in the StreamHandler
        //          instead of your own "raw" FrameHandler.
        pcap.loop(streamHandler);
    }
}
