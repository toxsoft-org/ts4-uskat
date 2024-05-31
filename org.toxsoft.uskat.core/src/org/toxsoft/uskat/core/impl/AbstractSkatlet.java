package org.toxsoft.uskat.core.impl;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.ctx.ITsContextRo;
import org.toxsoft.core.tslib.bricks.strid.impl.StridableParameterized;
import org.toxsoft.core.tslib.bricks.validator.ValidationResult;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.core.connection.ISkConnection;
import org.toxsoft.uskat.core.devapi.ISkatlet;

/**
 * Абстрактная реализация скатлета
 *
 * @author mvk
 */
public abstract class AbstractSkatlet
    extends StridableParameterized
    implements ISkatlet {

  private ISkConnection connection;
  private ILogger       logger;

  /**
   * Constructor.
   *
   * @param aId String - the ID (IDpath)
   * @param aParams {@link IOptionSet} - {@link #params()} initial values
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException ID is not an IDpath
   */
  public AbstractSkatlet( String aId, IOptionSet aParams ) {
    super( aId, aParams );
  }

  // ------------------------------------------------------------------------------------
  // API для наследников
  //
  /**
   * Возвращает соединение с которым работает скатлет
   *
   * @return {@link ISkConnection} соединение
   */
  protected final ISkConnection connection() {
    return connection;
  }

  /**
   * Возвращает журнал с которым работает скатлет
   *
   * @return {@link ILogger} журнал
   */
  protected final ILogger logger() {
    return logger;
  }

  // ------------------------------------------------------------------------------------
  // ISkatlet
  //
  @Override
  public final ValidationResult init( ITsContextRo aEnviron ) {
    connection = ISkatlet.REF_SK_CONNECTION.getRef( aEnviron );
    logger = ISkatlet.REF_LOGGER.getRef( aEnviron );
    return doInit( aEnviron );
  }

  /**
   * Initializes the unit to work in the environment specified as an argument.
   * <p>
   * Once successfully initialized (that is, put in execution environment) the unit can not initialized again. It may be
   * started/stopped but not initialized.
   *
   * @param aEnviron {@link ITsContextRo} - the execution environment
   * @return {@link ValidationResult} - initialization success result
   */
  protected abstract ValidationResult doInit( ITsContextRo aEnviron );
}
