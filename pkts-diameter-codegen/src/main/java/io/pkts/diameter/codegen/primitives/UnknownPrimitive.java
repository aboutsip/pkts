package io.pkts.diameter.codegen.primitives;

import io.pkts.diameter.codegen.CodeGenParseException;
import io.pkts.diameter.codegen.DiameterCollector;
import io.pkts.diameter.codegen.builders.AttributeContext;
import io.pkts.diameter.codegen.builders.DiameterSaxBuilder;
import org.xml.sax.SAXException;

/**
 * A primitive we haven't implemented yet. To keep things consistent and without
 * any annoying if-null statements, we'll use this to capture the things we haven't
 * gotten around to do yet.
 */
public interface UnknownPrimitive extends DiameterPrimitive {

    static Builder of(final AttributeContext ctx) throws CodeGenParseException {
        return new Builder(ctx, ctx.getElementName());
    }


    class Builder extends DiameterSaxBuilder.BaseBuilder<UnknownPrimitive> {

        private final String elementName;

        private Builder(final AttributeContext ctx, final String elementName) {
            super(ctx);
            this.elementName = elementName;
        }

        @Override
        public void characters(final char[] ch, final int start, final int length) throws SAXException {
            final char[] copy = new char[length];
            System.arraycopy(ch, start, copy, 0, length);
            System.err.println(new String(copy));
        }

        @Override
        public String getElementName() {
            return elementName;
        }

        @Override
        public boolean isUnknown() {
            return true;
        }

        @Override
        public UnknownPrimitive build(final DiameterCollector ctx) {
            return null;
        }

    }

    class DefaultUnknownPrimitive implements UnknownPrimitive {

        private final String elementName;

        private DefaultUnknownPrimitive(final String elementName) {
            this.elementName = elementName;
        }

        @Override
        public String getElementName() {
            return elementName;
        }
    }
}
