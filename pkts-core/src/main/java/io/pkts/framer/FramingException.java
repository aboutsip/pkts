package io.pkts.framer;

import io.pkts.protocol.Protocol;

public class FramingException extends Exception {
  private final Protocol protocol;

  FramingException(String message, Protocol protocol) {
    super(message);
    this.protocol = protocol;
  }

  public Protocol getProtocol() {
    return protocol;
  }
}
