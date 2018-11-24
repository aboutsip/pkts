package io.pkts.buffer;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class ReadOnlyByteBufferTest extends AbstractBufferTest {

    @Override
    public Buffer createBuffer(final byte[] array) {
        return ReadOnlyBuffer.of(array);
    }


    /**
     * Ensure that when we do slice operations that we get back a {@link ReadOnlyBuffer} and not
     * something else. Need to do a major overhaul of the Buffer stuff so that when you do slice
     * on a particular buffer, you get back that buffer and you don't have to actually cast it...
     *
     * @throws Exception
     */
    @Test
    public void testSliceReadOnly() throws Exception {
        final ReadOnlyBuffer buffer = ReadOnlyBuffer.of("Testing read-only");
        final ReadOnlyBuffer slice1 = ensureSliceIsReadOnly(buffer, 8, buffer.capacity(), "read-only");
        final ReadOnlyBuffer slice2 = ensureSliceIsReadOnly(buffer, -1, -1, "Testing read-only");
        final ReadOnlyBuffer slice3 = ensureSliceIsReadOnly(buffer, -1, 7, "Testing");

        // now move forward with the various buffers and ensure they do not effect each other.
        buffer.readByte();
        ensureSliceIsReadOnly(buffer, -1, 7, "esting");
        final ReadOnlyBuffer read01 = (ReadOnlyBuffer) buffer.readUntil((byte) ' ');
        assertThat(read01.toString(), is("esting"));

        // slice the rest of the original buffer
        final ReadOnlyBuffer slice4 = (ReadOnlyBuffer) buffer.slice();
        assertThat(slice4.toString(), is("read-only"));

        assertThat(slice2.toString(), is("Testing read-only"));
        assertThat(slice3.toString(), is("Testing"));

        assertThat(slice2.readUntil((byte) ' ').toString(), is(slice3.toString()));
    }

    private ReadOnlyBuffer ensureSliceIsReadOnly(final ReadOnlyBuffer buffer, final int start, final int stop, final String expected) throws Exception {
        // will blow up if this isn't a read only buffer, which is what we are testing.
        final ReadOnlyBuffer slice;
        if (start == -1 && stop == -1) {
            slice = (ReadOnlyBuffer) buffer.slice();
        } else if (start == -1) {
            slice = (ReadOnlyBuffer) buffer.slice(stop);
        } else {
            slice = (ReadOnlyBuffer) buffer.slice(start, stop);
        }

        assertThat(slice.toString(), is(expected));
        return slice;
    }

    @Test
    public void testCannotWrite() throws Exception {
        final ReadOnlyBuffer buffer = ReadOnlyBuffer.of("Testing read-only");
        ensureNoWriteSupport(buffer, b -> b.write('b'));
        ensureNoWriteSupport(buffer, b -> b.write(123));
        ensureNoWriteSupport(buffer, b -> b.write(12L));
        ensureNoWriteSupport(buffer, b -> b.write("nope"));
        ensureNoWriteSupport(buffer, b -> b.setByte(1, (byte) 12));
        ensureNoWriteSupport(buffer, b -> b.setInt(1, 12));
        ensureNoWriteSupport(buffer, b -> b.setUnsignedByte(1, (short) 12));
        ensureNoWriteSupport(buffer, b -> b.setUnsignedInt(1, 12L));
        ensureNoWriteSupport(buffer, b -> b.setWriterIndex(12));
    }

    private void ensureNoWriteSupport(final Buffer buffer, final ThrowingConsumer<Buffer> writeOp) throws Exception {
        try {
            writeOp.accept(buffer);
            fail("Expected to fail with a " + WriteNotSupportedException.class);
        } catch (final WriteNotSupportedException e) {
            // expected
        }
    }

    @FunctionalInterface
    public interface ThrowingConsumer<T> {
        // because the default Consumer doesn't throw exceptions...
        void accept(T t) throws Exception;
    }
}