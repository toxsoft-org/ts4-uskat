package org.toxsoft.uskat.core.devapi;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.utils.errors.*;
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
   * Возвращает значение параметра из информации о сервере.
   *
   * @param aParamId String идентификатор параметра.
   * @param aDefaultValue {@link IAtomicValue} значение по умолчанию
   * @return aParamValue {@link IAtomicValue} значение параметра
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  IAtomicValue getBackendInfoParam( String aParamId, IAtomicValue aDefaultValue );

  /**
   * Установить значение параметра в информации о сервере.
   * <p>
   * Если значение параметра уже было в информации о сервере, то его значение переписывается.
   *
   * @param aParamId String идентификатор параметра.
   * @param aParamValue {@link IAtomicValue} значение параметра
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  void setBackendInfoParam( String aParamId, IAtomicValue aParamValue );

  /**
   * Создает новое соединение.
   *
   * @param aName String имя соединения.
   * @param aArgs {@link ITsContextRo} дополнительные параметры для создания соединения, например,
   *          {@link ISkCoreConfigConstants#OPDEF_L10N_FILES_DIR}.
   * @return {@link ISkConnection} соединение.
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  ISkConnection createConnection( String aName, ITsContextRo aArgs );

  /**
   * Общий журнал скатлетов.
   *
   * @return {@link ILogger} журнал
   */
  ILogger logger();
}
