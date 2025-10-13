package io.pkts.framer;

import io.pkts.protocol.Protocol;

/**
* A custom runtime exception specifically designed for handling protocol framing errors in the Pkts networking library. 
* The FramingException captures both an error message and the associated Protocol, providing detailed context for network protocol framing issues. 
* It extends RuntimeException and includes a constructor that takes a message and protocol, with a getter method 
* to retrieve the specific protocol where the framing error occurred. This exception enhances error handling and 
* debugging by allowing developers to precisely identify and diagnose protocol-level framing problems during network packet processing.
*/

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
