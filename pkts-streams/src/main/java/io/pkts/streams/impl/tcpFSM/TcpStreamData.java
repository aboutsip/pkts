package io.pkts.streams.impl.tcpFSM;

import io.hektor.fsm.Data;
import io.pkts.packet.TCPPacket;
import io.pkts.streams.impl.TransportStreamId;

/**
 * Necessary class for the {@link TcpStreamFSM} using Hektor.
 * Stores sequence numbers of SYN and FIN packets and their corresponding stream IDs to determine
 * if SYN packets are retransmissions, making sure the state does not change in that case,
 * and to determine if FIN packets are acked, closing their side of the connection in that case.
 *
 * @author sebastien.amelinckx@gmail.com
 */
public class TcpStreamData implements Data {
    private long syn1Seq = -1; // in most cases base sequence number
    private long syn2Seq = -1; // in case of two way SYN
    private long fin1Seq;
    private long fin2Seq;

    private TransportStreamId syn1Id;
    private TransportStreamId syn2Id;
    private TransportStreamId fin1Id;
    private TransportStreamId fin2Id;
    private boolean isFin1Terminated = false;
    private boolean isFin2Terminated = false;

    public TcpStreamData(){}

    public void setFin1Seq(TCPPacket packet) {
        this.fin1Seq = packet.getSequenceNumber();
        this.fin1Id = new TransportStreamId(packet);
    }

    public void setFin2Seq(TCPPacket packet) {
        this.fin2Seq = packet.getSequenceNumber();
        this.fin2Id = new TransportStreamId(packet);
    }

    public void setSyn1Seq(TCPPacket packet){
        this.syn1Seq = packet.getSequenceNumber();
        this.syn1Id = new TransportStreamId(packet);
    }

    public void setSyn2Seq(TCPPacket packet){
        this.syn2Seq = packet.getSequenceNumber();
        this.syn2Id = new TransportStreamId(packet);
    }

    public long getSyn1Seq() {
        return syn1Seq;
    }

    public long getSyn2Seq() {
        return syn2Seq;
    }

    public TransportStreamId getSyn1Id() {
        return syn1Id;
    }

    public TransportStreamId getSyn2Id() {
        return syn2Id;
    }

    public long getFin1Seq() {
        return fin1Seq;
    }

    public long getFin2Seq() {
        return fin2Seq;
    }

    public TransportStreamId getFin1Id() {
        return fin1Id;
    }

    public TransportStreamId getFin2Id() {
        return fin2Id;
    }

    public boolean isFin1Terminated() {
        return isFin1Terminated;
    }

    public boolean isFin2Terminated() {
        return isFin2Terminated;
    }

    public void terminateFin1() {
        this.isFin1Terminated = true;
    }

    public void terminateFin2() {
        this.isFin2Terminated = true;
    }
}
