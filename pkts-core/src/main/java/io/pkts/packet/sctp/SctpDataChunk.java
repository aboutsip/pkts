package io.pkts.packet.sctp;

import io.pkts.buffer.Buffer;

public interface SctpDataChunk extends SctpChunk {

    /**
     * Flag: U bit
     *
     *
     * <p>
     * The (U)nordered bit, if set to '1', indicates that this is an
     * unordered DATA chunk, and there is no Stream Sequence Number
     * assigned to this DATA chunk.  Therefore, the receiver MUST ignore
     * the Stream Sequence Number field.
     * <p>
     * After reassembly (if necessary), unordered DATA chunks MUST be
     * dispatched to the upper layer by the receiver without any attempt
     * to reorder.
     * <p>
     * If an unordered user message is fragmented, each fragment of the
     * message MUST have its U bit set to '1'.
     * <p>
     * (source: RFC 4960 Section 3.3.1)
     *
     * @return
     */
    boolean isUnordered();

    /**
     * Flag: B bit
     * <p>
     * (source: RFC 4960 Section 3.3.1)
     *
     * @return
     */
    boolean isBeginningFragment();

    /**
     * Flag: E bit
     *
     * <p>
     * The (E)nding fragment bit, if set, indicates the last fragment of
     * a user message.
     * <p>
     * An unfragmented user message shall have both the B and E bits set to
     * '1'.  Setting both B and E bits to '0' indicates a middle fragment of
     * a multi-fragment user message, as summarized in the following table:
     *
     * <pre>
     *     B E                  Description
     * ============================================================
     * |  1 0 | First piece of a fragmented user message          |
     * +----------------------------------------------------------+
     * |  0 0 | Middle piece of a fragmented user message         |
     * +----------------------------------------------------------+
     * |  0 1 | Last piece of a fragmented user message           |
     * +----------------------------------------------------------+
     * |  1 1 | Unfragmented message                              |
     * ============================================================
     * |             Table 1: Fragment Description Flags          |
     * ============================================================
     * </pre>
     * <p>
     * When a user message is fragmented into multiple chunks, the TSNs are
     * used by the receiver to reassemble the message.  This means that the
     * TSNs for each fragment of a fragmented user message MUST be strictly
     * sequential.
     * (source: RFC 4960 Section 3.3.1)
     *
     * @return
     */
    boolean isEndingFragment();

    /**
     * Flag: I bit
     * <p>
     * By setting this bit, the sender of
     * a DATA chunk indicates that the corresponding SACK chunk should not
     * be delayed.
     * <p>
     * (source: RFC 7053 Section 3)
     */
    boolean isImmediate();

    /**
     * This value represents the TSN for this DATA chunk.  The valid
     * range of TSN is from 0 to 4294967295 (2**32 - 1).  TSN wraps back
     * to 0 after reaching 4294967295.
     * <p>
     * (source: RFC 4960 Section 3.3.1)
     */
    long getTransmissionSequenceNumber();


    /**
     * Identifies the stream to which the following user data belongs.
     * <p>
     * (source: RFC 4960 Section 3.3.1)
     */
    int getStreamIdentifier();

    /**
     * This value represents the Stream Sequence Number of the following
     * user data within the stream S.  Valid range is 0 to 65535.
     * <p>
     * When a user message is fragmented by SCTP for transport, the same
     * Stream Sequence Number MUST be carried in each of the fragments of
     * the message.
     * <p>
     * (source: RFC 4960 Section 3.3.1)
     */
    int getStreamSequenceNumber();

    /**
     * This value represents an application (or upper layer) specified
     * protocol identifier.  This value is passed to SCTP by its upper
     * layer and sent to its peer.  This identifier is not used by SCTP
     * but can be used by certain network entities, as well as by the
     * peer application, to identify the type of information being
     * carried in this DATA chunk.  This field must be sent even in
     * fragmented DATA chunks (to make sure it is available for agents in
     * the middle of the network).  Note that this field is NOT touched
     * by an SCTP implementation; therefore, its byte order is NOT
     * necessarily big endian.  The upper layer is responsible for any
     * byte order conversions to this field.
     * <p>
     * The value 0 indicates that no application identifier is specified
     * by the upper layer for this payload data.
     * <p>
     * (source: RFC 4960 Section 3.3.1)
     */
    long getPayloadProtocolIdentifier();

    /**
     * The actual payload of the data chunk. The returned buffer will not contain
     * any potential padding.
     */
    Buffer getUserData();

    @Override
    default Type getType() {
        return Type.DATA;
    }

}
