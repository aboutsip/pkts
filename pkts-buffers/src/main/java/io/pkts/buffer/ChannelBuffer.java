package io.pkts.buffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

/**
 * Streaming Buffer implementation wrapping a {@link java.nio.channels.Channel} by reading it into a
 * {@link java.nio.ByteBuffer} ring buffer.
 *
 * @author eric@wolak.net
 */
public class ChannelBuffer extends StreamingBuffer {

  private final ReadableByteChannel channel;
  private final ByteBuffer buffer;

  public ChannelBuffer(final ReadableByteChannel in) { this(DEFAULT_CAPACITY, in); }

  public ChannelBuffer(final int bufferCapacity, ReadableByteChannel in) {
    super(bufferCapacity);
    this.channel = in;
    this.buffer = ByteBuffer.allocate(bufferCapacity);
  }

  @Override
  public String toString() {
    return "ChannelBuffer{" +
            "channel=" + channel +
            ", buffer=" + buffer +
            ", localCapacity=" + localCapacity +
            ", readerIndex=" + readerIndex +
            ", writerIndex=" + writerIndex +
            '}';
  }

  @Override
  protected int actuallyReadFromStream(int offset, int length) throws IOException {
    this.buffer.position(offset);
    ByteBuffer readBuffer = this.buffer.slice();
    if (getLocalReaderIndex() > getLocalWriterIndex()) {
      // reader is above writer in the ring buffer, so we need to avoid overwriting that data
      readBuffer.limit(getLocalReaderIndex() - offset);
    }
    // Clamp read limit to requested length
    readBuffer.limit(Math.min(readBuffer.limit(), length));

    return this.channel.read(readBuffer);
  }

  @Override
  public byte getByte(final long index) throws IndexOutOfBoundsException, IOException {
    checkIndex(index);
    return this.buffer.get((int)(index % localCapacity));
  }

  @Override
  protected void readIntoBuffer(int sourceOffset, byte[] buf, int offset, int length) {
    this.buffer.position(sourceOffset);
    this.buffer.get(buf, offset, length);
  }
}
