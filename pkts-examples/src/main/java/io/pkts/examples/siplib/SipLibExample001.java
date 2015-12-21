package io.pkts.examples.siplib;

import io.pkts.packet.sip.SipMessage;
import io.pkts.packet.sip.SipRequest;
import io.pkts.packet.sip.SipResponse;
import io.pkts.packet.sip.address.SipURI;
import io.pkts.packet.sip.header.*;

import java.io.IOException;

/**
 * This file contains basic examples of how to use SIP Lib (pkts.io sip support)
 * for parsing, creating and manipulating SIP messages.
 *
 * @author jonas@jonasborjesson.com
 */
public class SipLibExample001 {

    /**
     * This is the most basic example showing how you can parse a SIP
     * message based off of a String. In this example, we are the ones
     * creating that raw message ourselves but typically you would read
     * this off of the network, or perhaps from a file if you are building
     * a tool of some sort.
     */
    public static void basicExample001() throws IOException {
        final String rawMessage = new StringBuilder("BYE sip:bob@127.0.0.1:5060 SIP/2.0\r\n")
                .append("Via: SIP/2.0/UDP 127.0.1.1:5061;branch=z9hG4bK-28976-1-7\r\n")
                .append("From: alice <sip:alice@127.0.1.1:5061>;tag=28976SIPpTag001\r\n")
                .append("To: bob <sip:bob@127.0.0.1:5060>;tag=28972SIPpTag011\r\n")
                .append("Call-ID: 1-28976@127.0.1.1\r\n")
                .append("CSeq: 2 BYE\r\n")
                .append("Contact: sip:alice@127.0.1.1:5061\r\n")
                .append("Max-Forwards: 70\r\n")
                .append("Subject: Example BYE Message\r\n")
                .append("Content-Length: 0\r\n")
                .append("\r\n").toString();

        // Every object in SIP Lib has a frame-method, which
        // will attempt to frame the raw content into that object.
        // This is true for SIP messages, SIP header, SIP URIs
        // etc etc. All frame-methods are overloaded and accept
        // Strings, Buffers and byte-arrays.
        final SipMessage msg = SipMessage.frame(rawMessage);

        // Once the message has successfully been parsed you
        // can access headers etc within the SIP message.
        final FromHeader from = msg.getFromHeader();

        // All headers that typically are needed for any application, and
        // in particular for SIP stacks, have explicit methods and returns
        // explicit objects. You may still use the generic getHeader but then
        // you will get a generic SIP header back.
        final ContactHeader contact = msg.getContactHeader();

        // Instead of having to do SipMessage.getMethod().equals("BYE") etc
        // the SIP message has many convenience methods for making the code
        // more readable, less error prone and less boiler place to write.
        if (msg.isBye()) {
            System.out.println("Yay, this was a BYE message");
        }

        if (msg.isRequest()) {
            System.out.println("Yay, this was SIP request");
        }
    }

    /**
     * All objects within SIP Lib are immutable. Hence, once an object, such as a
     * {@link SipMessage}, a {@link SipHeader} or a e.g. a {@link SipURI} has been
     * constructed you cannot change it. Therefore, all objects within SIP Lib make
     * use of the builder pattern, allowing the user to construct the desired object.
     *
     * Furthermore, the builders offers up the possibility for registering lambda-functions
     * to be executed when e.g. the Contact-header is constructed by a {@link io.pkts.packet.sip.SipMessage.Builder}
     * and is a very powerful concept, which will be further explored by other examples in this file.
     *
     * By default, the builder will create an a SIP Request with
     * the following default headers:
     * <ul>
     *     <li>{@link ToHeader} - the request-uri will be used to construct the to-header
     *     in the case of a request. For a response you have to supply it</li>
     *     <li>{@link CSeqHeader} - a new CSeq header will be added where the
     *     method is the same as this message and the sequence number is set to 1</li>
     *     <li>{@link CallIdHeader} - a new random call-id will be added</li>
     *     <li>{@link MaxForwardsHeader} - if we are building a request, a max forwards of 70 will be added</li>
     *     <li>{@link ContentLengthHeader} - Will be added if there is a body
     *     on the message and the length set to the correct length.</li>
     * </ul>
     *
     * @throws Exception
     */
    public static void basicExample002() throws Exception {

        final SipRequest invite = SipRequest.invite("sip:alice@aboutsip.com")
                .withFromHeader("sip:bob@pkts.io")
                .build();

        System.out.println(invite);
    }

    /**
     * Creating responses is typically done based off a request and SIP Lib
     * allows you to do so but of course, the object returned will be a builder.
     *
     * @throws Exception
     */
    public static void basicExample003() throws Exception {

        // Generate a request again
        final SipRequest invite = SipRequest.invite("sip:alice@aboutsip.com")
                .withFromHeader("sip:bob@pkts.io")
                .build();

        // Create a 200 OK to that INVITE and also add a generic
        // header for good measure.
        final SipResponse response = invite.createResponse(200)
                .withHeader(SipHeader.create("X-Hello", "World"))
                .build();

        System.out.println(response);
    }

    public static void main(final String ... args) throws Exception {
        SipLibExample001.basicExample001();
        SipLibExample001.basicExample002();
        SipLibExample001.basicExample003();
    }
}
