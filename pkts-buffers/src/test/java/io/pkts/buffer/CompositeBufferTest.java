/**
 * 
 */
package io.pkts.buffer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * @author jonas@jonasborjesson.com
 */
public class CompositeBufferTest {

    @Test
    public void testBasicStuff() throws Exception {
        final Buffer buffer = Buffers.wrap(Buffers.wrap("hello"), Buffers.wrap("world"));
        assertThat(buffer.getReadableBytes(), is(10));
        assertThat(buffer.getWritableBytes(), is(0));
        assertThat(buffer.readBytes(5).toString(), is("hello"));
        assertThat(buffer.readBytes(5).toString(), is("world"));
    }

    /**
     * It is possible to create a new composite buffer where one of the other
     * buffers is null.
     * 
     * @throws Exception
     */
    @Test
    public void testCreateOneIsNull() throws Exception {
        final Buffer hello = Buffers.wrap("hello");
        Buffer buffer = Buffers.wrap(hello, null);
        assertThat(buffer.getReadableBytes(), is(5));
        assertThat(buffer.getWritableBytes(), is(0));
        assertThat(buffer.readBytes(5).toString(), is("hello"));
        assertThat(buffer.getReadableBytes(), is(0));

        // the indices of buffer hello should not have been affected
        assertThat(hello.getReadableBytes(), is(5));
        assertThat(hello.readBytes(5).toString(), is("hello"));
        assertThat(hello.getReadableBytes(), is(0));

        // just make sure that the we can have the buffer
        // in "second" place as well
        buffer = Buffers.wrap(null, Buffers.wrap("world"));
        assertThat(buffer.getReadableBytes(), is(5));
        assertThat(buffer.getWritableBytes(), is(0));
        assertThat(buffer.readBytes(5).toString(), is("world"));
    }

    /**
     * 
     * @throws Exception
     */
    @Test
    public void testCreateBothNull() throws Exception {
        final Buffer buffer = Buffers.wrap(null, null);
        assertThat(buffer.isEmpty(), is(true));
    }

}
