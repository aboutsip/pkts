package io.pkts.diameter.codegen.primitives;

import io.pkts.diameter.avp.type.DiameterType;
import io.pkts.diameter.codegen.CodeGenParseException;
import io.pkts.diameter.codegen.DiameterContext;
import io.pkts.diameter.codegen.builders.AttributeContext;
import io.pkts.diameter.codegen.builders.DiameterSaxBuilder;

public interface TypePrimitive extends DiameterPrimitive {

    /**
     * The name of the XML element.
     */
    String NAME = "type";

    DiameterType.Type getType();

    @Override
    default String getElementName() {
        return NAME;
    }

    @Override
    default TypePrimitive toTypePrimitive() throws ClassCastException {
        return this;
    }

    static Builder of(final AttributeContext ctx) throws CodeGenParseException {
        ctx.ensureElementName(NAME);
        final DiameterType.Type type = DiameterType.Type.fromName(ctx.getString("type-name"));
        return new Builder(ctx, type);
    }

    class Builder extends DiameterSaxBuilder.BaseBuilder<TypePrimitive> {

        private final DiameterType.Type type;

        private Builder(final AttributeContext ctx, final DiameterType.Type type) {
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
            throw createException("Unexpected child element");
        }

        @Override
        public TypePrimitive build(final DiameterContext ctx) {
            return () -> type;
        }
    }
}
