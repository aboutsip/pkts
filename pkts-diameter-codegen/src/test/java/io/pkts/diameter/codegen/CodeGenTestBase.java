package io.pkts.diameter.codegen;

import org.junit.Before;

import java.nio.file.Path;
import java.nio.file.Paths;

public class CodeGenTestBase {

    private DiameterCollector collector;

    @Before
    public void setUp() {
        collector = new DiameterCollector();
    }

    /**
     * Load the XML given by the resource.
     *
     * @param resource
     * @return
     */
    public DiameterCollector load(final String resource) throws Exception {
        // final InputStream stream = CodeGenTestBase.class.getResourceAsStream(resource);
        final Path p = Paths.get(CodeGenTestBase.class.getResource(resource).toURI());
        final WiresharkDictionaryReader reader = new WiresharkDictionaryReader(collector);
        reader.parse(p);
        return collector;
    }


}
