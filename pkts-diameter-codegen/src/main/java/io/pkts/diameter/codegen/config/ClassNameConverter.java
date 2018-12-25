package io.pkts.diameter.codegen.config;

import io.pkts.diameter.codegen.primitives.DiameterPrimitive;

/**
 * Simple interface that takes the name of a diameter element, such as an AVP, a command or application,
 * and converts the name, which usually comes the dictionary xml files, into a Java class name.
 */
public interface ClassNameConverter {

    static ClassNameConverter defaultConverter() {
        return new DefaultClassNameConverter();
    }

    String convert(DiameterPrimitive primitive);

    String convert(String name);

    class DefaultClassNameConverter implements ClassNameConverter {

        /**
         * Default rule is simply of course camel case, no '-' and also, I don't like the
         * class name that has abbreviations and those are kept in upper case. E.g. the AVP
         * "Outgoing-Trunk-Group-ID" may be named OutgoingTrunkGroupID but this one will convert it
         * to OutgoingTrunkGroupId, note how the last ID is "Id", i.e. capital 'i' but lowercase 'd'.
         *
         * @param primitive
         * @return
         */
        @Override
        public String convert(final DiameterPrimitive primitive) {
            return convert(primitive.toAvpPrimitive().getName());
        }

        @Override
        public String convert(final String name) {
            final String origName = name.toLowerCase();
            final char[] className = new char[origName.length()];
            int index = 0;
            boolean toUpperCase = true;

            for (int i = 0; i < origName.length(); ++i) {
                final char ch = origName.charAt(i);

                // ensure that the first character isn't an illegal java classname one.
                // if it is, translate if if we have a good translation and if not, complain.
                // If you feel that you have a good translation, just add code here.
                if (i == 0 && Character.isDigit(ch)) {
                    if (ch == '3') {
                        className[index++] = 'T';
                    } else {
                        throw new IllegalArgumentException("A Java class name cannot start " +
                                "with a digit and I currently do not have a good translation for the digit " + ch +
                                ". If you have one, please update the code in "
                                + ClassNameConverter.class.getCanonicalName());
                    }
                    toUpperCase = false;
                    continue;
                }

                // next should be upper case.
                if (ch == '-' || ch == '_') {
                    toUpperCase = true;
                    continue;
                }

                className[index++] = toUpperCase ? Character.toUpperCase(ch) : ch;
                toUpperCase = false;
            }

            final char[] result = new char[index];
            System.arraycopy(className, 0, result, 0, index);
            return new String(result);
        }
    }

}
