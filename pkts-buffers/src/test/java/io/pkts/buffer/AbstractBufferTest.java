package io.pkts.buffer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Obviously, no matter what kind of underlying buffer is used, all the tests
 * should pass. Hence, we are putting all tests in this class and then each
 * sub-class just needs to override the createBuffer-factory method
 * 
 * @author jonas@jonasborjesson.com
 */
public abstract class AbstractBufferTest {

    protected InputStream stream;

    /**
     * A buffer containing a full SIP message
     */
    protected Buffer sipBuffer;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        this.stream = new ByteArrayInputStream(RawData.rawEthernetFrame);

        // always wrap a fresh copy since the RawData.sipBuffer is static
        // and therefore will be affected by all the other tests as well
        this.sipBuffer = RawData.sipBuffer.clone();
    }

    @After
    public void tearDown() throws Exception {
        // currently nothing...
    }

    /**
     * Factory method for creating the type of buffer that you need to test
     * 
     * @param array
     * @return
     */
    public abstract Buffer createBuffer(byte[] array);

    /**
     * Simple helper method to allocate an array of bytes. Each byte in the
     * array will just be +1 from the previous, making it easy to test the
     * various operations, such as slice, getByte etc (since you know exactly
     * what to expect at each index)
     * 
     * @param length
     * @return
     */
    protected byte[] allocateByteArray(final int length) {
        final byte[] array = new byte[length];
        for (int i = 0; i < length; ++i) {
            array[i] = (byte) i;
        }

        return array;
    }

    /**
     * Make sure that we can read the single-crlf sequence correctly.
     *
     * @throws Exception
     */
    @Test
    public void testReadUntilSingleCRLF() throws Exception {

        // no CRLF should lead to null...
        Buffer buffer = createBuffer("hello");
        assertThat(buffer.readUntilSingleCRLF(), is((Buffer)null));

        // Only CR should also lead to null
        buffer = createBuffer("hello\r");
        assertThat(buffer.readUntilSingleCRLF(), is((Buffer)null));

        // now we have two so that should yield a "hello" buffer back.
        buffer = createBuffer("hello\r\n");
        assertThat(buffer.readUntilSingleCRLF().toString(), is("hello"));

        // One hello and then a null
        buffer = createBuffer("hello\r\nworld");
        assertThat(buffer.readUntilSingleCRLF().toString(), is("hello"));
        assertThat(buffer.readUntilSingleCRLF(), is((Buffer)null));

        // One hello and then world
        buffer = createBuffer("hello\r\nworld\r\n");
        assertThat(buffer.readUntilSingleCRLF().toString(), is("hello"));
        assertThat(buffer.readUntilSingleCRLF().toString(), is("world"));

        // One hello and then an empty buffer
        buffer = createBuffer("hello\r\n\r\n");
        assertThat(buffer.readUntilSingleCRLF().toString(), is("hello"));
        assertThat(buffer.readUntilSingleCRLF().isEmpty(), is(true));
    }

    /**
     * Make sure that we can read the double-crlf sequence correctly.
     * 
     * @throws Exception
     */
    @Test
    public void testReadUntilDoubleCRLF() throws Exception {
        Buffer buffer = createBuffer("hello\r\n\r\nworld");
        Buffer hello = buffer.readUntilDoubleCRLF();
        assertThat(hello.toString(), is("hello"));
        assertThat(buffer.toString(), is("world"));

        // note that the first sequence is missing the last '\n'
        buffer = createBuffer("hello\r\n\rworld\r\n\r\n");
        hello = buffer.readUntilDoubleCRLF();
        assertThat(hello.toString(), is("hello\r\n\rworld"));
        assertThat(buffer.toString(), is(""));

        // if we only have double crlf we will end up with two empty buffers...
        buffer = createBuffer("\r\n\r\n");
        final Buffer empty = buffer.readUntilDoubleCRLF();
        assertThat(empty.isEmpty(), is(true));
        // isEmpty for stream backed buffers will never ever return anything but false for isEmpty()
        // however, has readable bytes do accomplish what we want for this test.
        assertThat(buffer.hasReadableBytes(), is(false));

        // of course, if there are two double-crlf sequences we should still
        // only read the first one... and reading the next double-crlf should
        // yield two buffers both with the word "world" in it...
        buffer = createBuffer("hello\r\n\r\nworld\r\n\r\nworld");
        hello = buffer.readUntilDoubleCRLF();
        assertThat(hello.toString(), is("hello"));
        assertThat(buffer.toString(), is("world\r\n\r\nworld"));
        final Buffer world = buffer.readUntilDoubleCRLF();
        assertThat(world.toString(), is("world"));
        assertThat(buffer.toString(), is("world"));

    }

    @Test
    public void testIndexOf() throws Exception {
        final Buffer buffer = createBuffer("hello world ena goa grejor".getBytes());
        assertThat(buffer.indexOf(100, (byte) 'h'), is(0));
        assertThat(buffer.indexOf(100, (byte) 'e'), is(1));
        assertThat(buffer.indexOf(100, (byte) 'l'), is(2));
        assertThat(buffer.indexOf(100, (byte) 'o'), is(4));
        assertThat(buffer.indexOf(100, (byte) ' '), is(5));
        assertThat(buffer.indexOf(100, (byte) 'w'), is(6));
        assertThat(buffer.getByte(6), is((byte) 'w'));

        // indexOf should not affect the reader index so
        // everything should still be there.
        assertThat(buffer.toString(), is("hello world ena goa grejor"));

        // read some byte so now that we try and find 'w' it is actually
        // not there anymore...
        assertThat(buffer.readBytes(11).toString(), is("hello world"));
        assertThat(buffer.indexOf((byte) 'w'), is(-1));
        // however, we still have an 'o' around in the visible area
        // Remember that the index is in relation to the entire buffer
        // and not where the reader index is.
        assertThat(buffer.indexOf((byte) 'o'), is(17));
    }

    @Test
    public void testReadUntil2() throws Exception {
        Buffer buffer = createBuffer("this is a somewhat long string".getBytes());
        Buffer buf = buffer.readUntil(100, (byte) 'a', (byte) 'o');
        assertThat(buf.toString(), is("this is "));

        buffer = createBuffer("this is a somewhat long string".getBytes());
        buf = buffer.readUntil(100, (byte) 'o', (byte) 'a');
        assertThat(buf.toString(), is("this is "));

        buffer = createBuffer("this is a somewhat long string".getBytes());
        buf = buffer.readUntil(100, (byte) 'k', (byte) 'c', (byte) 'o');
        assertThat(buf.toString(), is("this is a s"));

        buffer = createBuffer("this is a somewhat long string".getBytes());
        buf = buffer.readUntil(100, (byte) 'k', (byte) 'c', (byte) 'g');
        assertThat(buf.toString(), is("this is a somewhat lon"));
        assertThat(buffer.toString(), is(" string"));

        // but now we really only want to read a maximum 10 bytes
        buffer = createBuffer("this is a somewhat long string".getBytes());
        try {
            buf = buffer.readUntil(10, (byte) 'k', (byte) 'c', (byte) 'g');
            fail("Expected a ByteNotFoundException");
        } catch (final ByteNotFoundException e) {
            // the buffer should have been left untouched.
            assertThat(buffer.toString(), is("this is a somewhat long string"));
        }

        // also make sure that after slicing the read until stuff
        // works as expected.
        buffer = createBuffer("this is a somewhat long string".getBytes());
        buffer.readBytes(5);
        buf = buffer.readUntil((byte) 'i');
        assertThat(buf.toString(), is(""));

        buf = buffer.readUntil((byte) 'e');
        assertThat(buf.toString(), is("s a som"));

        // slice things up and make sure that our readUntil works on slices as well
        final Buffer slice = buffer.slice(buffer.getReaderIndex() + 5);
        assertThat(slice.toString(), is("what "));
        buf = slice.readUntil((byte) 'a');
        assertThat(buf.toString(), is("wh"));
        assertThat(slice.toString(), is("t "));

        buf = slice.readUntil((byte) ' ');
        assertThat(buf.toString(), is("t"));
        assertThat(slice.toString(), is(""));
        assertThat(slice.hasReadableBytes(), is(false));

        // head back to the buffer which should have been unaffected by our
        // work on the slice. we should have "what long string" left
        buf = buffer.readUntil((byte) 'o');
        assertThat(buf.toString(), is("what l"));
        assertThat(buffer.readUntil((byte) 'n').toString(), is(""));
        assertThat(buffer.readUntil((byte) 'i').toString(), is("g str"));
    }

    @Test
    public void testReadUntil() throws Exception {
        Buffer buffer = createBuffer("hello world".getBytes());
        final Buffer hello = buffer.readUntil((byte) ' ');
        assertThat(hello.toString(), is("hello"));

        // read the next 5 bytes and that should give us the world part
        final Buffer world = buffer.readBytes(5);
        assertThat(world.toString(), is("world"));

        // test to read until 'h' is found, which should yeild nothing
        // since the character that is found is essentially thrown away
        buffer = createBuffer("hello world again".getBytes());
        final Buffer empty = buffer.readUntil((byte) 'h');
        assertThat(empty.toString(), is(""));

        // then read until 'a' is found
        final Buffer more = buffer.readUntil((byte) 'a');
        assertThat(more.toString(), is("ello world "));

        final Buffer gai = buffer.readUntil((byte) 'n');
        assertThat(gai.toString(), is("gai"));

        // and now there should be nothing left
        try {
            buffer.readByte();
            fail("Expected a IndexOutOfBoundsException");
        } catch (final IndexOutOfBoundsException e) {
            // expected
        }

        // no 'u' in the string, exception expected
        buffer = createBuffer("nothing will match".getBytes());
        try {
            buffer.readUntil((byte) 'u');
            fail("Expected a ByteNotFoundException");
        } catch (final ByteNotFoundException e) {
            // expected
        }
    }

    @Test
    public void testRead() throws Exception {
        final Buffer buffer = createBuffer(RawData.rawEthernetFrame);

        // read 100 bytes at a time
        for (int i = 0; i < 5; ++i) {
            final Buffer hundred = buffer.readBytes(100);
            assertThat(hundred.capacity(), is(100));
            for (int k = 0; k < 100; ++k) {
                assertThat(hundred.getByte(k), is(RawData.rawEthernetFrame[k + i * 100]));
            }
        }

        // there are 547 bytes in the rawEthernetFrame so there should be 47
        // left
        final Buffer theRest = buffer.readBytes(47);
        assertThat(theRest.capacity(), is(47));
        for (int k = 0; k < 47; ++k) {
            assertThat(theRest.getByte(k), is(RawData.rawEthernetFrame[k + 500]));
        }
    }

    /**
     * Make sure we can read a single line that doesn't contain any new line
     * characters
     * 
     * @throws Exception
     */
    @Test
    public void testReadLineSingleLineNoCRLF() throws Exception {
        // final String s = "just a regular line, nothing special";
        final String s = "hello";
        final Buffer buffer = createBuffer(s.getBytes());
        assertThat(buffer.readLine().getArray(), is(s.getBytes()));

        // no more lines to read
        assertThat(buffer.readLine(), is((Buffer) null));
    }

    /**
     * Make sure that we can read line by line
     * 
     * @throws Exception
     */
    @Test
    public void testReadLine() throws Exception {
        int count = 0;
        final Buffer buffer = createBuffer(RawData.sipBuffer.getArray());
        while (buffer.readLine() != null) {
            ++count;
        }

        // this sip buffer contains 19 lines
        assertThat(count, is(19));
    }

    /**
     * Contains two lines separated by a single '\n'
     * 
     * @throws Exception
     */
    @Test
    public void testReadLines() throws Exception {
        final String line1 = "this is line 1";
        final String line2 = "and this is line 2";
        Buffer buffer = createBuffer((line1 + "\n" + line2).getBytes());

        // the first readLine should be equal to line 1 and
        // the '\n' should have been stripped off
        assertThat(buffer.readLine().getArray(), is(line1.getBytes()));

        // and then of course check the second line
        assertThat(buffer.readLine().getArray(), is(line2.getBytes()));

        // and then there should be no more
        assertThat(buffer.readLine(), is((Buffer) null));

        // now add only a CR
        buffer = createBuffer((line1 + "\r" + line2).getBytes());
        assertThat(buffer.readLine().getArray(), is(line1.getBytes()));
        assertThat(buffer.readLine().getArray(), is(line2.getBytes()));
        assertThat(buffer.readLine(), is((Buffer) null));

        // now add CR + LF
        buffer = createBuffer((line1 + "\r\n" + line2).getBytes());
        assertThat(buffer.readLine().getArray(), is(line1.getBytes()));
        assertThat(buffer.readLine().getArray(), is(line2.getBytes()));
        assertThat(buffer.readLine(), is((Buffer) null));

        // this one is a little trickier. Add LF + CR + LF, which should
        // result in line1, followed by empty line, followed by line 2
        buffer = createBuffer((line1 + "\n\r\n" + line2).getBytes());
        assertThat(buffer.readLine().getArray(), is(line1.getBytes()));
        assertThat(buffer.readLine().getArray(), is(new byte[0]));
        assertThat(buffer.readLine().getArray(), is(line2.getBytes()));
        assertThat(buffer.readLine(), is((Buffer) null));
    }

    @Test
    public void testReadBytes() throws IOException {
        final Buffer buffer = createBuffer(allocateByteArray(100));
        final Buffer b1 = buffer.readBytes(10);

        // both should have 90 bytes left to read
        // assertThat(buffer.readableBytes(), is(90));
        assertThat(buffer.readByte(), is((byte) 0x0a));
        assertThat(buffer.readByte(), is((byte) 0x0b));
        assertThat(buffer.readByte(), is((byte) 0x0c));
        assertThat(buffer.readByte(), is((byte) 0x0d));

        // the next buffer that will be read is the one at index 10

        // even though we read some bytes off of the main
        // buffer, we should still be able to directly access
        // the bytes
        assertThat(buffer.getByte(0), is((byte) 0x00));
        assertThat(buffer.getByte(5), is((byte) 0x05));
        assertThat(buffer.getByte(16), is((byte) 0x10));
        assertThat(buffer.getByte(32), is((byte) 0x20));

        // and this one should be 10 of course
        assertThat(b1.getReadableBytes(), is(10));
        assertThat(b1.capacity(), is(10));

        assertThat(b1.getByte(0), is((byte) 0x00));
        assertThat(b1.getByte(1), is((byte) 0x01));
        assertThat(b1.getByte(2), is((byte) 0x02));
        assertThat(b1.getByte(3), is((byte) 0x03));
        assertThat(b1.getByte(4), is((byte) 0x04));
        assertThat(b1.getByte(5), is((byte) 0x05));
        assertThat(b1.getByte(6), is((byte) 0x06));
        assertThat(b1.getByte(7), is((byte) 0x07));
        assertThat(b1.getByte(8), is((byte) 0x08));
        assertThat(b1.getByte(9), is((byte) 0x09));

        // the getByte doesn't move the reader index so we should be able
        // to read through all the above again

        assertThat(b1.readByte(), is((byte) 0x00));
        assertThat(b1.readByte(), is((byte) 0x01));
        assertThat(b1.readByte(), is((byte) 0x02));
        assertThat(b1.readByte(), is((byte) 0x03));
        assertThat(b1.readByte(), is((byte) 0x04));
        assertThat(b1.readByte(), is((byte) 0x05));
        assertThat(b1.readByte(), is((byte) 0x06));
        assertThat(b1.readByte(), is((byte) 0x07));
        assertThat(b1.readByte(), is((byte) 0x08));
        assertThat(b1.readByte(), is((byte) 0x09));
    }

    @Test
    public void testSlice() throws Exception {
        final Buffer buffer = createBuffer(allocateByteArray(100));
        final Buffer b1 = buffer.slice(50, 70);
        assertThat(b1.capacity(), is(20));
        assertThat(b1.readByte(), is((byte) 50));
        assertThat(b1.getByte(0), is((byte) 50));

        assertThat(b1.readByte(), is((byte) 51));
        assertThat(b1.getByte(1), is((byte) 51));

        assertThat(b1.getByte(19), is((byte) 69));
        try {
            b1.getByte(20);
            fail("Expected an IndexOutOfBoundsException");
        } catch (final IndexOutOfBoundsException e) {
            // expected
        }
        try {
            b1.getByte(21);
            fail("Expected an IndexOutOfBoundsException");
        } catch (final IndexOutOfBoundsException e) {
            // expected
        }

        // sliceing it again is based on the seconds
        // buffer's point-of-view
        final Buffer b2 = b1.slice(b1.capacity());

        // remember, we already read two bytes above so we should
        // be at 52
        assertThat(b2.readByte(), is((byte) 52));
        assertThat(b2.readByte(), is((byte) 53));

        // since the b1 buffer already have 2 of its bytes
        // read, there are 18 bytes left
        assertThat(b2.capacity(), is(18));
        assertThat(b2.getByte(17), is((byte) 69));

        try {
            // the capacity is 18, which means that the last
            // index is 17, which means that trying to access
            // 18 should yield in an exception
            b2.getByte(18);
            fail("Expected an IndexOutOfBoundsException");
        } catch (final IndexOutOfBoundsException e) {
            // expected
        }

        // grab the entire b2 buffer
        final Buffer b3 = b2.slice(0, 18);
        assertThat(b3.capacity(), is(18));
        assertThat(b3.readByte(), is((byte) 52));
        assertThat(b3.getByte(b3.capacity() - 1), is((byte) 69));
    }

    /**
     * Test to make sure that it is possible to mark the reader index, continue
     * reading and then reset the buffer and as such, continue from where we
     * last called marked...
     * 
     * @throws Exception
     */
    @Test
    public void testResetAndMarkReaderIndex() throws Exception {
        final Buffer buffer = createBuffer(allocateByteArray(100));

        // read and "throw away" 10 bytes
        buffer.readBytes(10);

        // make sure that the next byte that is being read is 10
        assertThat(buffer.readByte(), is((byte) 0x0A));

        // reset the buffer and make sure that now, the next
        // byte being read should be zero again
        buffer.resetReaderIndex();
        assertThat(buffer.readByte(), is((byte) 0x00));
        assertThat(buffer.readByte(), is((byte) 0x01));

        // mark it and read a head, then check that we are in
        // the correct spot, reset and check that we are back again
        buffer.markReaderIndex();
        assertThat(buffer.readByte(), is((byte) 0x02));
        buffer.readBytes(10);
        assertThat(buffer.readByte(), is((byte) 0x0D));
        buffer.resetReaderIndex();
        assertThat(buffer.readByte(), is((byte) 0x02));

        // make sure that it works for slices as well
        final Buffer slice = buffer.slice(30, 50);
        assertThat(slice.readByte(), is((byte) 30));
        assertThat(slice.readByte(), is((byte) 31));
        slice.resetReaderIndex();
        assertThat(slice.readByte(), is((byte) 30));
        slice.readBytes(5);
        slice.markReaderIndex();
        assertThat(slice.readByte(), is((byte) 36));
        assertThat(slice.readByte(), is((byte) 37));
        assertThat(slice.readByte(), is((byte) 38));
        slice.resetReaderIndex();
        assertThat(slice.readByte(), is((byte) 36));
    }

    public Buffer createBuffer(final String s) {
        return createBuffer(s.getBytes());
    }

}
