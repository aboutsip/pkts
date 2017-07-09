/**
 * 
 */
package io.pkts.buffer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public class ByteBufferTest extends AbstractBufferTest {

    /**
     * @throws java.lang.Exception
     */
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testWriteIntAsString() throws Exception {
        final Buffer buffer = Buffers.createBuffer(100);
        buffer.writeAsString(0);
        buffer.write((byte) ' ');
        buffer.writeAsString(10);
        buffer.write((byte) ' ');
        buffer.writeAsString(100);
        buffer.write((byte) ' ');
        buffer.writeAsString(9712);
        assertThat(buffer.toString(), is("0 10 100 9712"));
    }

    @Test
    public void testWriteLongAsString() throws Exception {
        final Buffer buffer = Buffers.createBuffer(100);
        buffer.writeAsString(0L);
        buffer.write((byte) ' ');
        buffer.writeAsString(10L);
        buffer.write((byte) ' ');
        buffer.writeAsString(100L);
        buffer.write((byte) ' ');
        buffer.writeAsString(9712L);
        assertThat(buffer.toString(), is("0 10 100 9712"));
    }

    /**
     * 
     * @throws Exception
     */
    @Test
    public void testWrapArraySpecifyingTheWindows() throws Exception {
        final Buffer buffer = Buffers.wrap("hello world".getBytes(), 3, 9);
        assertThat(buffer.toString(), is("lo wor"));
        assertThat(buffer.getByte(0), is((byte) 'l'));
        assertThat(buffer.getByte(1), is((byte) 'o'));

        assertThat(buffer.readByte(), is((byte) 'l'));
        assertThat(buffer.readByte(), is((byte) 'o'));

        assertThat(buffer.getByte(0), is((byte) 'l'));
        assertThat(buffer.getByte(1), is((byte) 'o'));
    }

    /**
     * A buffer can be parsed as an integer assuming there are no bad characters
     * in there.
     * 
     * @throws Exception
     */
    @Test
    public void testParseAsInt() throws Exception {
        assertParseAsInt("1234", 1234);
        assertParseAsInt("-1234", -1234);
        assertParseAsInt("0", 0);
        assertParseAsInt("5060", 5060);

        // negative tests
        assertParseAsIntBadInput("apa");
        assertParseAsIntBadInput("");
        assertParseAsIntBadInput("-5 nope, everything needs to be digits");
        assertParseAsIntBadInput("5 nope, everything needs to be digits");

        assertParseAsIntSliceFirst("hello:5060:asdf", 6, 10, 5060);
        assertParseAsIntSliceFirst("hello:-5:asdf", 6, 8, -5);
    }

    /**
     * Bug found when parsing a SIP URI. The port wasn't picked up because we
     * were doing parseToInt without regards to the offset within the buffer
     * 
     * @throws Exception
     */
    @Test
    public void testParseAsIntBugWhenParsingSipURI() throws Exception {
        final Buffer b = Buffers.wrap("sip:alice@example.com:5099");
        assertThat(b.readBytes(4).toString(), is("sip:"));
        assertThat(b.readBytes(5).toString(), is("alice"));
        assertThat(b.readByte(), is((byte) '@'));
        final Buffer hostPort = b.slice();
        assertThat(hostPort.toString(), is("example.com:5099"));
        assertThat(hostPort.readBytes(11).toString(), is("example.com"));
        final Buffer host = hostPort.slice(0, 11);
        assertThat(host.toString(), is("example.com"));
        assertThat(hostPort.readByte(), is((byte) ':'));
        assertThat(hostPort.parseToInt(), is(5099));
    }

    private void assertParseAsIntSliceFirst(final String s, final int lowerSlice, final int upperSlice,
            final int expected) throws Exception {
        final Buffer buffer = createBuffer(s);
        buffer.readByte(); // should affect nothing
        buffer.readByte(); // should affect nothing
        buffer.readByte(); // should affect nothing
        buffer.readByte(); // should affect nothing
        final Buffer number = buffer.slice(lowerSlice, upperSlice);
        assertThat(number.parseToInt(), is(expected));
    }

    private void assertParseAsIntBadInput(final String badNumber) throws IOException {
        try {
            final Buffer buffer = createBuffer(badNumber);
            buffer.parseToInt();
            fail("Expected a NumberFormatException");
        } catch (final NumberFormatException e) {
            // expected
        }
    }

    private void assertParseAsInt(final String number, final int expectedNumber) throws NumberFormatException,
    IOException {
        final Buffer buffer = createBuffer(number);
        assertThat(buffer.parseToInt(), is(expectedNumber));
    }

    /**
     * When cloning, the underlying array will also be copied meaning that any
     * changes to the original buffer will not affect the cloned one.
     * 
     * @throws Exception
     */
    @Test
    public void testClone() throws Exception {
        final Buffer buffer = Buffers.wrap(allocateByteArray(100));
        final Buffer clone = buffer.clone();
        assertBuffers(buffer, clone);

        // now, change something in the clone and make sure that
        // it does NOT affect the original buffer
        clone.setByte(0, (byte) 0x12);
        assertThat(clone.getByte(0), is((byte) 0x12));
        assertThat(buffer.getByte(0), is((byte) 0x00));

        // make sure that cloning slices are also
        // correct
        final Buffer slice = clone.slice(30, 40);
        assertThat(slice.getByte(0), is((byte) 30));
        final Buffer sliceClone = slice.clone();
        assertBuffers(sliceClone, slice);
    }

    @Test
    public void testMap() throws Exception {
        final Buffer a = createBuffer("hello");
        final Map<Buffer, String> map = new HashMap<Buffer, String>();
        map.put(a, "fup");
        assertThat(map.get(a), is("fup"));

        final Buffer b = createBuffer("hello");
        assertThat(map.get(b), is("fup"));

        final Buffer c = createBuffer("world");
        map.put(c, "apa");
        assertThat(map.containsKey(c), is(true));

        final Buffer d = createBuffer("nope");
        assertThat(map.containsKey(d), is(false));
    }

    @Test
    public void testHashCode() {
        final Buffer a = createBuffer("hello");
        final Buffer b = createBuffer("hello");
        final Buffer c = createBuffer("world");
        assertThat(a.hashCode(), is(b.hashCode()));
        assertThat(c.hashCode(), not(b.hashCode()));
    }

    @Test
    public void testEqualsBasicStuff() throws Exception {
        assertBufferEquality("hello", "hello", true);
        assertBufferEquality("hello", "world", false);
        assertBufferEquality("hello ", "world", false);
        assertBufferEquality("hello world", "world", false);
        assertBufferEquality("Hello", "hello", false);
        assertBufferEquality("h", "h", true);
    }

    @Test
    public void testEqualsIgnoreCase() throws Exception {
        assertBufferEqualityIgnoreCase("Hello", "hello", true);
        assertBufferEqualityIgnoreCase("this is A lOng string...", "tHis iS a long string...", true);
        assertBufferEqualityIgnoreCase("Hello", "HEllo", true);
        assertBufferEqualityIgnoreCase("Hello", "HEllO", true);
        assertBufferEqualityIgnoreCase("Hello", "HEllO ", false); // space at the end
        assertBufferEqualityIgnoreCase("123 abC", "123 abc", true);
        assertBufferEqualityIgnoreCase("123 abC !@#$", "123 ABc !@#$", true);
    }

    @Test
    public void testUtf8EqualsIgnoreCase() throws Exception {
        // case-insensitive comparison looks only at the 5 least significant bits for characters
        // that are in the 7-bit ASCII range.

        // 1-byte UTF-8 characters
        assertThat(createBuffer(new byte[] {0x40}).equalsIgnoreCase(createBuffer(new byte[] {0x40})), is(true));
        assertThat(createBuffer(new byte[] {0x40}).equalsIgnoreCase(createBuffer(new byte[] {0x60})), is(false));
        assertThat(createBuffer(new byte[] {0x41}).equalsIgnoreCase(createBuffer(new byte[] {0x41})), is(true));
        assertThat(createBuffer(new byte[] {0x41}).equalsIgnoreCase(createBuffer(new byte[] {0x61})), is(true)); // 'A' and 'a'
        assertThat(createBuffer(new byte[] {0x5a}).equalsIgnoreCase(createBuffer(new byte[] {0x5a})), is(true));
        assertThat(createBuffer(new byte[] {0x5a}).equalsIgnoreCase(createBuffer(new byte[] {0x7a})), is(true)); // 'Z' and 'z'
        assertThat(createBuffer(new byte[] {0x5b}).equalsIgnoreCase(createBuffer(new byte[] {0x5b})), is(true));
        assertThat(createBuffer(new byte[] {0x5b}).equalsIgnoreCase(createBuffer(new byte[] {0x7b})), is(false));

        // 2-byte UTF-8 characters. The second byte has the 5 least significant bits the same. In Java,
        // bytes are signed, so we need to convert unsigned notation to signed for the compiler to take it.

        assertThat(createBuffer(new byte[] {0xc0 - 256, 0x80 - 256}).equalsIgnoreCase(createBuffer(new byte[] {0xc0 - 256, 0x80 - 256})), is(true));
        assertThat(createBuffer(new byte[] {0xc0 - 256, 0x80 - 256}).equalsIgnoreCase(createBuffer(new byte[] {0xc0 - 256, 0xa0 - 256})), is(false));
        assertThat(createBuffer(new byte[] {0xc0 - 256, 0x8f - 256}).equalsIgnoreCase(createBuffer(new byte[] {0xc0 - 256, 0x8f - 256})), is(true));
        assertThat(createBuffer(new byte[] {0xc0 - 256, 0x8f - 256}).equalsIgnoreCase(createBuffer(new byte[] {0xc0 - 256, 0xaf - 256})), is(false));
    }

    private void assertBufferEqualityIgnoreCase(final String a, final String b, final boolean equals) {
        final Buffer bufA = createBuffer(a);
        final Buffer bufB = createBuffer(b);
        assertThat(bufA.equalsIgnoreCase(bufB), is(equals));
        assertThat(bufB.equalsIgnoreCase(bufA), is(equals));
    }

    private void assertBufferEquality(final String a, final String b, final boolean equals) {
        final Buffer bufA = createBuffer(a);
        final Buffer bufB = createBuffer(b);
        assertThat(bufA.equals(bufB), is(equals));
        assertThat(bufB.equals(bufA), is(equals));
    }

    @Test
    public void testEqualsHashCode() throws Exception {
        final Buffer b1 = createBuffer("hello world");
        final Buffer b2 = createBuffer("hello world");
        assertThat(b1, is(b2));
        assertThat(b1.hashCode(), is(b2.hashCode()));

        final Buffer b3 = createBuffer("hello not world");
        assertThat(b1, is(not(b3)));
        assertThat(b1.hashCode(), is(not(b3.hashCode())));
        assertThat(b2, is(not(b3)));

        // because the way we do equals right now when one of the
        // buffers has read a head they are no longer equal.
        // One motivation is because the bytes that have been
        // consumed actually can be discarded...
        b2.readByte();
        assertThat(b1, is(not(b2)));
        assertThat(b1.hashCode(), is(not(b2.hashCode())));

        // because of this, if we now read a head in both
        // b1 and b3 and only leave the "world" portion left
        // then all of a sudden b1 and b3 actually are equal
        b1.readBytes(6);
        b3.readBytes(10);
        assertThat(b1, is(b3));
        assertThat(b1.hashCode(), is(b3.hashCode()));

        final Buffer a1 = createBuffer("123 world");
        final Buffer a2 = createBuffer("456 world");
        assertThat(a1, not(a2));
        assertThat(a1.hashCode(), not(a2.hashCode()));

        final Buffer a1_1 = a1.readBytes(3);
        final Buffer a2_1 = a2.readBytes(3);
        assertThat(a1_1, not(a2_1));
        assertThat(a1_1.hashCode(), not(a2_1.hashCode()));

        // now they should be equal
        final Buffer a1_2 = a1.slice();
        final Buffer a2_2 = a2.slice();
        assertThat(a1_2, is(a2_2));
        assertThat(a1_2.hashCode(), is(a2_2.hashCode()));

        final Buffer a1_3 = a1.readBytes(5);
        final Buffer a2_3 = a2.readBytes(5);
        assertThat(a1_3, is(a2_3));
        assertThat(a1_3.hashCode(), is(a2_3.hashCode()));

        final Buffer from = createBuffer("From");
        final Buffer fromHeader = createBuffer("some arbitrary crap first then From and then some more shit");
        fromHeader.readBytes(31);
        final Buffer fromAgain = fromHeader.readBytes(4);
        assertThat(fromAgain.toString(), is("From"));
        assertThat(fromAgain, is(from));
        assertThat(from, is(fromAgain));

    }

    /**
     * Test the read until on a sliced buffer.
     * 
     * @throws Exception
     */
    @Test
    public void testReadUntilFromSlicedBuffer() throws Exception {
        final Buffer original = createBuffer("hello world this is going to be a longer one".getBytes());
        final Buffer buffer = original.slice(6, original.capacity());
        final Buffer world = buffer.readUntil((byte) ' ');
        assertThat(world.toString(), is("world"));

        final Buffer longer = buffer.readUntil((byte) 'a');
        assertThat(longer.toString(), is("this is going to be "));

        final Buffer theRest = buffer.readLine();
        assertThat(theRest.toString(), is(" longer one"));
    }

    private void assertBuffers(final Buffer b1, final Buffer b2) throws Exception {
        // make sure they are the exact same size and have
        // the same content etc
        assertThat(b1.capacity(), is(b2.capacity()));
        for (int i = 0; i < b1.capacity(); ++i) {
            assertThat(b1.getByte(i), is(b2.getByte(i)));
        }
        // should really be the same as the for loop but
        // I guess you never know...
        assertThat(b1.getArray(), is(b2.getArray()));
    }

    @Test
    public void testSliceEmptyBuffer() throws Exception {
        Buffer buffer = Buffers.EMPTY_BUFFER;
        assertEmptyBuffer(buffer.slice());

        buffer = Buffers.wrap("a little harder");
        buffer.readBytes(buffer.capacity());
        assertEmptyBuffer(buffer.slice());
    }

    /**
     * Convenience method for making sure that the buffer is indeed empty
     * 
     * @param buffer
     */
    private void assertEmptyBuffer(final Buffer buffer) {
        assertThat(buffer.capacity(), is(0));
        assertThat(buffer.hasReadableBytes(), is(false));
        assertThat(buffer.isEmpty(), is(true));
    }

    /**
     * Since a slice is sharing the same data, changing the data will affect
     * each other.
     */
    @Test
    public void testSliceChangesAffectEachOther() throws Exception {
        final Buffer buffer = Buffers.wrap(allocateByteArray(100));
        final Buffer b1 = buffer.slice(10, 20);

        assertThat(b1.getByte(0), is((byte) 10));
        assertThat(buffer.getByte(10), is((byte) 10));

        b1.setByte(0, (byte) 0xFF);
        assertThat(b1.getByte(0), is((byte) 0xFF));
        assertThat(buffer.getByte(10), is((byte) 0xFF));
    }

    /**
     * Test get bytes when the destination is always bigger than the src.
     * 
     * @throws Exception
     */
    @Test
    public void testGetBytesDestinationBigger() throws Exception {
        final Buffer buffer = Buffers.wrap("hello world");
        Buffer b = Buffers.createBuffer(100);
        buffer.getBytes(b);
        assertThat(b.toString(), is("hello world"));
        assertThat(b.getWritableBytes(), is(100 - "hello world".length()));
        assertThat(b.getReadableBytes(), is("hello world".length()));

        b = Buffers.createBuffer(100);
        buffer.getBytes(5, b);
        assertThat(b.toString(), is(" world"));
        assertThat(b.getWritableBytes(), is(100 - " world".length()));
        assertThat(b.getReadableBytes(), is(" world".length()));
    }

    /**
     * Make sure that slicing multiple times works since that is a very common
     * operation.
     * 
     * @throws Exception
     */
    @Test
    public void testDoubleSlice() throws Exception {
        final String str = "This is a fairly long sentance and all together this should be 71 bytes";
        final Buffer buffer = createBuffer(str);
        buffer.readByte();
        final Buffer b1 = buffer.slice();
        assertThat(b1.toString(), is(str.substring(1)));

        final Buffer b2 = b1.readBytes(20);
        assertThat(b2.toString(), is(str.subSequence(1, 21)));

        // now, slice the already sliced b1_1. Since we haven't ready anything
        // from b1_1Slice just yet we should end up with the exact same thing.
        final Buffer b2Slice = b2.slice();
        assertThat(b2Slice.toString(), is(str.subSequence(1, 21)));

        final Buffer again = b2Slice.slice(4, 10);
        assertThat(again.toString(), is("is a f"));
    }

    /**
     * Make sure that we can do getBytes after we have written bytes a few
     * times.
     * 
     * @throws Exception
     */
    @Test
    public void testGetBytesAfterBunchOfWrites() throws Exception {
        final Buffer buffer = Buffers.createBuffer(100);
        buffer.write("hello");

        final Buffer copy = Buffers.createBuffer(100);
        buffer.getBytes(copy);
        assertThat(copy.toString(), is("hello"));
        copy.write((byte) ' ');
        copy.write((byte) 'a');
        copy.write((byte) 'b');
        copy.write((byte) ' ');
        copy.write("tjo");
        copy.write((byte) ' ');
        copy.writeAsString(5060);
        copy.write((byte) ' ');
        copy.write("fup");
        assertThat(copy.toString(), is("hello ab tjo 5060 fup"));
    }

    /**
     * Test the getBytes method where the destination is always smaller than the
     * buffer we are getting the bytes from
     * 
     * @throws Exception
     */
    @Test
    public void testGetBytes() throws Exception {
        final Buffer buffer = Buffers.wrap("hello world");
        Buffer b = Buffers.createBuffer(5);
        assertThat(b.getWritableBytes(), is(5));
        assertThat(b.getReadableBytes(), is(0));
        buffer.getBytes(b);
        assertThat(b.toString(), is("hello"));
        assertThat(b.getWritableBytes(), is(0));
        assertThat(b.getReadableBytes(), is(5));

        // original buffer should have been left intact
        assertThat(buffer.getWritableBytes(), is(0));
        assertThat(buffer.getReadableBytes(), is("hello world".length()));
        assertThat(buffer.toString(), is("hello world"));

        // now, if we read the first part and then do
        // getBytes we should be getting just "world"
        buffer.readBytes("hello ".length());
        b = Buffers.createBuffer(5);
        buffer.getBytes(b);
        assertThat(b.toString(), is("world"));

        // however, if we specify the index we should
        // be getting stuff from there.
        b = Buffers.createBuffer(5);
        buffer.getBytes(2, b);
        assertThat(b.toString(), is("llo w"));
    }

    /**
     * 
     * @throws Exception
     */
    @Test
    public void testGetBytesAfterSlice() throws Exception {
        final Buffer buffer = Buffers.wrap("hello world");
        buffer.readBytes(5);
        buffer.readByte();
        final Buffer world = buffer.slice();
        assertThat(world.toString(), is("world"));
        final Buffer copy = Buffers.createBuffer(50);
        world.getBytes(0, copy);
        assertThat(copy.toString(), is("world"));
    }

    @Test
    public void testWrapLong() throws Exception {
        assertThat(Buffers.wrap(123L).toString(), is("123"));
        assertThat(Buffers.wrap(-123L).toString(), is("-123"));
    }

    @Test
    public void testWrapInt() throws Exception {
        assertThat(Buffers.wrap(123).toString(), is("123"));
        assertThat(Buffers.wrap(-123).toString(), is("-123"));
    }

    /**
     * Make sure that we are able to write to a buffer.
     * 
     * @throws Exception
     */
    @Test
    public void testWrite() throws Exception {
        final Buffer buffer = Buffers.createBuffer(100);
        assertThat(buffer.toString(), is(""));
        assertThat(buffer.getWritableBytes(), is(100));

        final String s = "hello world";
        buffer.write(s);
        assertThat(buffer.getWritableBytes(), is(100 - s.length()));
        assertThat(buffer.toString(), is(s));
        assertThat(buffer.readBytes(6).toString(), is("hello "));
        assertThat(buffer.toString(), is("world"));

        // just because we read from the buffer shouldn't affect
        // the writer area.
        assertThat(buffer.getWritableBytes(), is(100 - s.length()));

        // consume everything by using the readLine()
        assertThat(buffer.readLine().toString(), is("world"));

        // which means that the buffer now has nothing left in
        // the reader area
        assertThat(buffer.toString(), is(""));
        assertThat(buffer.getReadableBytes(), is(0));

        // however, we can write some stuff back to the buffer
        buffer.write((byte) 'a');
        buffer.write("boutsip.com");
        assertThat(buffer.getWritableBytes(), is(100 - s.length() - "aboutsip.com".length()));
        assertThat(buffer.toString(), is("aboutsip.com"));

        // the slice should be independent
        final Buffer b = buffer.slice();
        assertThat(b.getReadableBytes(), is(12));
        assertThat(b.getByte(0), is((byte) 'a'));
        assertThat(b.getByte(1), is((byte) 'b'));
        assertThat(b.getByte(2), is((byte) 'o'));
        assertThat(b.toString(), is("aboutsip.com"));
        assertThat(b.getWritableBytes(), is(0));
        assertThat(b.readLine().toString(), is("aboutsip.com"));
        assertThat(b.getWritableBytes(), is(0));
        try {
            b.write((byte) 'h');
            fail("Should not have been able to write");
        } catch (final IndexOutOfBoundsException e) {
            // expected
        }

        buffer.write(" " + s);
        assertThat(buffer.getWritableBytes(), is(100 - s.length() * 2 - 1 - "aboutsip.com".length()));
        assertThat(buffer.toString(), is("aboutsip.com hello world"));
    }

    /**
     * Make sure that we can write exactly the amount we allocated for.
     * 
     * @throws Exception
     */
    @Test
    public void testWriteJustEnough() throws Exception {
        final Buffer buffer = Buffers.createBuffer("hello world".length() * 2 + 1);
        buffer.write("hello world");
        buffer.write(" ");
        buffer.write("hello world");
        assertThat(buffer.toString(), is("hello world hello world"));

        // nothing more to write
        assertThat(buffer.getWritableBytes(), is(0));

        // but everything to read, which should be the same as the
        // actual capacity of the buffer
        assertThat(buffer.getReadableBytes(), is(buffer.capacity()));

        try {
            buffer.write("b");
            fail("Expected an IndexOutOfBoundsException");
        } catch (final IndexOutOfBoundsException e) {
            // out of space so all good
        }
    }

    /**
     * Make sure we handle the case where we write too much
     * 
     * @throws Exception
     */
    @Test
    public void testWriteTooMuch() throws Exception {
        final int capacity = "hello world".length() + 1;
        final Buffer buffer = Buffers.createBuffer(capacity);
        try {
            buffer.write("hello world wont fit");
            fail("Expected an IndexOutOfBoundsException");
        } catch (final IndexOutOfBoundsException e) {
            // out of space so all good
        }

        // according to our javadoc, we should not have written
        // nothing to the buffer so it should have been left at the
        // same place we "found" it
        assertThat(buffer.getWritableBytes(), is(capacity));
        assertThat(buffer.getReadableBytes(), is(0));

        buffer.write("hello");
        assertThat(buffer.getWritableBytes(), is(capacity - "hello".length()));
        assertThat(buffer.getReadableBytes(), is("hello".length()));

        buffer.write(" world");
        assertThat(buffer.getWritableBytes(), is(capacity - "hello world".length()));
        assertThat(buffer.getReadableBytes(), is("hello world".length()));

        try {
            buffer.write("too much again");
            fail("Expected an IndexOutOfBoundsException");
        } catch (final IndexOutOfBoundsException e) {
            // out of space so all good
        }
        assertThat(buffer.getWritableBytes(), is(capacity - "hello world".length()));
        assertThat(buffer.getReadableBytes(), is("hello world".length()));
        assertThat(buffer.toString(), is("hello world"));
        assertThat(buffer.readLine().toString(), is("hello world"));
        assertThat(buffer.getReadableBytes(), is(0));
    }

    @Test
    public void testWriteToSmall() throws Exception {
        final Buffer buffer = Buffers.createBuffer(1);
        assertThat(buffer.getWritableBytes(), is(1));
        assertThat(buffer.getReadableBytes(), is(0));
        assertThat(buffer.toString(), is(""));

        buffer.write((byte) 'h');
        assertThat(buffer.getWritableBytes(), is(0));
        assertThat(buffer.getReadableBytes(), is(1));
        assertThat(buffer.toString(), is("h"));
        assertThat(buffer.getByte(0), is((byte) 'h'));

        assertThat(buffer.readByte(), is((byte) 'h'));
        assertThat(buffer.getWritableBytes(), is(0));
        assertThat(buffer.getReadableBytes(), is(0));

        try {
            buffer.write("b");
            fail("Expected an IndexOutOfBoundsException");
        } catch (final IndexOutOfBoundsException e) {
        }

        try {
            buffer.write((byte) 'b');
            fail("Expected an IndexOutOfBoundsException");
        } catch (final IndexOutOfBoundsException e) {
        }

    }

    @Override
    public Buffer createBuffer(final byte[] array) {
        return new ByteBuffer(array);
    }

}
