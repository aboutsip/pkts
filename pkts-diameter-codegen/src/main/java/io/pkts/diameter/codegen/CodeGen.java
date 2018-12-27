package io.pkts.diameter.codegen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.pkts.diameter.codegen.config.Attributes;
import io.pkts.diameter.codegen.config.CodeConfig;
import io.pkts.diameter.codegen.config.Settings;
import io.pkts.diameter.codegen.primitives.AvpPrimitive;
import io.pkts.diameter.codegen.templates.AvpTemplate;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.pkts.diameter.codegen.PreConditions.ensureArgument;

public class CodeGen {

    private static final Logger logger = LoggerFactory.getLogger(CodeGen.class);

    private static final String DICTIONARY_FILE_NAME = "dictionary.xml";
    private static final String DICTIONARY_DTD_FILE_NAME = "dictionary.dtd";

    private final CodeConfig config;
    private final Path dictionaryDir;
    private final DiameterCollector collector = new DiameterCollector();

    private CodeGen(final CodeConfig config, final Path dictionaryDir) {
        this.config = config;
        this.dictionaryDir = dictionaryDir;
    }

    private void execute() {
        try {
            final Path dictionary = dictionaryDir.resolve(DICTIONARY_FILE_NAME);
            logger.info("Parsing " + dictionary);
            final WiresharkDictionaryReader reader = new WiresharkDictionaryReader(collector);
            reader.parse(dictionary);
            renderAvps();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private void renderAvps() throws IOException, URISyntaxException {
        final Settings settings = config.getAvpSettings();
        final List<AvpPrimitive> avps;
        if (settings.renderAll()) {
            logger.info("Rendering all found AVPs, minus the ones on the exclude list.");
            avps = collector.getAvps().stream().filter(avp -> !settings.isExcluded(avp.getName())).collect(Collectors.toList());
        } else {
            logger.info("Rendering only those AVPs explicitly listed on the \"include\" list");
            avps = collector.getAvps().stream().filter(avp -> settings.isIncluded(avp.getName())).collect(Collectors.toList());
        }

        for (final AvpPrimitive avp : avps) {
            final AvpTemplate template = AvpTemplate.load(avp);
            final Attributes attributes = config.createAvpConfig(avp);
            final String rendered = template.render(attributes.getAttributes());
            save(settings, attributes, rendered);
        }
    }

    private void save(final Settings settings, final Attributes attributes, final String content) throws IOException {
        final Path src = settings.getJavaSrcDir();
        final Path packageDir = src.resolve(attributes.getPackage().replaceAll("\\.", File.separator));
        final Path fullFileName = packageDir.resolve(attributes.getName() + ".java");
        logger.debug("Saving {} as {}", attributes.getName(), fullFileName);
        // System.err.println(content);
        Files.createDirectories(packageDir);
        Files.write(fullFileName, content.getBytes());
    }

    private static ArgumentParser configureParser() {
        final ArgumentParser parser = ArgumentParsers.newFor("codegen").build();
        parser.description("Code generator for diameter");

        // the directory where we expect all the dicionary.xml files to live.
        parser.addArgument("--wireshark")
                .help("Directory where the wireshark diameter dictionary.xml files lives")
                .metavar("<wireshark dir>")
                .required(true);

        // configuration file with all our settings for e.g. where to generate the
        // code, if there are any that should be skipped etc etc.
        parser.addArgument("--config")
                .help("The configuration file")
                .metavar("<config>")
                .required(true);

        return parser;
    }

    /**
     * Ensure that the specified directory has all the various xml files etc.
     *
     * @param dir
     * @return
     * @throws IllegalArgumentException
     */
    private static Path ensureDictionaryDirectory(final String dir) throws IllegalArgumentException {
        final Path root = Paths.get(dir);
        ensureArgument(Files.exists(root), "The given directory doesn't exist (" + root + ")");
        ensureArgument(Files.isDirectory(root), "The given directory is not a directory (" + root + ")");

        final Path altRoot = root.resolve("diameter");

        // if we can't find the dictionary.xml in the given directory, see if it exists in
        // a sub-directory named "diameter" since that is the structure of wireshark.
        for (final Path p : Arrays.asList(root, altRoot)) {
            final Path xml = p.resolve(DICTIONARY_FILE_NAME);
            final Path dtd = p.resolve(DICTIONARY_DTD_FILE_NAME);
            if (Files.exists(xml) && Files.exists(dtd)) {
                return p;
            }
        }

        throw new IllegalArgumentException("Unable to locate the dictionary.xml and " +
                "dictionary.dtd in the given directory. I even checked the subdirectory 'diameter' (" + altRoot + ")");
    }

    private static CodeConfig loadConfig(final String config) throws IOException {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        final Path path = Paths.get(config).toAbsolutePath();
        ensureArgument(Files.exists(path), "The given config file does not exist (" + path + ")");
        ensureArgument(Files.isRegularFile(path), "The given config file is not a regular file (" + path + ")");

        return mapper.readValue(path.toFile(), CodeConfig.class);
    }

    private static Optional<CodeGen> parse(final String... args) {
        final ArgumentParser parser = configureParser();

        try {
            final Namespace result = parser.parseArgs(args);
            final Path dictionaryDir = ensureDictionaryDirectory(result.getString("wireshark"));
            final CodeConfig config = loadConfig(result.getString("config"));
            return Optional.of(new CodeGen(config, dictionaryDir));
        } catch (final ArgumentParserException e) {
            parser.handleError(e);
        } catch (final IllegalArgumentException e) {
            System.err.println("[ERROR] " + e.getMessage());
        } catch (final IOException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public static void main(final String... args) throws Exception {
        parse(args).ifPresent(CodeGen::execute);
    }
}
