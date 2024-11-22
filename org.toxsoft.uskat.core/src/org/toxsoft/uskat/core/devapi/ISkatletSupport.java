package org.toxsoft.uskat.core.devapi;

import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.impl.*;

/**
 * Поддержка работы скатлетов {@link ISkatlet}.
 *
 * @author mvk
 */
public interface ISkatletSupport {

  /**
   * Возвращает общее, разделяемое между другими скатлетами соединение.
   * <p>
   * Попытка закрыть возвращаемое соединение ({@link ISkConnection#close()}) игнорируется.
   *
   * @return {@link ISkConnection} соединение.
   */
  ISkConnection getSharedConnection();

  /**
   * Создает новое соединение.
   *
   * @param aName String имя соединения.
   * @param aArgs {@link ITsContextRo} дополнительные параметры для создания соединения, например,
   *          {@link ISkCoreConfigConstants#OPDEF_L10N_FILES_DIR}.
   * @return {@link ISkConnection} соединение.
   */
  ISkConnection createConnection( String aName, ITsContextRo aArgs );

  /**
   * Общий журнал скатлетов.
   *
   * @return {@link ILogger} журнал
   */
  ILogger logger();
}
