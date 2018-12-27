package io.pkts.diameter.codegen.primitives;

import io.pkts.diameter.codegen.Typedef;

/**
 * Base interface for our code generation for generating various diameter "primitives".
 */
public interface DiameterPrimitive {

    /**
     * The name of the primitive as it is used in the XML dictionary files.
     *
     * @return
     */
    String getElementName();

    Typedef getTypedef();

    default GavpPrimitive toGavpPrimitive() throws ClassCastException {
        throw new ClassCastException("Unable to cast a " + this.getClass().getName() + " into a " + GavpPrimitive.class.getName());
    }

    default AvpPrimitive toAvpPrimitive() throws ClassCastException {
        throw new ClassCastException("Unable to cast a " + this.getClass().getName() + " into a " + AvpPrimitive.class.getName());
    }

    default TypePrimitive toTypePrimitive() throws ClassCastException {
        throw new ClassCastException("Unable to cast a " + this.getClass().getName() + " into a " + TypePrimitive.class.getName());
    }

    default EnumPrimitive toEnumPrimitive() throws ClassCastException {
        throw new ClassCastException("Unable to cast a " + this.getClass().getName() + " into a " + EnumPrimitive.class.getName());
    }

    default GroupedPrimitive toGroupedPrimitive() throws ClassCastException {
        throw new ClassCastException("Unable to cast a " + this.getClass().getName() + " into a " + GroupedPrimitive.class.getName());
    }

    default TypedefPrimitive toTypedefPrimitive() throws ClassCastException {
        throw new ClassCastException("Unable to cast a " + this.getClass().getName() + " into a " + TypedefPrimitive.class.getName());
    }
}
