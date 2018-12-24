package io.pkts.diameter.codegen.primitives;

import io.pkts.diameter.codegen.CodeGenParseException;
import io.pkts.diameter.codegen.DiameterContext;
import io.pkts.diameter.codegen.builders.AttributeContext;
import io.pkts.diameter.codegen.builders.DiameterSaxBuilder;

public interface TypePrimitive extends DiameterPrimitive {

    /**
     * The name of the XML element.
     */
    String NAME = "type";

    @Override
    default String getElementName() {
        return NAME;
    }

    static Builder of(final AttributeContext ctx) throws CodeGenParseException {
        ctx.ensureElementName(NAME);

        // TODO: should probably be an ENUM
        final String type = ctx.getString("type-name");
        return new Builder(ctx, type);
    }

    class Builder extends DiameterSaxBuilder.BaseBuilder<TypePrimitive> {

        private final String type;

        private Builder(final AttributeContext ctx, final String type) {
            super(ctx);
            this.type = type;
        }

        @Override
        public String getElementName() {
            return NAME;
        }

        /**
         * We do not expect that there is a child attribute to the typedefn element.
         *
         * @param child
         */
        @Override
        public void attachChildBuilder(final DiameterSaxBuilder child) {
            throwException("Unexpected child element");
        }

        @Override
        public TypePrimitive build(final DiameterContext ctx) {
            return null;
        }
    }
}
