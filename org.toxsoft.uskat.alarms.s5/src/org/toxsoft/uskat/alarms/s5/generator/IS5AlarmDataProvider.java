package org.toxsoft.uskat.alarms.s5.generator;

import org.toxsoft.core.tslib.bricks.strid.IStridable;
import org.toxsoft.core.tslib.utils.ICloseable;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Поставщик данных для формирования алармов
 *
 * @author mvk
 */
public interface IS5AlarmDataProvider
    extends IStridable, ICloseable {

  /**
   * Слушатель поставщика данных
   */
  interface IAlarmDataProviderListener {

    /**
     * Изменились данные поставщика
     *
     * @param aProvider {@link IS5AlarmDataProvider} поставщика данных
     */
    void onUpdate( IS5AlarmDataProvider aProvider );
  }

  /**
   * Добавить слушателя поставщика данных
   * <p>
   * Если слушатель уже добавлен, то ничего не делает
   *
   * @param aListener {@link IAlarmDataProviderListener} слушатель
   * @throws TsNullArgumentRtException аргумент = null
   */
  void addProviderListener( IAlarmDataProviderListener aListener );

  /**
   * Удалить слушателя поставщика данных
   * <p>
   * Если слушатель не зарегистрирован, то ничего не делает
   *
   * @param aListener {@link IAlarmDataProviderListener} слушатель
   * @throws TsNullArgumentRtException аргумент = null
   */
  void removeProviderListener( IAlarmDataProviderListener aListener );
}
