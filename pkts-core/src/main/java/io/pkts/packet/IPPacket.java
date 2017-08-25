package io.pkts.packet;

public interface IPPacket extends Packet, Cloneable {
  /**
   * Get the raw source ip.
   *
   * Note, these are the raw bits and should be treated as such. If you really want to print it,
   * then you should treat it as unsigned
   *
   * @return
   */
  byte[] getRawSourceIp();

  /**
   * Convenience method for returning the source IP in a more human readable form.
   *
   * @return
   */
  String getSourceIP();

  /**
   * Get the raw destination ip.
   *
   * Note, these are the raw bits and should be treated as such. If you really want to print it,
   * then you should treat it as unsigned
   *
   * @return
   */
  byte[] getRawDestinationIp();

  /**
   * Convenience method for returning the destination IP in a more human readable form.
   *
   * @return
   */
  String getDestinationIP();


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
   * (source: http://en.wikipedia.org/wiki/IPv4)
   *
   * @return
   */
  int getTotalIPLength();


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
}
