package org.toxsoft.uskat.core.impl;

import static org.toxsoft.uskat.core.impl.ISkResources.*;

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
public abstract class SkatletBase
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
  public SkatletBase( String aId, IOptionSet aParams ) {
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
  protected ValidationResult doInit( ITsContextRo aEnviron ) {
    logger().info( FMT_INFO_SKATLET_INIT, id(), connection() );
    return ValidationResult.SUCCESS;
  }

  // ------------------------------------------------------------------------------------
  // IWorkerComponent
  //
  @Override
  public void start() {
    logger().info( FMT_INFO_SKATLET_START, id() );
  }

  @Override
  public boolean queryStop() {
    logger().info( FMT_INFO_SKATLET_QUERY_STOP, id() );
    return true;
  }

  @Override
  public boolean isStopped() {
    return true;
  }

  @Override
  public void destroy() {
    logger().info( FMT_INFO_SKATLET_DESTROY, id() );
  }

  // ------------------------------------------------------------------------------------
  // ICooperativeMultiTaskable
  //
  @Override
  public void doJob() {
    logger().info( FMT_INFO_SKATLET_DOJOB, id() );
  }

}
