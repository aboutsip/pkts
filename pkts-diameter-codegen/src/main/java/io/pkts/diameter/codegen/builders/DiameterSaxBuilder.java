package io.pkts.diameter.codegen.builders;

import io.pkts.diameter.codegen.CodeGenParseException;
import io.pkts.diameter.codegen.DiameterCollector;
import io.pkts.diameter.codegen.Typedef;
import io.pkts.diameter.codegen.primitives.ApplicationPrimitive;
import io.pkts.diameter.codegen.primitives.AvpPrimitive;
import io.pkts.diameter.codegen.primitives.DiameterPrimitive;
import io.pkts.diameter.codegen.primitives.EnumPrimitive;
import io.pkts.diameter.codegen.primitives.GavpPrimitive;
import io.pkts.diameter.codegen.primitives.GroupedPrimitive;
import io.pkts.diameter.codegen.primitives.TypePrimitive;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public interface DiameterSaxBuilder<T extends DiameterPrimitive> {

    void characters(final char[] ch, final int start, final int length) throws SAXException;

    void attachChildBuilder(DiameterSaxBuilder<T> child) throws CodeGenParseException;

    /**
     * The name of the XML element for which this builder applies.
     *
     * @return
     */
    String getElementName();

    default boolean isAvp() {
        return AvpPrimitive.NAME.equals(getElementName());
    }

    default boolean isApplication() {
        return ApplicationPrimitive.NAME.equals(getElementName());
    }

    default boolean isType() {
        return TypePrimitive.NAME.equals(getElementName());
    }

    default boolean isUnknown() {
        return false;
    }

    T build(DiameterCollector ctx);

    abstract class BaseBuilder<T extends DiameterPrimitive> implements DiameterSaxBuilder<T> {

        private final AttributeContext ctx;

        private final Map<String, List<DiameterSaxBuilder>> children = new HashMap<>();

        protected BaseBuilder(final AttributeContext ctx) {
            this.ctx = ctx;
        }

        protected List<String> getKnownChildElements() {
            // perhaps a bit odd but if there is no known list then we assume
            // accept all. Result of gradually building up the sax builders
            // while learning diameter as a protocol and the dictionary xml files.
            return null;
        }

        protected CodeGenParseException createException(final String msg) {
            return new CodeGenParseException(ctx.getLocator(), msg);
        }

        protected Map<String, List<DiameterPrimitive>> buildChildren(final DiameterCollector ctx) {

            final Map<String, List<DiameterPrimitive>> builtChildren = new HashMap<>();
            children.entrySet().forEach(entry -> {
                builtChildren.put(entry.getKey(),
                        entry.getValue().stream().map(b -> b.build(ctx)).collect(Collectors.toList()));
            });
            return builtChildren;
        }

        /**
         * Sub-builders can decide whether or not they want to rejet the child element (builder).
         * The sub-builder can do so silently by just returning false, or loudly by throwing an
         * exception. Use exception when you do not expect a particular child-element and you want
         * the world to know so that we can either fix the dictionary.xml file or add the missing
         * code.
         *
         * @param child
         * @return
         */
        protected boolean acceptChild(final DiameterSaxBuilder child) throws CodeGenParseException {

            if (getKnownChildElements() == null || getKnownChildElements().contains(child.getElementName())) {
                return true;
            }

            // ok, unknown element for which we do not have a policy (accept or silently ignore).
            // complain...
            throw createException("Un-acceptable child element '" + child.getElementName()
                    + "'. Please either fix the XML file or add missing code to accept this child element");
        }

        @Override
        public void characters(final char[] ch, final int start, final int length) throws SAXException {
            final char[] que = new char[length];
            System.arraycopy(ch, start, que, 0, length);
            System.err.println("-->" + (new String(que)) + "<---");
            throw new CodeGenParseException(ctx.getLocator(), "Did not expect to get any additional characters");
        }

        @Override
        public void attachChildBuilder(final DiameterSaxBuilder child) throws CodeGenParseException {
            if (acceptChild(child)) {
                children.computeIfAbsent(child.getElementName(), key -> new ArrayList<>()).add(child);
            }
        }

        protected boolean isNotEmpty(final List list) {
            return list != null && !list.isEmpty();
        }

        protected List<GavpPrimitive> getGavps(final Map<String, List<DiameterPrimitive>> map) {
            return map.getOrDefault(GavpPrimitive.NAME, Collections.emptyList())
                    .stream()
                    .map(DiameterPrimitive::toGavpPrimitive).collect(Collectors.toList());
        }

        protected List<TypePrimitive> getTypes(final Map<String, List<DiameterPrimitive>> map) {
            return map.getOrDefault(TypePrimitive.NAME, Collections.emptyList())
                    .stream()
                    .map(DiameterPrimitive::toTypePrimitive).collect(Collectors.toList());
        }

        protected Optional<Typedef> getType(final Map<String, List<DiameterPrimitive>> map) {
            final List<TypePrimitive> types = getTypes(map);
            if (types.size() > 1) {
                throw createException("There can only be one type element");
            }
            return types.isEmpty() ? Optional.empty() : Optional.of(types.get(0).getTypedef());
        }

        protected List<GroupedPrimitive> getGroupeds(final Map<String, List<DiameterPrimitive>> map) {
            return map.getOrDefault(GroupedPrimitive.NAME, Collections.emptyList())
                    .stream()
                    .map(DiameterPrimitive::toGroupedPrimitive).collect(Collectors.toList());
        }

        protected Optional<GroupedPrimitive> getGrouped(final Map<String, List<DiameterPrimitive>> map) {
            final List<GroupedPrimitive> groups = getGroupeds(map);
            if (groups.size() > 1) {
                throw createException("There can only be one grouped element");
            }

            return groups.isEmpty() ? Optional.empty() : Optional.of(groups.get(0));
        }

        protected List<EnumPrimitive> getEnums(final Map<String, List<DiameterPrimitive>> map) {
            try {
                return map.getOrDefault(EnumPrimitive.NAME, Collections.emptyList())
                        .stream()
                        .map(DiameterPrimitive::toEnumPrimitive).collect(Collectors.toList());
            } catch (final NullPointerException e) {
                throw e;
            }
        }
    }

}
