package org.toxsoft.uskat.core.impl;

import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * An {@link ILogger} wrapper over {@link ILogger} with log severity control.
 * <p>
 * Initial logged messages severity is determined by {@link ISkCoreConfigConstants#OPDEF_DEF_CORE_LOG_SEVERITY}
 * option in the constructor. Each instance may change it's severity by {@link #setMinLogSeverity(ELogSeverity)}.
 *
 * @author hazard157
 */
public class CoreLogger
    extends AbstractBasicLogger {

  private final ILogger wrappedLogger;

  private ELogSeverity minSeverity;

  /**
   * Constructor.
   *
   * @param aWrappedLogger {@link ILogger} - logger to be wrapped
   * @param aConnArgs {@link ITsContextRo} - {@link ISkConnection#open(ITsContextRo)} arguments
   */
  public CoreLogger( ILogger aWrappedLogger, ITsContextRo aConnArgs ) {
    TsNullArgumentRtException.checkNulls( aWrappedLogger, aConnArgs );
    wrappedLogger = aWrappedLogger;
    minSeverity = ISkCoreConfigConstants.OPDEF_DEF_CORE_LOG_SEVERITY.getValue( aConnArgs.params() ).asValobj();
  }

  // ------------------------------------------------------------------------------------
  // AbstractBasicLogger
  //

  @Override
  public boolean isSeverityOn( ELogSeverity aSeverity ) {
    if( wrappedLogger.isSeverityOn( aSeverity ) ) {
      return aSeverity.compareTo( minSeverity ) >= 0;
    }
    return false;
  }

  @Override
  public void log( ELogSeverity aLogSeverity, String aMessage, Object... aArgs ) {
    if( isSeverityOn( aLogSeverity ) ) {
      wrappedLogger.log( aLogSeverity, aMessage, aArgs );
    }
  }

  @Override
  public void log( ELogSeverity aLogSeverity, Throwable aException, String aMessage, Object... aArgs ) {
    if( isSeverityOn( aLogSeverity ) ) {
      wrappedLogger.log( aLogSeverity, aException, aMessage, aArgs );
    }
  }

  @Override
  public void log( ELogSeverity aLogSeverity, Throwable aException ) {
    if( isSeverityOn( aLogSeverity ) ) {
      wrappedLogger.log( aLogSeverity, aException );
    }
  }

  /**
   * Sets minimal logger serverity.
   *
   * @param aSeverity {@link ELogSeverity} - minimal logger serverity
   */
  public void setMinLogSeverity( ELogSeverity aSeverity ) {
    TsNullArgumentRtException.checkNull( aSeverity );
    minSeverity = aSeverity;
  }

}
