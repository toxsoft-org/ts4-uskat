package org.toxsoft.uskat.core.impl;

import static org.toxsoft.uskat.core.impl.ISkResources.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.devapi.*;

/**
 * Base skatlet implementation.
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
   * Skatlet's execution environment
   *
   * @return {@link ITsContextRo} - the execution environment
   */
  protected final ITsContextRo environ() {
    return environ;
  }

  /**
   * Возвращает значение параметра из информации о сервере.
   *
   * @param aParamId String идентификатор параметра.
   * @param aDefaultValue {@link IAtomicValue} значение по умолчанию
   * @return aParamValue {@link IAtomicValue} значение параметра
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  IAtomicValue getBackendInfoParam( String aParamId, IAtomicValue aDefaultValue ) {
    return ISkatlet.REF_SKATLET_SUPPORT.getRef( environ ).getBackendInfoParam( aParamId, aDefaultValue );
  }

  /**
   * Установить значение параметра в информации о сервере
   * <p>
   * Если значение параметра уже было в информации о сервере, то его значение переписывается.
   *
   * @param aParamId String идентификатор параметра.
   * @param aParamValue {@link IAtomicValue} значение параметра
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  protected final void setBackendInfoParam( String aParamId, IAtomicValue aParamValue ) {
    ISkatlet.REF_SKATLET_SUPPORT.getRef( environ ).setBackendInfoParam( aParamId, aParamValue );
  }

  /**
   * Skatlet's shared connection.
   *
   * @return {@link ISkConnection} connection.
   * @throws TsIllegalStateRtException scatlet is not initialized.
   */
  protected final ISkConnection getSharedConnection() {
    TsIllegalStateRtException.checkNull( environ );
    return ISkatlet.REF_SHARED_CONNECTION.getRef( environ );
  }

  /**
   * Create a new skatlet's connection.
   *
   * @param aName String connection name.
   * @param aArgs {@link ITsContextRo} connection config params.
   * @return {@link ISkConnection} connection.
   * @throws TsIllegalStateRtException scatlet is not initialized.
   */
  protected final ISkConnection createConnection( String aName, ITsContextRo aArgs ) {
    TsNullArgumentRtException.checkNulls( aName, aArgs );
    TsIllegalStateRtException.checkNull( environ );
    return ISkatlet.REF_SKATLET_SUPPORT.getRef( environ ).createConnection( aName, aArgs );
  }

  /**
   * Skatlet's logger.
   *
   * @return {@link ILogger} logger.
   */
  protected final ILogger logger() {
    if( environ == null ) {
      return LoggerUtils.defaultLogger();
    }
    return ISkatlet.REF_SKATLET_SUPPORT.getRef( environ ).logger();
  }

  // ------------------------------------------------------------------------------------
  // ISkatlet
  //
  /**
   * Initializes libraries, register types & service creators.
   *
   * @return {@link ValidationResult} - initialization success result
   */
  @Override
  public final ValidationResult initialize() {
    return doInitialize();
  }

  @Override
  public final ValidationResult setContext( ITsContextRo aEnviron ) {
    environ = aEnviron;
    return doSetContext( aEnviron );
  }

  /**
   * Initializes libraries, register types & service creators.
   *
   * @return {@link ValidationResult} - initialization success result
   */
  protected ValidationResult doInitialize() {
    logger().info( FMT_INFO_SKATLET_INITIALIZE, id() );
    return ValidationResult.SUCCESS;
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
  protected ValidationResult doSetContext( ITsContextRo aEnviron ) {
    logger().info( FMT_INFO_SKATLET_SET_CONTEXT, id(), getSharedConnection() );
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
    logger().debug( FMT_INFO_SKATLET_DOJOB, id() );
  }

}
