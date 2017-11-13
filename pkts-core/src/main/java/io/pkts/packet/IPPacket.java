/**
 * 
 */
package io.pkts.packet;

/**
 * Represents a packet from the Network Layer (layer 3). Actually, to be
 * completely honest, the model implemented (at least so far) is more geared
 * towards what is commonly referred to as the Internet Layer and is strictly
 * speaking not quite the same as the Network Layer as specified by the OSI
 * model. However, until it becomes an issue this little "issue" is going to be
 * ignored and for now the Network Layer is equal to the Internet Layer.
 * 
 * The current version of pkts.io is focused on IP anyway so...
 * 
 * @author jonas@jonasborjesson.com
 */
public interface IPPacket extends Packet, Cloneable {
    /**
     * Convenience method for returning the source IP in a more human readable form.
     * 
     * @return
     */
    String getSourceIP();

    /**
     * Get the raw bytes, either 4 or 16, that represent the source IP.
     * @return
     */
    byte[] getRawSourceIP();

    /**
     * Set the source IP of this {@link IPPacket}.
     *
     * @param sourceIp
     */
    void setSourceIP(String sourceIp);

    /**
     * Convenience method for returning the destination IP in a more human readable form.
     * 
     * @return
     */
    String getDestinationIP();

    /**
     * Get the raw bytes, either 4 or 16, that represent the destination IP address.
     * @return
     */
    byte[] getRawDestinationIP();


    /**
     * Set the destination IP of this {@link IPPacket}.
     */
    void setDestinationIP(String destinationIP);

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
     * (source: http://en.wikipedia.org/wiki/IPv4)
     * 
     * @return
     */
    int getTotalIPLength();

    @Override
    IPPacket clone();

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
}
