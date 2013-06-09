/**
 * 
 */
package io.pkts.filters;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import io.pkts.filters.FilterFactory;
import io.pkts.filters.SipCallIdFilter;

import org.junit.Before;
import org.junit.Test;

/**
 * @author jonas@jonasborjesson.com
 * 
 */
public class FilterFactoryTest {

    private FilterFactory factory;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        this.factory = FilterFactory.getInstance();
    }

    @Test
    public void testCreateSimpleSipCallIdFilter() {
        assertThat(((SipCallIdFilter) this.factory.createFilter("sip.Call-ID == 1234")).getCallId(), is("1234"));
        assertThat(((SipCallIdFilter) this.factory.createFilter("sip.Call-ID ==1234")).getCallId(), is("1234"));
        assertThat(((SipCallIdFilter) this.factory.createFilter("sip.Call-ID == hello   ")).getCallId(), is("hello"));
    }

}
