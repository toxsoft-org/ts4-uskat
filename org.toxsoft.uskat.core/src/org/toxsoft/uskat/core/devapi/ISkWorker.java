package org.toxsoft.uskat.core.devapi;

import org.toxsoft.core.tslib.bricks.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Компонент обработки данных uskat.
 *
 * @author mvk
 */
public interface ISkWorker
    extends IStridable, ICooperativeWorkerComponent {

  /**
   * Установить контекст компонента.
   * <p>
   * Вызывается после конструктора компонента, но до вызова {@link ISkWorker#start()}.
   *
   * @param aContext {@link ITsContext} контекст компонента
   * @throws TsNullArgumentRtException аргумент = null
   */
  void setContext( ITsContextRo aContext );

  /**
   * Установить конфигурацию компонента.
   * <p>
   * Первый раз вызывается после {@link ISkWorker#setContext(ITsContextRo)}, но до вызова
   * {@link ISkWorker#start()}. Последующие вызывы метода определяеются логикой клиента.
   *
   * @param aConfiguration {@link ISkWorkerConfig} конфигурация компонента
   * @throws TsNullArgumentRtException аргумент = null
   */
  void setConfiguration( ISkWorkerConfig aConfiguration );

}
