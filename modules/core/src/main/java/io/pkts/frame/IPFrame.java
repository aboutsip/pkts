/**
 * 
 */
package io.pkts.frame;

import io.pkts.packet.IPPacket;
import io.pkts.packet.PacketParseException;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public interface IPFrame extends Layer3Frame {

    /**
     * The IP version (4 or 6)
     * 
     * @return
     */
    int getVersion();

    /**
     * Get the length of the IP headers (in bytes)
     * 
     * @return
     */
    int getHeaderLength();

    /**
     * This 16-bit field defines the entire packet (fragment) size, including
     * header and data, in bytes. The minimum-length packet is 20 bytes (20-byte
     * header + 0 bytes data) and the maximum is 65,535 bytes — the maximum
     * value of a 16-bit word. The largest datagram that any host is required to
     * be able to reassemble is 576 bytes, but most modern hosts handle much
     * larger packets. Sometimes subnetworks impose further restrictions on the
     * packet size, in which case datagrams must be fragmented. Fragmentation is
     * handled in either the host or router in IPv4.
     * 
     * (source http://en.wikipedia.org/wiki/IPv4)
     * 
     * @return
     */
    int getTotalLength();

    /**
     * Note, this should be treated as a unsigned short.
     * 
     * This field is an identification field and is primarily used for uniquely
     * identifying fragments of an original IP datagram. Some experimental work
     * has suggested using the ID field for other purposes, such as for adding
     * packet-tracing information to help trace datagrams with spoofed source
     * addresses
     * 
     * (source http://en.wikipedia.org/wiki/IPv4)
     * 
     * @return
     */
    int getIdentification();

    /**
     * 
     * @return
     */
    boolean isFragmented();


    /**
     * The Reserved flag is part of the three-bit flag field and those flags
     * are: (in order, from high order to low order):
     * 
     * <pre>
     * bit 0: Reserved; must be zero.
     * bit 1: Don't Fragment (DF)
     * bit 2: More Fragments (MF)
     * </pre>
     * 
     * (source http://en.wikipedia.org/wiki/IPv4)
     * 
     * @return should always return false
     */
    boolean isReservedFlagSet();

    /**
     * The DF flag is part of the three-bit flag field and those flags are: (in
     * order, from high order to low order):
     * 
     * <pre>
     * bit 0: Reserved; must be zero.
     * bit 1: Don't Fragment (DF)
     * bit 2: More Fragments (MF)
     * </pre>
     * 
     * If the DF flag is set, and fragmentation is required to route the packet,
     * then the packet is dropped. This can be used when sending packets to a
     * host that does not have sufficient resources to handle fragmentation. It
     * can also be used for Path MTU Discovery, either automatically by the host
     * IP software, or manually using diagnostic tools such as ping or
     * traceroute. For unfragmented packets, the MF flag is cleared. For
     * fragmented packets, all fragments except the last have the MF flag set.
     * The last fragment has a non-zero Fragment Offset field, differentiating
     * it from an unfragmented packet.
     * 
     * (source http://en.wikipedia.org/wiki/IPv4)
     * 
     * @return
     */
    boolean isDontFragmentSet();

    /**
     * The MF flag is part of the three-bit flag field and those flags are: (in
     * order, from high order to low order):
     * 
     * <pre>
     * bit 0: Reserved; must be zero.
     * bit 1: Don't Fragment (DF)
     * bit 2: More Fragments (MF)
     * </pre>
     * 
     * If the DF flag is set, and fragmentation is required to route the packet,
     * then the packet is dropped. This can be used when sending packets to a
     * host that does not have sufficient resources to handle fragmentation. It
     * can also be used for Path MTU Discovery, either automatically by the host
     * IP software, or manually using diagnostic tools such as ping or
     * traceroute. For unfragmented packets, the MF flag is cleared. For
     * fragmented packets, all fragments except the last have the MF flag set.
     * The last fragment has a non-zero Fragment Offset field, differentiating
     * it from an unfragmented packet.
     * 
     * (source http://en.wikipedia.org/wiki/IPv4)
     * 
     * @return
     */
    boolean isMoreFragmentsSet();

    /**
     * The fragment offset field, measured in units of eight-byte blocks, is 13
     * bits long and specifies the offset of a particular fragment relative to
     * the beginning of the original unfragmented IP datagram. The first
     * fragment has an offset of zero. This allows a maximum offset of (213 – 1)
     * × 8 = 65,528 bytes, which would exceed the maximum IP packet length of
     * 65,535 bytes with the header length included (65,528 + 20 = 65,548
     * bytes).
     * 
     * (source http://en.wikipedia.org/wiki/IPv4)
     * 
     * @return
     */
    short getFragmentOffset();

    /**
     * {@inheritDoc}
     */
    @Override
    IPPacket parse() throws PacketParseException;

}
