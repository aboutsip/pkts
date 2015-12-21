# pkts.io

pkts.io is a pure java library for reading and writing pcaps. It's primary purpose is to manipulate/analyze existing pcaps, allowing you to build various tools around pcaps.

For full documentation, please see [aboutsip.com](http://www.aboutsip.com/pktsio/)


----------------------------------------
--- Old stuff that needs to be moved ---
----------------------------------------

There may be situations where you need to post process pcap files but the existing pcap libraries doesn't do the job for one reason or another. E.g., most of them are using libpcap under the hood and can therefore be challenging to get working across platforms.

However, if you do need more abilities that just reading you should check out the following libraries:

* http://jnetpcap.com/
* http://netresearch.ics.uci.edu/kfujii/Jpcap/doc/

In fact, you should check them out anyway :-)

## Details to be moved into javadoc...

Framers take raw data and only examines just enough of it in order to find the boundaries of the message and frame it. The result of this operation is a Frame.

A Frame contains raw data but has examined some of the information in the frame in order to be able to make decisions such as what payload it contains etc. A Frame can be asked to fully parse its data in order to produce a Packet.

A Packet is a parsed frame and has usually partially examined all the data within the frame to find all the necessary information about that particular protocol packet. However, everything in yajpcap is done lazily so even though the first pass of the data didn't find any obvious errors, there still can be some. If you really need to find out if all the data within the package is correct, you can call Packet.verify() which forces the packet to check everything and complain if it finds any errors.

# How To

This section describes how to e.g. add a new protocol framer and how to test that it is working.

## How to add a new protocol

1. Define a new Protocol in the Protocol enum.
1. Create a new Framer and add it into the appropriate package.
1. Add a new Frame for the new protocol.
1. Define and implement a new Packet, which is what we ultimately want.
1. Register your new Framer with the FramerManager.
1. Done...


### Define new Protocol


### Create a new Framer

Every protocol, no matter how simple it is, needs a Framer. The job of the framer is just to frame the entire message for that particular protocol. The framer is not supposed to do any extensive validation of the content, it should only concentrate on finding the boundaries of the data and that's it. The framer needs to be as fast as possible so feel free to make a lot of assumptions of the data. If it so happens that the data is corrupt in some way, that will later be detected when the content is parsed into a packet.

So, create a new protocol framer and add it to the corresponding framer layer. E.g., the most common thing to do is probably to create a new io.sipstack.application.application layer framer such as one for HTTP or RTP etc. In this case, that Framer (let's call is RTPFramer) should live in the package com.aboutsip.yajpcap.framer.layer7. Note, there is a helper interface in each different layer (such as Layer7Framer) that you probably want to extend.

## Example: adding RTP

RTP (real-time transport protocol) is a simple protocol for transporting e.g. audio. We will use this protocol as an example of how to add a new protocol to yajpcap. RTP is defined in RFC3550.

### Define a new Protocol

We are simply going to add the RTP enum into com.aboutsip.yajpcap.protocol.Protocol like so:

	ICMP("icmp"), IGMP("igmp"), TCP("tcp"), UDP("udp"), SCTP("sctp"), SIP("sip"), SDP("sdp"), ETHERNET_II("eth"), SLL("sll"), IPv4( "ip"), PCAP("pcap"), RTP("rtp"), UNKNOWN("unknown");


### Create a new Framer

Let's create a new RTPFramer and add it to the com.aboutsip.yajpcap.framer.layer7. We could implement the Framer interface directly but there is a helper interface for each layer that makes things a little more useful so let's implement the Layer7Framer instead.

``` java
package com.aboutsip.yajpcap.framer.layer7;

public final class RTPFramer implements Layer7Framer {

    private final FramerManager framerManager;

    public RTPFramer(final FramerManager framerManager) {
        this.framerManager = framerManager;
    }

    @Override
    public Protocol getProtocol() {
        return Protocol.RTP;
    }

    @Override
    public boolean accept(final Buffer data) throws IOException {
        return false;
    }

    @Override
    public Layer7Frame frame(final Layer4Frame parent, final Buffer buffer) throws IOException {
        return null;
    }
}
```

The above code is our skeleton for creating the new RTPFramer. I already implemented the ```getProtocol()``` method since it is so simple. Also, the constructor is taking a FramerManager, which we will be using later on so you probably want to include this one as well in your framer. The next steps are to implement the accept and frame methods.

#### Implementing the accept-method.

The purpose of the accept method is to check whether the supplied buffer could be an RTP frame or not. The trick here is to look ahead just as much as we need to figure out whether it is a good chance of this data being of the prefered type or not. In our case, the preferred type is of course RTP. Depending on the protocol you are adding, this can be easier said than done. E.g., in the case of HTTP  you may look three bytes a head and see if those bytes either are "GET", "PUT", "DEL" (for delete) or "POS" (for post) and if so, then we will return true and hope that this indeed is an HTTP frame. Of course, there is a chance that these three bytes may just accidently are "GET" and then we will falsly report true. At the same time though, we cannot go over too much either since the entire yajpcap library would be so slow it would be useless. Hence, it is up to you to decide how much look ahead you need to do in order to be fairly confident that your packet is what you are looking for.

So, back to RTP.  Unfortunately, there is no real good test to make sure that the data indeed is an RTP packet. Appendix 2 in RFC3550 describes one way of doing it but you really need a sequence of packets in order to be able to determine if this indeed is a RTP packet or not. The best is to analyze the session negotiation but here we are just looking at a single packet so can't do that. Therefore, we will only do what we can which isn't much. First, the size of the headers is always at least 12 bytes so let's verify that we have that. Also, the version of the RTP packet is most commonly 2 and you will find that in the first byte of the message. If both of these things are true then we return true.

``` java
public boolean accept(final Buffer data) throws IOException {

    if (data.readableBytes() < 12) {
        data.markReaderIndex();
        final Buffer b = data.readBytes(12);
        if (b.capacity() < 12) {
            return false;
        }
        data.resetReaderIndex();
    }

    final byte b = data.getByte(0);
    return ((b & 0xC0) >> 6) == 0x02;
}
```
Remember, this is a very basic check that is very likely to incorrectly claim that a packet is an RTP packet even though it is not. 

#### Implementing the frame-method.


