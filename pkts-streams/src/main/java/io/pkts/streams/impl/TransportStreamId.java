package io.pkts.streams.impl;

import io.pkts.packet.TCPPacket;
import io.pkts.protocol.Protocol;
import io.pkts.streams.StreamId;

import java.util.Objects;

public class TransportStreamId implements StreamId {

    private final String sourceAddress;
    private final String destinationAddress;
    private final int sourcePort;
    private final int destinationPort;

    private final Protocol protocol;

    public TransportStreamId(String sourceAddress, String destinationAddress, int sourcePort, int destinationPort, Protocol protocol){
        this.sourceAddress = sourceAddress;
        this.destinationAddress = destinationAddress;
        this.sourcePort = sourcePort;
        this.destinationPort = destinationPort;
        this.protocol = protocol;
    }

    public TransportStreamId(TCPPacket packet){
        this.sourceAddress = packet.getParentPacket().getSourceIP();
        this.destinationAddress = packet.getParentPacket().getDestinationIP();
        this.sourcePort = packet.getSourcePort();
        this.destinationPort = packet.getDestinationPort();
        this.protocol = packet.getProtocol(); // is always TCP
    }


    public TransportStreamId oppositeFlowDirection(){
        return new TransportStreamId(this.destinationAddress, this.sourceAddress, this.destinationPort, this.sourcePort, this.protocol);
    }

    public String getSourceAddress() {
        return sourceAddress;
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }

    public int getSourcePort() {
        return sourcePort;
    }

    public int getDestinationPort() {
        return destinationPort;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    @Override
    public int hashCode(){
        return Objects.hash(sourceAddress, destinationAddress, sourcePort, destinationPort, protocol);
    }

    @Override
    public String asString() {
        return "Source Address: "+ sourceAddress + "\n"
                + "Destination Address: " + destinationAddress + "\n"
                + "Source Port: " + sourcePort + "\n"
                + "Destination Port: " + destinationPort + "\n"
                + "Transport Layer Protocol: " + protocol + "\n";
    }

    @Override
    public boolean equals(final Object obj){
        if (obj instanceof TransportStreamId other){
            return (other.getSourceAddress().equals(this.sourceAddress)
                    && other.getDestinationAddress().equals(this.destinationAddress)
                    && other.getSourcePort() == this.sourcePort
                    && other.getDestinationPort() == this.destinationPort
                    && other.getProtocol() == this.protocol);
        } else {
            return false;
        }
    }
}
