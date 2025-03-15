package io.pkts.streams;

import io.pkts.packet.TCPPacket;
import io.pkts.streams.impl.tcpFSM.TcpStreamFSM.TcpState;

/**
 * An {@link TcpStream} represents a stream of {@link TCPPacket}s belonging to the same connection.
 * A TCP conversation is identified it's 5-tuple (src-ip, src-port, dest-ip, dest-port, protocol=TCP), and
 * should follow a valid state progression for the lifetime of the connection (see RFC 793). This means that for
 * the same 5-tuple, if an event occurs that indicates the connection is no longer valid (such as a new SYN exchange
 * or data exchange when the connection should be closed), the stream should be ended and a new stream should be
 * potentially created for the new connection.
 *
 * @author sebastien.amelinckx@gmail.com
 */
public interface TcpStream extends Stream<TCPPacket> {

    String getSrcAddr();

    String getDestAddr();

    int getSrcPort();

    int getDestPort();

    void addPacket(TCPPacket packet);

    TcpState getState();

    long getUuid();

    boolean isEnded();
}
