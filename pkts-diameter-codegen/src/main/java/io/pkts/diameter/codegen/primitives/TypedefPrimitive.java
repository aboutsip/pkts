package io.pkts.diameter.codegen.primitives;

import io.pkts.diameter.codegen.CodeGenParseException;
import io.pkts.diameter.codegen.DiameterCollector;
import io.pkts.diameter.codegen.builders.AttributeContext;
import io.pkts.diameter.codegen.builders.DiameterSaxBuilder;
import org.xml.sax.SAXException;

import java.util.Optional;

public interface TypedefPrimitive extends DiameterPrimitive {

    /**
     * The name of the XML element.
     */
    String NAME = "typedefn";

    @Override
    default String getElementName() {
        return NAME;
    }

    @Override
    default TypedefPrimitive toTypedefPrimitive() throws ClassCastException {
        return this;
    }

    static Builder of(final AttributeContext ctx) throws CodeGenParseException {
        ctx.ensureElementName(NAME);

        // TODO: should probably be an ENUM
        final String typeName = ctx.getString("type-name");
        final Optional<String> typeParent = ctx.getOptionalString("type-parent");
        return new Builder(ctx, typeName, typeParent);
    }

    class Builder extends DiameterSaxBuilder.BaseBuilder<TypedefPrimitive> {

        private final String type;
        private final Optional<String> parent;

        private Builder(final AttributeContext ctx, final String type, final Optional<String> parent) {
            super(ctx);
            this.type = type;
            this.parent = parent;
        }

        @Override
        public String getElementName() {
            return NAME;
        }

        @Override
        public void characters(final char[] ch, final int start, final int length) throws SAXException {
            final char[] copy = new char[length];
            System.arraycopy(ch, start, copy, 0, length);
            System.err.println(new String(copy));
        }

        /**
         * We do not expect that there is a child attribute to the typedefn element.
         *
         * @param child
         */
        @Override
        public void attachChildBuilder(final DiameterSaxBuilder child) {
            throw createException("Unexpected child element");
        }

        @Override
        public TypedefPrimitive build(final DiameterCollector ctx) {
            return null;
        }

    }
}
