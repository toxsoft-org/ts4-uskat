package org.toxsoft.uskat.core.devapi;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.core.tslib.bricks.ctx.impl.TsContextRefDef.*;
import static org.toxsoft.uskat.core.devapi.ISkResources.*;

import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.utils.plugins.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.impl.*;

/**
 * Загружаемый модуль (плагин) работающий в контексте соединения {@link ISkConnection}.
 * <p>
 * Модуль работает под управлением элемента {@link SkatletUnit} контейнера {@link SkatletBox}.
 *
 * @author mvk
 */
public interface ISkatlet
    extends IStridableParameterized, ICooperativeMultiTaskable, IWorkerComponent {

  /**
   * Параметр контекста {@link #setContext(ITsContextRo)}: API контейнера скатлета.
   * <p>
   * Тип: {@link ISkConnection}.
   */
  ITsContextRefDef<ISkatletSupport> REF_SKATLET_SUPPORT = create( "SkatletSupport", ISkatletSupport.class, //$NON-NLS-1$
      TSID_NAME, STR_SKATLET_SUPPORT, //
      TSID_DESCRIPTION, STR_SKATLET_SUPPORT_D, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_IS_MANDATORY, AV_TRUE //
  );

  /**
   * Параметр контекста {@link #setContext(ITsContextRo)}: общее, разделяемое между скатлетами соединение.
   * <p>
   * Общее соединение могут использовать скатлеты запускаемые при старте сервера или скатлеты которые не регистрируют в
   * соединении новые типы или службы.
   * <p>
   * Тип: {@link ISkConnection}.
   */
  ITsContextRefDef<ISkConnection> REF_SHARED_CONNECTION = create( "SharedConnection", ISkConnection.class, //$NON-NLS-1$
      TSID_NAME, STR_SKATLET_SHARED_CONNECTION, //
      TSID_DESCRIPTION, STR_SKATLET_SHARED_CONNECTION_D, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_IS_MANDATORY, AV_TRUE //
  );

  /**
   * Параметр контекста {@link #setContext(ITsContextRo)}: Порядок загрузки скатлетов. Скатлеты не указанные в списке
   * загружаются последними.
   * <p>
   * В списке указываются идентфикаторы плагинов представляющие скателты {@link IPluginInfo#pluginId()}.
   */
  IDataDef OPDEF_SKATLETS_LOAD_ORDER = DataDef.create( "SkatletLoadOrder", VALOBJ, // //$NON-NLS-1$
      TSID_NAME, STR_SKATLET_LOAD_ORDER, //
      TSID_DESCRIPTION, STR_SKATLET_LOAD_ORDER_D, //
      TSID_DEFAULT_VALUE, avValobj( IStringList.EMPTY ), //
      TSID_IS_MANDATORY, AV_FALSE //
  );

  /**
   * Initializes libraries, register types & service creators.
   *
   * @return {@link ValidationResult} - initialization success result
   */
  ValidationResult initialize();

  /**
   * Set skatlet context.
   *
   * @param aEnviron {@link ITsContextRo} - the execution environment
   * @return {@link ValidationResult} - initialization success result
   */
  ValidationResult setContext( ITsContextRo aEnviron );

}
