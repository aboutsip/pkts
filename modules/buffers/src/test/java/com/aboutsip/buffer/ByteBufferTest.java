/**
 * 
 */
package com.aboutsip.buffer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

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

    @Test
    public void testEquals() throws Exception {
        final Buffer b1 = createBuffer("hello world");
        final Buffer b2 = createBuffer("hello world");
        assertThat(b1, is(b2));

        final Buffer b3 = createBuffer("hello not world");
        assertThat(b1, is(not(b3)));
        assertThat(b2, is(not(b3)));

        // because the way we do equals right now when one of the
        // buffers has read a head they are no longer equal.
        // One motivation is because the bytes that have been
        // consumed actually can be discarded...
        b2.readByte();
        assertThat(b1, is(not(b2)));

        // because of this, if we now read a head in both
        // b1 and b3 and only leave the "world" portion left
        // then all of a sudden b1 and b3 actually are equal
        b1.readBytes(6);
        b3.readBytes(10);
        assertThat(b1, is(b3));
    }

    public Buffer createBuffer(final String s) {
        return createBuffer(s.getBytes());
    }


    @Override
    public Buffer createBuffer(final byte[] array) {
        return new ByteBuffer(array);
    }

}
