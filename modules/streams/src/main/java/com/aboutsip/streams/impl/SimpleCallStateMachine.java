/**
 * 
 */
package com.aboutsip.streams.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aboutsip.streams.SipStream.CallState;
import com.aboutsip.yajpcap.packet.sip.SipMessage;
import com.aboutsip.yajpcap.packet.sip.SipResponse;
import com.aboutsip.yajpcap.packet.sip.impl.SipParseException;

/**
 * A "state machine" for SIP but really mainly for SIP VoIP calls. The reason
 * for the quotes around state machine is that it is really not a true state
 * machine for various reasons, one of which is that it allows events to be
 * re-ordered since if multiple pcaps are merged you may actually push a pcap
 * that was captured later first and as such we need to be able to handle this
 * use case.
 * 
 * @author jonas@jonasborjesson.com
 */
public final class SimpleCallStateMachine {

    private static final Logger logger = LoggerFactory.getLogger(SimpleCallStateMachine.class);

    /**
     * 
     */
    private NavigableSet<SipMessage> messages;

    /**
     * A list of all our transitions.
     */
    private List<CallState> callTransitions;

    /**
     * Should always be the same as the last element in the
     * {@link #callTransitions} list but just for easy access...
     */
    private CallState currentState;

    private final String callId;

    public SimpleCallStateMachine(final String callId) {
        this.callId = callId;
        init();
    }

    /**
     * Start over from a clean slate...
     */
    private void init() {
        this.currentState = CallState.START;
        this.callTransitions = new ArrayList<CallState>();
        this.messages = new TreeSet<SipMessage>(new SipMessageComparator());
    }

    /**
     * At some point we may want to have a generic event but for now this will
     * only be {@link SipMessage}s.
     * 
     * Note, if the {@link SipMessage} arrived earlier than the oldest element
     * that this {@link SimpleCallStateMachine} has seen before then this new
     * {@link SipMessage} will be inserted in the time sequence and then all the
     * events will be "re-played".
     * 
     * @param msg
     */
    public void onEvent(final SipMessage msg) throws SipParseException {
        // logger.debug("OnEvent: \n{}", msg);
        if (msg == null) {
            return;
        }

        final SipMessage previousMsg = this.messages.isEmpty() ? null : this.messages.last();
        this.messages.add(msg);
        if ((previousMsg != null) && (msg.getArrivalTime() < previousMsg.getArrivalTime())) {
            redrive();
            return;
        }

        try {
            handleStateChange(msg);
        } catch (final SipParseException e) {
            e.printStackTrace();
        }
    }

    private void handleStateChange(final SipMessage msg) throws SipParseException {
        switch (this.currentState) {
        case START:
            initializeState(msg);
            break;
        case INITIAL:
        case TRYING:
        case RINGING:
            handleInProvisionalState(msg);
            break;
        case IN_CALL:
            handleInConfirmedState(msg);
            break;
        case REJECTED:
            break;
        case COMPLETED:
            handleInCompletedState(msg);
            break;
        case FAILED:
            break;
        case UNKNOWN:
            break;
        default:
            throw new RuntimeException("Unknown state, should be impossible. State is: " + this.currentState);
        }
    }

    /**
     * Handle state transitions for when we are already in the completed state.
     * 
     * @param msg
     * @throws SipParseException
     */
    private void handleInCompletedState(final SipMessage msg) throws SipParseException {
        if (msg.isRequest()) {
            // TODO:
        } else {
            if (msg.isBye()) {
                transition(CallState.COMPLETED, msg);
            }
        }
    }

    /**
     * We will only get to the confirmed state on a 2xx response to the INVITE.
     * From here, we can stay in the confirmed state if we get an ACK or
     * transition over to completed if we get a BYE request.
     * 
     * @param msg
     * @throws SipParseException
     */
    private void handleInConfirmedState(final SipMessage msg) throws SipParseException {
        if (msg.isRequest()) {
            if (msg.isBye()) {
                transition(CallState.COMPLETED, msg);
            } else if (msg.isAck()) {
                transition(CallState.IN_CALL, msg);
            }
        } else {
            final SipResponse response = (SipResponse) msg;
            if (response.isBye()) {
                // already in completed (or should be)
            }
        }
    }

    /**
     * From the initial state we accept the following state changes:
     * 
     * TODO: check what wireshark does for 403, 404, 400 etc etc.
     * <ul>
     * <li>100 -> TRYING</li>
     * <li>180 -> RINGING</li>
     * <li>183 -> RINGING</li>
     * <li>2xx -> IN_CALL</li>
     * <li>3xx -> REDIRECT</li>
     * <li>A bunch of 4xx responses -> REJECTED</li>
     * <li>If not rejected 4xx response, then -> FAILED</li>
     * <li>5xx -> FAILED</li>
     * <li>6xx -> FAILED</li>
     * <li></li>
     * <li></li>
     * <li></li>
     * <li></li>
     * </ul>
     * 
     * @param msg
     */
    private void handleInProvisionalState(final SipMessage msg) {
        if (msg.isRequest()) {
            // illegal. Need to throw something
            return;
        }

        final SipResponse response = (SipResponse) msg;
        if (response.is100Trying()) {
            transition(CallState.TRYING, msg);
        } else if (response.isRinging()) {
            transition(CallState.RINGING, msg);
        } else if (response.isSuccess()) {
            transition(CallState.IN_CALL, msg);
        } else if (response.isRedirect()) {
            transition(CallState.REDIRECT, msg);
        } else if (isRejected(response.getStatus())) {
            transition(CallState.REJECTED, msg);
        } else if (response.isClientError()) {
            transition(CallState.FAILED, msg);
        } else if (response.isServerError()) {
            transition(CallState.FAILED, msg);
        } else if (response.isGlobalError()) {
            transition(CallState.FAILED, msg);
        }
    }

    /**
     * All the response codes that yield a rejected state.
     * 
     * Perhaps this should be narrowed down a little more? Perhaps only errors
     * that the user triggered should lead to a rejected state? E.g., 486 is
     * typical when the user is either busy or when they pressed the
     * "ignore call" button. However, a 404 or 480 is not quite the same
     * thing...
     * 
     * @param status
     * @return
     */
    private boolean isRejected(final int status) {
        return (status == 401) || (status == 403) || (status == 404) || (status == 407)
                || (status == 480) || (status == 486);
    }

    /**
     * Since traffic can come in any order due to the fact that we can merge
     * multiple pcaps etc we may miss the initial traffic (perhaps we didn't
     * even captured it) so when initializing the state we can really jump into
     * the state machine anywhere. Hence, this method tries to figure out where
     * in the state machine we need to "jump" into assuming that we would have
     * received the traffic that has gone missing.
     * 
     * 
     * @param msg
     * @throws SipParseException
     */
    private void initializeState(final SipMessage msg) throws SipParseException {
        if (msg.isRequest()) {
            if (msg.isInvite() && msg.isInitial()) {
                transition(CallState.INITIAL, msg);
            } else if (msg.isAck()) {
                // TODO: need to figure out whether this is an ACK to a 200 or
                // a error response
                // since we would transition differently
                transition(CallState.IN_CALL, msg);
            } else if (msg.isBye() && !msg.isInitial()) {
                transition(CallState.COMPLETED, msg);
            }

        } else {
            final SipResponse response = (SipResponse) msg;
            if (response.isInvite()) {
                if (response.is100Trying()) {
                    transition(CallState.TRYING, msg);
                } else if (response.isRinging()) {
                    transition(CallState.RINGING, msg);
                } else if (response.isSuccess()) {
                    transition(CallState.IN_CALL, msg);
                }
            } else if (response.isBye() && !response.isInitial()) {
                transition(CallState.COMPLETED, msg);
            }
        }
    }

    /**
     * Helper method for doing transitions.
     * 
     * @param nextState
     * @param msg
     */
    private void transition(final CallState nextState, final SipMessage msg) {
        final CallState previousState = this.currentState;
        this.currentState = nextState;
        if (previousState != nextState) {
            // don't add the same transition twice
            this.callTransitions.add(nextState);
        }
        logger.info("[{}] {} -> {} Event: {}", this.callId, previousState, this.currentState, msg.getInitialLine());
    }


    /**
     * When a messages (or event in general) "arrives" to this state machine but
     * this message's arrival time is actually before our last seen message then
     * we will have to re-drive everything. Perhaps we could have been a little
     * smarter but this just seems easier, safer and less error prone since it
     * would not require any special logic to figure out what state changes we
     * would have gone through etc.
     */
    private void redrive() throws SipParseException {
        logger.info("Out-of-sequence event detected. Redriving all traffic.");
        final NavigableSet<SipMessage> oldMessages = this.messages;
        init();
        while (!oldMessages.isEmpty()) {
            final SipMessage msg = oldMessages.pollFirst();
            onEvent(msg);
        }
    }

    /**
     * Simple comparator of {@link SipMessage}s that is just comparing time
     * stamps.
     */
    private static class SipMessageComparator implements Comparator<SipMessage>, Serializable {

        /**
         * Because it is serializable. And the reason it is is because if you
         * put elements in a treemap or whatever then it cannot serialize itself
         * unless the comparator is also serializable...
         */
        private static final long serialVersionUID = 1L;

        @Override
        public int compare(final SipMessage o1, final SipMessage o2) {
            final long t1 = o1.getArrivalTime();
            final long t2 = o2.getArrivalTime();
            if (t1 == t2) {
                return 0;
            }
            if (t1 < t2) {
                return -1;
            }

            return 1;
        }

    }

    /**
     * Get a list of all the transitions this state machine took.
     * 
     * @return
     */
    public List<CallState> getTransitions() {
        return Collections.unmodifiableList(this.callTransitions);
    }

    /**
     * The current state of this state machine, same as getting the last element
     * in {@link #getTransitions()}
     * 
     * @return
     */
    public CallState getCallState() {
        return this.currentState;
    }

}
