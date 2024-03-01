package io.pkts.streams.impl.tcpFSM;

import io.hektor.fsm.Data;
import io.pkts.packet.TCPPacket;
import io.pkts.streams.impl.TransportStreamId;

public class TcpStreamData implements Data {
    private long FIN_1_seq;
    private TransportStreamId FIN_1_id;
    private boolean is_FIN_1_terminated;
    private long FIN_2_seq;
    private TransportStreamId FIN_2_id;
    private boolean is_FIN_2_terminated;

    public TcpStreamData(){
        this.is_FIN_1_terminated = false;
        this.is_FIN_2_terminated = false;
    }

    public void setFIN_1_seq(TCPPacket packet) {
        this.FIN_1_seq = packet.getSequenceNumber();
        this.FIN_1_id = new TransportStreamId(packet);
    }

    public void setFIN_2_seq(TCPPacket packet) {
        this.FIN_2_seq = packet.getSequenceNumber();
        this.FIN_2_id = new TransportStreamId(packet);
    }

    public long getFIN_1_seq() {
        return FIN_1_seq;
    }

    public long getFIN_2_seq() {
        return FIN_2_seq;
    }

    public TransportStreamId getFIN_1_id() {
        return FIN_1_id;
    }

    public TransportStreamId getFIN_2_id() {
        return FIN_2_id;
    }

    public boolean isFIN_1_Terminated() {
        return is_FIN_1_terminated;
    }

    public boolean isFIN_2_Terminated() {
        return is_FIN_2_terminated;
    }

    public void terminateFIN_1() {
        this.is_FIN_1_terminated = true;
    }

    public void terminateFIN_2() {
        this.is_FIN_2_terminated = true;
    }
}
