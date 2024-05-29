package org.toxsoft.uskat.core.devapi;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.core.tslib.bricks.ctx.impl.TsContextRefDef.*;

import org.toxsoft.core.tslib.bricks.ICooperativeMultiTaskable;
import org.toxsoft.core.tslib.bricks.IWorkerComponent;
import org.toxsoft.core.tslib.bricks.ctx.ITsContextRefDef;
import org.toxsoft.core.tslib.bricks.ctx.ITsContextRo;
import org.toxsoft.core.tslib.bricks.strid.IStridableParameterized;
import org.toxsoft.core.tslib.bricks.validator.ValidationResult;
import org.toxsoft.uskat.core.connection.ISkConnection;
import org.toxsoft.uskat.core.impl.SkatletBox;
import org.toxsoft.uskat.core.impl.SkatletUnit;

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
   * Параметр контекста {@link #init(ITsContextRo)}: соединение в рамках которого работает скатлет.
   * <p>
   * Тип: {@link ISkConnection}.
   */
  ITsContextRefDef<ISkConnection> REF_SK_CONNECTION = create( "SkConnection", ISkConnection.class, //$NON-NLS-1$
      TSID_NAME, "TODO: ISkConnection name", //
      TSID_DESCRIPTION, "TODO: ISkConnection descr", //
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
