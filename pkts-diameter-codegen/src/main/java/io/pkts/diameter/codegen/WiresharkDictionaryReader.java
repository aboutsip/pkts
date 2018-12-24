package io.pkts.diameter.codegen;

import io.pkts.diameter.codegen.builders.AttributeContext;
import io.pkts.diameter.codegen.builders.DiameterSaxBuilder;
import io.pkts.diameter.codegen.primitives.ApplicationPrimitive;
import io.pkts.diameter.codegen.primitives.AvpPrimitive;
import io.pkts.diameter.codegen.primitives.EnumPrimitive;
import io.pkts.diameter.codegen.primitives.GavpPrimitive;
import io.pkts.diameter.codegen.primitives.GroupedPrimitive;
import io.pkts.diameter.codegen.primitives.TypePrimitive;
import io.pkts.diameter.codegen.primitives.TypedefPrimitive;
import io.pkts.diameter.codegen.primitives.UnknownPrimitive;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.Function;

public class WiresharkDictionaryReader {

    private final SAXParserFactory saxFactory = SAXParserFactory.newInstance();

    public WiresharkDictionaryReader() throws Exception {
        saxFactory.setValidating(true);
        final SAXParser saxParser = saxFactory.newSAXParser();
        final WiresharkXmlHandler handler = new WiresharkXmlHandler();
        final String home = "/home/jonas/development/3rd-party/wireshark/diameter";
        final String dictionary = home + "/dictionary.xml";
        saxParser.parse(new File(dictionary), handler);
    }

    private static class WiresharkXmlHandler extends DefaultHandler {

        private final Stack<DiameterSaxBuilder> builders = new Stack<>();

        /**
         * just ignore these elements from the xml. No need to create builders for them.
         */
        private final List<String> ignore = new ArrayList<>();

        private final Map<String, Function<AttributeContext, DiameterSaxBuilder>> creators = new HashMap<>();

        /**
         * Use this one for unknown/un-handled/yet-to-be-handled attributes.
         */
        private final Function<AttributeContext, DiameterSaxBuilder> defaultBuilder = UnknownPrimitive::of;

        /**
         * Keeps track of where, and in which, XML file we are. Mainly used for error
         * reporting so the exceptions contain the file and location. Makes debugging
         * quite a bit easier.
         */
        private Locator locator;

        private final DiameterContext ctx;

        @Override
        public void setDocumentLocator(final Locator locator) {
            this.locator = locator;
        }

        public WiresharkXmlHandler() {
            // Put all the known builders here.
            creators.put(ApplicationPrimitive.NAME, ApplicationPrimitive::of);
            creators.put(AvpPrimitive.NAME, AvpPrimitive::of);
            creators.put(TypePrimitive.NAME, TypePrimitive::of);
            creators.put(TypedefPrimitive.NAME, TypedefPrimitive::of);
            creators.put(EnumPrimitive.NAME, EnumPrimitive::of);
            creators.put(GroupedPrimitive.NAME, GroupedPrimitive::of);
            creators.put(GavpPrimitive.NAME, GavpPrimitive::of);

            // ignore these xml elements
            ignore.add("dictionary");
            ignore.add("base");

            ctx = new DiameterContext();
        }

        @Override
        public void startDocument() throws SAXException {
            final AttributeContext empty = new AttributeContext("root", locator, null);
            builders.push(new DiameterRootBuilder(empty));
        }

        @Override
        public void endDocument() throws SAXException {
            final DiameterSaxBuilder root = builders.pop();
            if (!builders.isEmpty()) {
                throw new IllegalStateException("Seems like we didnt push/pop correctly");
            }
        }

        @Override
        public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
            final String element = qName.toLowerCase();
            if (ignore.contains(element)) {
                return;
            }
            final AttributeContext ctx = new AttributeContext(element, locator, attributes);
            final DiameterSaxBuilder builder = creators.getOrDefault(element, defaultBuilder).apply(ctx);
            builders.peek().attachChildBuilder(builder);
            builders.push(builder);
        }

        @Override
        public void characters(final char[] ch, final int start, final int length) throws SAXException {
            builders.peek().characters(ch, start, length);
        }

        @Override
        public void endElement(final String uri, final String localName, final String qName) throws SAXException {
            final String element = qName.toLowerCase();
            if (ignore.contains(element)) {
                return;
            }

            builders.pop().build(ctx);
        }

    }

}
