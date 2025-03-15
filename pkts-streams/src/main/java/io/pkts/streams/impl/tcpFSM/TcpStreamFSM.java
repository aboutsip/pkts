package io.pkts.streams.impl.tcpFSM;

import io.hektor.fsm.Definition;
import io.hektor.fsm.FSM;
import io.hektor.fsm.builder.FSMBuilder;
import io.hektor.fsm.builder.StateBuilder;
import io.pkts.packet.TCPPacket;
import io.pkts.streams.impl.TransportStreamId;

import static io.pkts.streams.impl.tcpFSM.TcpStreamFSM.TcpState.*;

/**
 * FSM using {@link io.hektor.fsm.FSM} to model the TCP connection. This class handles the state transitions for an
 * ongoing TCP connection. The states are defined in {@link TcpState}. This class uses {@link TcpStreamData} to store
 * the necessary data for the FSM to work. It has no use for the {@link TcpStreamContext} class, but it is there to
 * make the library work.
 *
 * @author sebastien.amelinckx@gmail.com
 */
public class TcpStreamFSM{
    public final static Definition<TcpState, TcpStreamContext, TcpStreamData> definition;
    public enum TcpState{
        INIT, HANDSHAKE, ESTABLISHED, FIN_WAIT_1, FIN_WAIT_2,
        CLOSING_1_CLOSING_2, CLOSED_1_CLOSING_2, CLOSING_1_CLOSED_2, CLOSED, CLOSED_PORTS_REUSED
    }

    static {
        final FSMBuilder<TcpState, TcpStreamContext, TcpStreamData> builder=
                FSM.of(TcpState.class).ofContextType(TcpStreamContext.class).withDataType(TcpStreamData.class);


        // define all states for the builder
        final StateBuilder<TcpState, TcpStreamContext, TcpStreamData> init = builder.withInitialState(INIT);
        final StateBuilder<TcpState, TcpStreamContext, TcpStreamData> handshake = builder.withState(HANDSHAKE);
        final StateBuilder<TcpState, TcpStreamContext, TcpStreamData> established = builder.withState(ESTABLISHED);
        final StateBuilder<TcpState, TcpStreamContext, TcpStreamData> finWait1 = builder.withState(FIN_WAIT_1);
        final StateBuilder<TcpState, TcpStreamContext, TcpStreamData> finWait2 = builder.withState(FIN_WAIT_2);
        final StateBuilder<TcpState, TcpStreamContext, TcpStreamData> closing1Closing2 = builder.withState(CLOSING_1_CLOSING_2);
        final StateBuilder<TcpState, TcpStreamContext, TcpStreamData> closed1Closing2 = builder.withState(CLOSED_1_CLOSING_2);
        final StateBuilder<TcpState, TcpStreamContext, TcpStreamData> closing1Closed2 = builder.withState(CLOSING_1_CLOSED_2);
        final StateBuilder<TcpState, TcpStreamContext, TcpStreamData> closed = builder.withState(CLOSED);
        final StateBuilder<TcpState, TcpStreamContext, TcpStreamData> closedPortsReused = builder.withFinalState(CLOSED_PORTS_REUSED);

        // define all transitions
        init.transitionTo(HANDSHAKE).onEvent(TCPPacket.class).withGuard(TcpStreamFSM::isSynPacket).withAction(TcpStreamFSM::setSyn1);
        init.transitionTo(FIN_WAIT_1).onEvent(TCPPacket.class).withGuard(TcpStreamFSM::isFinPacket).withAction(TcpStreamFSM::setFin1);;
        init.transitionTo(CLOSED).onEvent(TCPPacket.class).withGuard(TcpStreamFSM::isRstPacket);
        init.transitionTo(ESTABLISHED).onEvent(TCPPacket.class);

        handshake.transitionTo(CLOSED).onEvent(TCPPacket.class).withGuard(TcpStreamFSM::isRstPacket);
        handshake.transitionToSelf().onEvent(TCPPacket.class).withGuard(TcpStreamFSM::isSynPacket).withAction(TcpStreamFSM::setSyn2);
        handshake.transitionTo(FIN_WAIT_1).onEvent(TCPPacket.class).withGuard(TcpStreamFSM::isFinPacket).withAction(TcpStreamFSM::setFin1);
        handshake.transitionTo(ESTABLISHED).onEvent(TCPPacket.class);

        established.transitionTo(CLOSED).onEvent(TCPPacket.class).withGuard(TcpStreamFSM::isRstPacket);
        established.transitionTo(CLOSED_PORTS_REUSED).onEvent(TCPPacket.class).withGuard(TcpStreamFSM::isNewSynPacket); // skipped the end of stream, New stream noticed
        established.transitionTo(FIN_WAIT_1).onEvent(TCPPacket.class).withGuard(TcpStreamFSM::isFinPacket).withAction(TcpStreamFSM::setFin1);
        established.transitionToSelf().onEvent(TCPPacket.class);

        finWait1.transitionTo(CLOSED).onEvent(TCPPacket.class).withGuard(TcpStreamFSM::isRstPacket);
        finWait1.transitionTo(CLOSED_PORTS_REUSED).onEvent(TCPPacket.class).withGuard(TcpStreamFSM::isNewSynPacket);
        finWait1.transitionTo(CLOSED_1_CLOSING_2).onEvent(TCPPacket.class).withGuard(TcpStreamFSM::ackOfFin1AndFin2).withAction(TcpStreamFSM::closeFin1SetFin2); // special case FIN + ACKOfFin1 packet
        finWait1.transitionTo(FIN_WAIT_2).onEvent(TCPPacket.class).withGuard(TcpStreamFSM::isAckOfFin1).withAction(TcpStreamFSM::closeFin1); // if first fin has been acked
        finWait1.transitionTo(CLOSING_1_CLOSING_2).onEvent(TCPPacket.class).withGuard(TcpStreamFSM::isSecondFinPacket).withAction(TcpStreamFSM::setFin2);
        finWait1.transitionToSelf().onEvent(TCPPacket.class);

        finWait2.transitionTo(CLOSED).onEvent(TCPPacket.class).withGuard(TcpStreamFSM::isRstPacket);
        finWait2.transitionTo(CLOSED_PORTS_REUSED).onEvent(TCPPacket.class).withGuard(TcpStreamFSM::isNewSynPacket);
        finWait2.transitionTo(CLOSED_1_CLOSING_2).onEvent(TCPPacket.class).withGuard(TcpStreamFSM::isSecondFinPacket).withAction(TcpStreamFSM::setFin2); // 2nd fin observed
        finWait2.transitionToSelf().onEvent(TCPPacket.class);

        closing1Closing2.transitionTo(CLOSED).onEvent(TCPPacket.class).withGuard(TcpStreamFSM::isRstPacket);
        closing1Closing2.transitionTo(CLOSED_PORTS_REUSED).onEvent(TCPPacket.class).withGuard(TcpStreamFSM::isNewSynPacket);
        closing1Closing2.transitionTo(CLOSED_1_CLOSING_2).onEvent(TCPPacket.class).withGuard(TcpStreamFSM::isAckOfFin1).withAction(TcpStreamFSM::closeFin1);
        closing1Closing2.transitionTo(CLOSING_1_CLOSED_2).onEvent(TCPPacket.class).withGuard(TcpStreamFSM::isAckOfFin2).withAction(TcpStreamFSM::closeFin2);
        closing1Closing2.transitionToSelf().onEvent(TCPPacket.class);

        closed1Closing2.transitionTo(CLOSED).onEvent(TCPPacket.class).withGuard(TcpStreamFSM::isRstPacket);
        closed1Closing2.transitionTo(CLOSED_PORTS_REUSED).onEvent(TCPPacket.class).withGuard(TcpStreamFSM::isNewSynPacket);
        closed1Closing2.transitionTo(CLOSED).onEvent(TCPPacket.class).withGuard(TcpStreamFSM::isAckOfFin2).withAction(TcpStreamFSM::closeFin2);
        closed1Closing2.transitionToSelf().onEvent(TCPPacket.class);

        closing1Closed2.transitionTo(CLOSED).onEvent(TCPPacket.class).withGuard(TcpStreamFSM::isRstPacket);
        closing1Closed2.transitionTo(CLOSED_PORTS_REUSED).onEvent(TCPPacket.class).withGuard(TcpStreamFSM::isNewSynPacket);
        closing1Closed2.transitionTo(CLOSED).onEvent(TCPPacket.class).withGuard(TcpStreamFSM::isAckOfFin1).withAction(TcpStreamFSM::closeFin1);
        closing1Closed2.transitionToSelf().onEvent(TCPPacket.class);

        /*
         * When a stream is in a closed state (gracefully or abruptly), it can still receive packets such
         * as retransmissions, keep-alive, new RST packets, data, etc. The only case were we should never add a
         * new packet to the stream is when a new SYN packet is received, which means a new stream is starting.
         * Explaining why here, the terminal state for the FSM is when we observe that ports are reused.
         */
        closed.transitionTo(CLOSED_PORTS_REUSED).onEvent(TCPPacket.class).withGuard(TcpStreamFSM::isNewSynPacket);
        closed.transitionToSelf().onEvent(TCPPacket.class);

        definition = builder.build();
    }

    private static boolean isSynPacket(TCPPacket packet){
        return packet.isSYN();
    }

    private static boolean isFinPacket(TCPPacket packet){
        return packet.isFIN();
    }

    private static boolean isSecondFinPacket(TCPPacket packet, TcpStreamContext ctx, TcpStreamData data){ // check if FIN segment comes from the second party
        return packet.isFIN() && (!data.getFin1Id().equals(new TransportStreamId(packet))); // is it a FIN segment AND not coming from the same direction as FIN 1
    }

    private static boolean isRstPacket(TCPPacket packet){
        return packet.isRST();
    }

    private static void setFin1(TCPPacket packet, TcpStreamContext ctx, TcpStreamData data){
        data.setFin1Seq(packet);
    }

    private static void setFin2(TCPPacket packet, TcpStreamContext ctx, TcpStreamData data){
        data.setFin2Seq(packet);
    }

    private static void closeFin1(TCPPacket packet, TcpStreamContext ctx, TcpStreamData data){
        data.terminateFin1();
    }

    private static void closeFin2(TCPPacket packet, TcpStreamContext ctx, TcpStreamData data){
        data.terminateFin2();
    }

    private static boolean isAckOfFin1(TCPPacket packet, TcpStreamContext ctx, TcpStreamData data){
        return packet.isACK() && !data.isFin1Terminated() &&
                data.getFin1Id().equals(new TransportStreamId(packet).remoteTransportStreamId()) &&
                data.getFin1Seq() < packet.getAcknowledgementNumber();
    }

    private static boolean isAckOfFin2(TCPPacket packet, TcpStreamContext ctx, TcpStreamData data){
        return packet.isACK() && !data.isFin2Terminated() &&
                data.getFin2Id().equals(new TransportStreamId(packet).remoteTransportStreamId()) &&
                data.getFin2Seq() < packet.getAcknowledgementNumber();
    }

    private static boolean ackOfFin1AndFin2(TCPPacket packet, TcpStreamContext ctx, TcpStreamData data){
        return isAckOfFin1(packet, ctx, data) && isSecondFinPacket(packet, ctx, data);
    }

    private static void closeFin1SetFin2(TCPPacket packet, TcpStreamContext ctx, TcpStreamData data){
        closeFin1(packet, ctx, data);
        setFin2(packet, ctx, data);
    }

    private static boolean isNewSynPacket(TCPPacket packet, TcpStreamContext ctx, TcpStreamData data){
        return isSynPacket(packet) && !isSynDuplicate(packet, ctx, data);
    }

    // special case where a SYN packet is a retransmitted packet, which means we need to ignore it
    // instead of closing the stream
    private static boolean isSynDuplicate(TCPPacket packet, TcpStreamContext ctx, TcpStreamData data){
        long seqSynNum = packet.getSequenceNumber();
        TransportStreamId packetStreamId = new TransportStreamId(packet);

        return (seqSynNum == data.getSyn1Seq() && packetStreamId.equals(data.getSyn1Id()))
                || (seqSynNum == data.getSyn2Seq() && packetStreamId.equals(data.getSyn2Id()));
    }

    private static void setSyn1(TCPPacket packet, TcpStreamContext ctx, TcpStreamData data){
        data.setSyn1Seq(packet);
    }

    private static void setSyn2(TCPPacket packet, TcpStreamContext ctx, TcpStreamData data){
        if (!isSynDuplicate(packet, ctx, data)){
            data.setSyn2Seq(packet);
        }
    }
}
