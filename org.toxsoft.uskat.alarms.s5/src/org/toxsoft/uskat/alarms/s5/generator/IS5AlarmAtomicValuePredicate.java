package org.toxsoft.uskat.alarms.s5.generator;

import java.util.function.Predicate;

import org.toxsoft.core.tslib.av.IAtomicValue;

/**
 * Условие на атомарное значение
 *
 * @author mvk
 */
public interface IS5AlarmAtomicValuePredicate
    extends Predicate<IAtomicValue> {
  // nop
}
