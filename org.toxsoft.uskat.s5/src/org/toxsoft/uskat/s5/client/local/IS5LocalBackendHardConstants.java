package org.toxsoft.uskat.s5.client.local;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.impl.DataDef.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.s5.client.local.IS5Resources.*;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.av.metainfo.IDataDef;
import org.toxsoft.core.tslib.bricks.ctx.ITsContextRo;

import ru.uskat.backend.ISkFrontendRear;
import ru.uskat.core.api.users.ISkSession;

/**
 * Константы по умолчанию определяющие локальное подключение к s5-серверу.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
public interface IS5LocalBackendHardConstants {

  // ------------------------------------------------------------------------------------
  // Опции
  //
  /**
   * String prefix of the all s5 client identifiers.
   */
  String S5_LOCAL_CLIENT_PREFIX = "s5.client.local.";

  /**
   * Опция : Имя модуля создающего подключение к серверу
   * <p>
   * Тип: {@link EAtomicType#STRING}
   * <p>
   * Используется:
   * <ul>
   * <li>{@link S5LocalConnectionSingleton#createBackend(ISkFrontendRear, ITsContextRo)};</li>
   * <li>{@link ISkSession#connectionCreationParams()}.</li>
   * </ul>
   */
  IDataDef OP_LOCAL_MODULE = create( S5_LOCAL_CLIENT_PREFIX + "module", STRING, //
      TSID_NAME, STR_N_LOCAL_MODULE, //
      TSID_DESCRIPTION, STR_D_LOCAL_MODULE, //
      TSID_IS_MANDATORY, AV_TRUE, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, AV_STR_EMPTY );

  /**
   * Опция : Имя узла кластера на котором работает модуль {@link #OP_LOCAL_MODULE}
   * <p>
   * Тип: {@link EAtomicType#STRING}
   * <p>
   * Используется:
   * <ul>
   * <li>{@link S5LocalConnectionSingleton#createBackend(ISkFrontendRear, ITsContextRo)};</li>
   * <li>{@link ISkSession#connectionCreationParams()}.</li>
   * </ul>
   */
  IDataDef OP_LOCAL_NODE = create( S5_LOCAL_CLIENT_PREFIX + "node", STRING, //
      TSID_NAME, STR_N_LOCAL_NODE, //
      TSID_DESCRIPTION, STR_D_LOCAL_NODE, //
      TSID_IS_MANDATORY, AV_TRUE, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, AV_STR_EMPTY );
}
