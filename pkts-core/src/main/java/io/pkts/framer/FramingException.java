package io.pkts.framer;

import io.pkts.protocol.Protocol;

public class FramingException extends RuntimeException {
  private final Protocol protocol;

  FramingException(final String message, final Protocol protocol) {
    super(message);
    this.protocol = protocol;
  }

  public Protocol getProtocol() {
    return protocol;
  }
}
