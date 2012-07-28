/**
 * 
 */
package com.aboutsip.buffer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

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
        final Buffer a = createBuffer("hello");
        final Buffer b = createBuffer("hello");
        final Buffer c = createBuffer("world");
        assertThat(a, is(b));
        assertThat(b, is(a));
        assertThat(c, is(c));
        assertThat(c, not(b));
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

        b1.setByte(0, (byte)0xFF);
        assertThat(b1.getByte(0), is((byte) 0xFF));
        assertThat(buffer.getByte(10), is((byte) 0xFF));
    }

    @Override
    public Buffer createBuffer(final byte[] array) {
        return new ByteBuffer(array);
    }

}
