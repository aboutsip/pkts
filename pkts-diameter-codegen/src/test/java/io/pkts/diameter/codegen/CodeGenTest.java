package io.pkts.diameter.codegen;

import org.junit.Test;

public class CodeGenTest {

    @Test
    public void testReadXml() throws Exception {
        final DiameterContext collector = new DiameterContext();
        final WiresharkDictionaryReader reader = new WiresharkDictionaryReader(collector);
        System.err.println(collector.getAvps().size());
        // collector.getAvps().forEach(avp -> {
            // System.out.println("\"" + avp.getName() + "\"");
        // });
    }
}
