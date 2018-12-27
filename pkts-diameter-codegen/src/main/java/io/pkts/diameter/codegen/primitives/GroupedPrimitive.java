package io.pkts.diameter.codegen.primitives;

import io.pkts.diameter.codegen.CodeGenParseException;
import io.pkts.diameter.codegen.DiameterCollector;
import io.pkts.diameter.codegen.Typedef;
import io.pkts.diameter.codegen.builders.AttributeContext;
import io.pkts.diameter.codegen.builders.DiameterSaxBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface GroupedPrimitive extends DiameterPrimitive {

    /**
     * The name of the XML element.
     */
    String NAME = "grouped";

    @Override
    default Typedef getTypedef() {
        return Typedef.GROUPED;
    }

    /**
     * Get the AVPs that is part of this group.
     */
    List<GavpPrimitive> getGroupedAvps();

    @Override
    default String getElementName() {
        return NAME;
    }

    @Override
    default GroupedPrimitive toGroupedPrimitive() throws ClassCastException {
        return this;
    }

    static Builder of(final AttributeContext ctx) throws CodeGenParseException {
        ctx.ensureElementName(NAME);
        return new Builder(ctx);
    }

    class Builder extends DiameterSaxBuilder.BaseBuilder<GroupedPrimitive> {

        /**
         * Those elements that we have builders for and that we should accept.
         */
        private static final List<String> acceptableChildElements = new ArrayList<>();

        static {
            acceptableChildElements.add(GavpPrimitive.NAME);
        }

        private Builder(final AttributeContext ctx) {
            super(ctx);
        }

        @Override
        protected List<String> getKnownChildElements() {
            return acceptableChildElements;
        }

        @Override
        public String getElementName() {
            return NAME;
        }

        @Override
        public GroupedPrimitive build(final DiameterCollector ctx) {
            final Map<String, List<DiameterPrimitive>> primitives = buildChildren(ctx);

            // we only expect one type of child and that's the gavp.
            // Now, that should have already been taken care of but
            // just in case it hasn't, let's check again.
            final List<DiameterPrimitive> children = primitives.get(GavpPrimitive.NAME);
            if (children == null || children.isEmpty()) {
                throw createException("Expected at least one 'gavp' as part of the '" + NAME + "' element");
            }

            if (primitives.size() != 1) {
                throw createException("Only expected child elements of '" + GavpPrimitive.NAME + "' but found others too");
            }

            return () -> children.stream().map(DiameterPrimitive::toGavpPrimitive).collect(Collectors.toList());

        }
    }

}
