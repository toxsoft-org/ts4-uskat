package org.toxsoft.uskat.core.impl;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.ctx.ITsContextRo;
import org.toxsoft.core.tslib.bricks.strid.impl.StridableParameterized;
import org.toxsoft.core.tslib.bricks.validator.ValidationResult;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.core.connection.ISkConnection;
import org.toxsoft.uskat.core.devapi.ISkatlet;

/**
 * Abstract skatlet implementation.
 *
 * @author mvk
 */
public abstract class AbstractSkatlet
    extends StridableParameterized
    implements ISkatlet {

  private ITsContextRo environ;

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
   * Skatlet's connection.
   *
   * @return {@link ISkConnection} connection.
   * @throws TsIllegalStateRtException scatlet is not initialized.
   */
  protected final ISkConnection connection() {
    TsIllegalStateRtException.checkNull( environ );
    return ISkatlet.REF_SK_CONNECTION.getRef( environ );
  }

  /**
   * Skatlet's logger.
   *
   * @return {@link ILogger} logger.
   */
  protected final ILogger logger() {
    TsIllegalStateRtException.checkNull( environ );
    return ISkatlet.REF_LOGGER.getRef( environ );
  }

  // ------------------------------------------------------------------------------------
  // ISkatlet
  //
  @Override
  public final ValidationResult init( ITsContextRo aEnviron ) {
    environ = aEnviron;
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
