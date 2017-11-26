package io.pkts.buffer;

import java.io.IOException;
import java.io.InputStream;

/**
 * Streaming Buffer implementation wrapping an InputStream by reading it into a raw byte[] ring buffer.
 *
 * @author jonas@jonasborjesson.com
 */
public class BoundedInputStreamBuffer extends StreamingBuffer {

    private final InputStream is;

    final byte[] buffer;

    BoundedInputStreamBuffer(final InputStream is) {
        this(DEFAULT_CAPACITY, is);
    }

    /**
     *
     * @param bufferCapacity To be sure that the PCAP framer works, this needs to be same or larger than SNAPLENGTH used to get PCAP.
     * Default SNAPLENGTH of tcpdump is 262144. If using '-s NNN' parameter of tcpdump, you should also be able
     * to reduce this to a lower value for less memory usage.

     * @param is
     */
    public BoundedInputStreamBuffer(final int bufferCapacity, final InputStream is) {
        super(bufferCapacity);
        assert is != null;
        this.is = is;
        buffer = new byte[bufferCapacity];
    }

    @Override
    protected void readIntoBuffer(int sourceOffset, byte[] buf, int offset, int length) {
        System.arraycopy(this.buffer, sourceOffset, buf, offset, length);
    }

    @Override
    protected int actuallyReadFromStream(int offset, int length) throws IOException {
        return this.is.read(this.buffer, offset, length);
    }

    public byte getByte(final long index) throws IndexOutOfBoundsException, IOException {
        checkIndex(index);
        return this.buffer[(int)(index % localCapacity)];
    }
}
