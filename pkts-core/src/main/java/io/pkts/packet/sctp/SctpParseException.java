package io.pkts.packet.sctp;

import io.pkts.packet.PacketParseException;

public class SctpParseException extends PacketParseException {

    public SctpParseException(final int errorOffset, final String message) {
        super(errorOffset, message);
    }
}
