package io.pkts.diameter.codegen;

import io.pkts.diameter.codegen.config.CodeConfig;
import org.junit.Test;

public class CodeGenTest {

    @Test
    public void testReadXml() throws Exception {
        final DiameterCollector collector = new DiameterCollector();
        final WiresharkDictionaryReader reader = new WiresharkDictionaryReader(collector);

        final String home = "/home/jonas/development/3rd-party/wireshark/diameter";
        final String dictionary = home + "/dictionary.xml";
        reader.parse(dictionary);

        final CodeConfig config = CodeConfig.of().build();
        final Renderer renderer = new Renderer(config, collector);
        renderer.renderAvp("Origin-Host");
    }
}
