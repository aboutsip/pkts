package io.pkts.packet.sctp;

import io.pkts.buffer.Buffer;
import io.pkts.packet.sctp.impl.DefaultSctpChunk;
import io.pkts.packet.sctp.impl.SctpDataChunkImpl;

import static io.pkts.packet.sctp.SctpChunk.Type.DATA;

public interface SctpChunk {

    static SctpChunk frame(final Buffer buffer) {
        final DefaultSctpChunk chunk = DefaultSctpChunk.frame(buffer);
        switch (chunk.getType()) {
            case DATA:
                return SctpDataChunkImpl.of(chunk);
            default:
                return chunk;
        }
    }


    Type getType();

    /**
     * <p>
     * This value represents the size of the chunk in bytes, including
     * the Chunk Type, Chunk Flags, Chunk Length, and Chunk Value fields.
     * Therefore, if the Chunk Value field is zero-length, the Length
     * field will be set to 4.  The Chunk Length field does not count any
     * chunk padding.
     * <p>
     * Chunks (including Type, Length, and Value fields) are padded out
     * by the sender with all zero bytes to be a multiple of 4 bytes
     * long.  This padding MUST NOT be more than 3 bytes in total.  The
     * Chunk Length value does not include terminating padding of the
     * chunk.  However, it does include padding of any variable-length
     * parameter except the last parameter in the chunk.  The receiver
     * MUST ignore the padding.
     * <p>
     * Note: A robust implementation should accept the chunk whether or
     * not the final padding has been included in the Chunk Length.
     * <p>
     * (source: RFC 4960 Section 3.2)
     *
     * @return
     */
    int getLength();

    /**
     * Get the raw header of this SCTP Chunk
     *
     */
    Buffer getHeader();

    /**
     * Get the Chunk Value excluding any potential padding that may have
     * been included on the wire.
     *
     * @return
     */
    Buffer getValue();

    /**
     * @return
     */
    int getPadding();

    byte getFlags();

    /**
     * The length of the value of the chunk.
     * <p>
     * Note that the length as encoded into the Chunk "header" includes the 4 bytes containing the size of
     * the fields making up the header. This length is JUST the length of the actual value. Also, it does NOT
     * include padding (if any).
     */
    int getValueLength();

    /**
     * RFC 4960 section 3.2 Chunk Field Descriptions and some additional copy/pasted from Wikipedia
     */
    enum Type {
        DATA((short) 0, "Payload data"),
        INIT((short) 1, "Initiation"),
        INIT_ACK((short) 2, "Initiation Acknowledgement"),
        SACK((short) 3, "Selective Acknowledgement"),
        HEARTBEAT((short) 4, "Heartbeat Request"),
        HEARTBEAT_ACK((short) 5, "Heartbeat Acknowledgement"),
        ABORT((short) 6, "Abort"),
        SHUTDOWN((short) 7, "Shutdown"),
        SHUTDOWN_ACK((short) 8, "Shutdown Acknowledgement"),
        ERROR((short) 9, "Operation Error"),
        COOKIE_ECHO((short) 10, "State Cookie"),
        COOKIE_ACK((short) 11, "State Acknowledgement"),
        ECNE((short) 12, "Explicit Congestion Notification Echo (reserved)"),
        CWR((short) 13, "Congestion Window Reduced  (reserved)"),
        SHUTDOWN_COMPLETE((short) 14, "Shutdown Complete"),
        AUTH((short) 15, "Authentication"),
        I_DATA((short) 64, "Payload data supporting packet interleaving"),
        ASCONF_ACK((short) 128, "Address configuration change acknowledgement"),
        RE_CONFIG((short) 130, "Stream reconfiguration"),
        PAD((short) 132, "Packet Padding"),
        FORWARD_TSN((short) 192, "Increment expected TSN"),
        ASCONF((short) 193, "Address configuration change"),
        I_FORWARD_TSN((short) 194, "Increment expected TSN, supporting packet interleaving");

        private final short type;
        private final String description;

        Type(final short type, final String description) {
            this.type = type;
            this.description = description;
        }

        public short getType() {
            return type;
        }

        public String getDescription() {
            return description;
        }

    }

    static Type lookup(final short type) {
        switch (type) {
            case 0:
                return DATA;
            case 1:
                return Type.INIT;
            case 2:
                return Type.INIT_ACK;
            case 3:
                return Type.SACK;
            case 4:
                return Type.HEARTBEAT;
            case 5:
                return Type.HEARTBEAT_ACK;
            case 6:
                return Type.ABORT;
            case 7:
                return Type.SHUTDOWN;
            case 8:
                return Type.SHUTDOWN_ACK;
            case 9:
                return Type.ERROR;
            case 10:
                return Type.COOKIE_ECHO;
            case 11:
                return Type.COOKIE_ACK;
            case 12:
                return Type.ECNE;
            case 13:
                return Type.CWR;
            case 14:
                return Type.SHUTDOWN_COMPLETE;
            case 15:
                return Type.AUTH;
            case 64:
                return Type.I_DATA;
            case 128:
                return Type.ASCONF_ACK;
            case 130:
                return Type.RE_CONFIG;
            case 132:
                return Type.PAD;
            case 192:
                return Type.FORWARD_TSN;
            case 193:
                return Type.ASCONF;
            case 194:
                return Type.I_FORWARD_TSN;
            default:
                return null;
        }
    }

}
