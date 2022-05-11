package org.toxsoft.uskat.sysext.alarms.api.generator;

import org.toxsoft.core.tslib.bricks.strid.IStridable;
import org.toxsoft.core.tslib.utils.ICloseable;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Поставщик данных для формирования алармов
 *
 * @author mvk
 */
public interface ISkAlarmDataProvider
    extends IStridable, ICloseable {

  /**
   * Слушатель поставщика данных
   */
  interface IAlarmDataProviderListener {

    /**
     * Изменилось данные поставщика
     *
     * @param aProvider {@link ISkAlarmDataProvider} поставщика данных
     */
    void onUpdate( ISkAlarmDataProvider aProvider );
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
