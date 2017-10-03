/**
 * 
 */
package io.pkts.buffer;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * @author jonas@jonasborjesson.com
 */
public class BoundedInputStreamBufferTest {


    /**
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * {@inheritDoc}
     */
    public Buffer createBuffer(final byte[] array) {
        return new BoundedInputStreamBuffer(new ByteArrayInputStream(array));
    }

    /**
     * The {@link InputStreamBuffer} is dynamically reading off a stream so we
     * don't really know how much we are going to read. Since we cant really
     * allocate a huge buffer right off we need to allocate more as we go along
     * (up until a max size).
     * 
     * Make sure that no matter what size each of "sub-arrays" have, we should
     * be able to read the input stream in a normal fashion
     * 
     * @throws Exception
     */
    @Test
    public void testDynamicallyIncreaseBuffer() throws Exception {
        // kind of stupid to have such low capacity but should be possible
        verifyDynamicallyIncreaseBuffer(1);

        // some other "random" buffer sizes.
        for (int i = 1; i < 200; ++i) {
            verifyDynamicallyIncreaseBuffer(i);
        }

        // with a initial capacity of 1000 the entire sip buffer
        // should fit within one buffer. Not that it should matter
        // but test it to make sure that it works
        verifyDynamicallyIncreaseBuffer(1000);

        // the buffer that we are using to test this stuff is 505
        // long so test around that boundary to make sure we dont
        // screw up with a 1-off error
        verifyDynamicallyIncreaseBuffer(504);
        verifyDynamicallyIncreaseBuffer(505);
        verifyDynamicallyIncreaseBuffer(506);
    }

    /**
     * Check that we can read in the stream no matter what the initial capacity
     * of the underlying buffer is
     * 
     * @param initialSize
     * @throws Exception
     */
    private void verifyDynamicallyIncreaseBuffer(final int initialCapacity) throws Exception {
        // setting the initial capacity to very low
        // remember, the raw data is 505 bytes total
        final byte[] content = RawData.sipBuffer.getArray();
        final Buffer buffer = new InputStreamBuffer(initialCapacity, new ByteArrayInputStream(content));

        final Buffer initial = buffer.readBytes(50);
        assertContent(initial, content, 0, 50);

        final Buffer forty = buffer.readBytes(40);
        assertContent(forty, content, 50, 40);

        final Buffer fifty = buffer.readBytes(50);
        assertContent(fifty, content, 90, 50);

        // we are now at 140. Read another 300 bytes,
        final Buffer threehundred = buffer.readBytes(300);
        assertContent(threehundred, content, 140, 300);

        // which leaves us with 65 bytes. read those as well and check them
        final Buffer theRest = buffer.readBytes(65);
        assertContent(theRest, content, 440, 65);

        // and there should not be any more bytes left in the input stream
        assertThat(buffer.hasReadableBytes(), is(false));

        // so this should obviously fail as well
        try {
            buffer.readBytes(5);
            fail("expected an IndexOutOfBoundsException");
        } catch (final IndexOutOfBoundsException e) {
            // expected
        }

    }

    /**
     * Check that the buffer correctly reads more bytes from the underlying stream when they're needed.
     */
    @Test
    public void testReadingOnDemand() throws Exception {
        final byte[] content = RawData.rawEthernetFrame;
        final InputStream in = new ByteArrayInputStream(content);
        final Buffer outer = Buffers.wrap(in);
        final Buffer first = outer.readBytes(50);
        final Buffer second = outer.readBytes(150);
        assertContent(first, content, 0, 50);
        assertContent(second, content, 50, 150);
    }

    /**
     * After we have been reading etc it is also important that we actually
     * verify that the new read buffers indeed contains the correct content.
     * Especially important to check for 1-off errors.
     * 
     * @param buffer the buffer whose content we should check
     * @param actual the actual content
     * @param offset the offest into the actual content where we expect to find
     *            the content of the buffer
     */
    private void assertContent(final Buffer buffer, final byte[] actual, final int offset, final int length) throws Exception {
        assertThat("Size differs", buffer.capacity(), is(length));
        for (int i = 0; i < buffer.capacity(); ++i) {
            assertThat("Index: " + i + " Actual Index: " + (i + offset), buffer.getByte(i), is(actual[i + offset]));
        }
    }

    /**
     * Check that slice is doing the right thing with a wrap-around buffer.
     */
    @Test
    public void testSliceAndWrapAroundBuffer() throws Exception {
        // Content is 547 bytes, default buffer is 40kb
        final byte[] content = RawData.rawEthernetFrame;
        final InputStream in = new ByteArrayInputStream(content);

        // Use a prime number that is in interval (size < x < size*2)
        final int bufferSize = 719;

        assertTrue("Buffer size not valid", bufferSize > content.length && content.length * 2 > bufferSize);

        // Make internal buffer _only_ 50% bigger than content.
        final BoundedInputStreamBuffer buffer = new BoundedInputStreamBuffer(bufferSize, in);

        // Read more than twice! Second iteration should hit buffer roll-around logic.
        for (int counter = 0; counter < 20; counter++) {

//            final Buffer slice = buffer.slice((long)buffer.getReaderIndex(), buffer.getReaderIndex() + content.length);
//            assertContent(slice, content, 0, content.length);

            final Buffer data = buffer.readBytes(content.length);
            assertContent(data, content, 0, content.length);

            in.reset();
        }
    }
}
