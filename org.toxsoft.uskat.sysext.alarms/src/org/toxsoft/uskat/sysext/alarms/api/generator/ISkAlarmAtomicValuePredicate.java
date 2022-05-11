package org.toxsoft.uskat.sysext.alarms.api.generator;

import java.util.function.Predicate;

import org.toxsoft.core.tslib.av.IAtomicValue;

/**
 * Условие на атомарное значение
 *
 * @author mvk
 */
public interface ISkAlarmAtomicValuePredicate
    extends Predicate<IAtomicValue> {
  // nop
}
