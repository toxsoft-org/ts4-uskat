package org.toxsoft.uskat.sysext.alarms.impl;

import java.io.Serializable;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.uskat.sysext.alarms.api.flacon.ISkAlarmFlacon;

/**
 * Хранение информации о параметрах одного аларма( разнотипные значения сущностей в одном флаконе).
 * <p>
 *
 * @author goga, dima
 */
public class SkAlarmFlacon
    implements ISkAlarmFlacon, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * параметры аларма
   */
  IOptionSet params;

  @Override
  public IOptionSet params() {
    return params;
  }

  /**
   * Создает флакон по параметрам
   *
   * @param aParams параметры
   */
  public SkAlarmFlacon( IOptionSet aParams ) {
    super();
    params = aParams;
  }

  /**
   * Пустой констурктор для сериализации
   */
  public SkAlarmFlacon() {
    super();
  }
}
