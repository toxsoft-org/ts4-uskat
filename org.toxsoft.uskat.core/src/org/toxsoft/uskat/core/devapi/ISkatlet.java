package org.toxsoft.uskat.core.devapi;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.core.tslib.bricks.ctx.impl.TsContextRefDef.*;
import static org.toxsoft.uskat.core.devapi.ISkResources.*;

import org.toxsoft.core.tslib.bricks.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.bricks.validator.*;
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
   * Параметр контекста {@link #init(ITsContextRo)}: API контейнера скатлета.
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
   * Initializes the unit to work in the environment specified as an argument.
   * <p>
   * Once successfully initialized (that is, put in execution environment) the unit can not initialized again. It may be
   * started/stopped but not initialized.
   *
   * @param aEnviron {@link ITsContextRo} - the execution environment
   * @return {@link ValidationResult} - initialization success result
   */
  ValidationResult init( ITsContextRo aEnviron );

}
