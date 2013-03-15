/**
 * 
 */
package com.aboutsip.yajpcap.packet;


/**
 * Represents a packet from the Network Layer (layer 3). Actually, to be
 * completely honest, the model implemented (at least so far) is more geared
 * towards what is commonly referred to as the Internet Layer and is strictly
 * speaking not quite the same as the Network Layer as specified by the OSI
 * model. However, until it becomes an issue this little "issue" is going to be
 * ignored and for now the Network Layer is equal to the Internet Layer.
 * 
 * The current version of YAJPcap is focused on IP anyway so...
 * 
 * @author jonas@jonasborjesson.com
 */
public interface IPPacket extends MACPacket {

    String getSourceIP();

    /**
     * Setting an IPv4 address the fast way! Specify each part separately. E.g.,
     * setting 192.168.0.100 would be accomplished like so:
     * 
     * {@link #setSourceIP(192, 168, 0, 100)}
     * 
     * @param a
     *            the first part of the IPv4 address, e.g. 192
     * @param b
     *            the second part of the IPv4 address, e.g. 168
     * @param c
     *            the third part of the IPv4 address, e.g. 0
     * @param d
     *            the fourth part of the IPv4 address, e.g. 100
     */
    void setSourceIP(int a, int b, int c, int d);

    /**
     * Set the source IP of this {@link IPPacket}. Note, using
     * {@link #setSourceIP(int, int, int, int)} will be must faster so try and
     * use it instead.
     * 
     * @param sourceIp
     */
    void setSourceIP(String sourceIp);

    String getDestinationIP();

    /**
     * Setting an IPv4 address the fast way! Specify each part separately. E.g.,
     * setting 192.168.0.100 would be accomplished like so:
     * 
     * {@link #setSourceIP(192, 168, 0, 100)}
     * 
     * @param a
     *            the first part of the IPv4 address, e.g. 192
     * @param b
     *            the second part of the IPv4 address, e.g. 168
     * @param c
     *            the third part of the IPv4 address, e.g. 0
     * @param d
     *            the fourth part of the IPv4 address, e.g. 100
     */
    void setDestinationIP(int a, int b, int c, int d);

    /**
     * Set the destination IP of this {@link IPPacket}. Note, using
     * {@link #setDestinationIP(int, int, int, int)} will be must faster so try
     * and use it instead.
     * 
     * @param sourceIp
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
    int getTotalLength();
}
