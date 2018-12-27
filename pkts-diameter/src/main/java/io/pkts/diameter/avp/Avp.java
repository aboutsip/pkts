package io.pkts.diameter.avp;

import io.pkts.diameter.avp.type.DiameterType;
import io.pkts.diameter.avp.type.Enumerated;

/**
 * The difference between this {@link Avp} and the raw version, {@link FramedAvp} is that
 * this one has been fully parsed so that we know what type it is etc. Quite often, you
 * don't want to work with every AVP in a message and as such, we shouldn't waste time to
 * parse them all fully, which this allows you to do. So the normal flow is that most
 * of your code will work with the FramedAvp because you don't care what it is, however, for
 * certain AVPs you do want to convert them to their real fully parsed versions, since it
 * is simply just easier to work with.
 */
public interface Avp<T extends DiameterType> extends FramedAvp {

    T getValue();

    /**
     * Check if this AVP is an enumerated AVP.
     *
     * @return
     */
    default boolean isEnumerated() {
        return false;
    }

    default <E extends Enum<E>> Avp<Enumerated<E>> toEnumerated() throws ClassCastException {
        throw new ClassCastException("Unable to cast a " + this.getClass().getName() + " into a " + Enumerated.class.getName());
    }
}
