package org.toxsoft.uskat.s5.server.logger;

import org.jboss.logging.*;
import org.jboss.logging.Logger.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;

/**
 * Wraps over {@link org.jboss.logging.Logger} to implementa tslib interface {@link ILogger}.
 *
 * @author mvk
 */
public class LoggerWrapper
    extends AbstractBasicLogger {

  private final Logger source;

  /**
   * Creates wrapper over the specified Log logger.
   *
   * @param aSource {@link org.jboss.logging.Logger} - wilfly logger
   * @return ILogger {@link ILogger} - created instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static ILogger getLogger( org.jboss.logging.Logger aSource ) {
    return new LoggerWrapper( aSource );
  }

  /**
   * Creates wrapper for the category specified by the name.
   *
   * @param aName String - the category name
   * @return ILogger {@link ILogger} - created instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static ILogger getLogger( String aName ) {
    return new LoggerWrapper( aName );
  }

  /**
   * Creates wrapper to log messages from the specified class.
   *
   * @param aClass Class - the class
   * @return ILogger {@link ILogger} - created instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static ILogger getLogger( Class<?> aClass ) {
    return new LoggerWrapper( aClass );
  }

  /**
   * Writes single validation check result to the logger.
   *
   * @param aLogger {@link ILogger} - the logger
   * @param aResult {@link ValidationResult} - the result to be written
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static void resultToLog( ILogger aLogger, ValidationResult aResult ) {
    TsNullArgumentRtException.checkNulls( aLogger, aResult );
    switch( aResult.type() ) {
      case OK:
        if( !aResult.message().equals( TsLibUtils.EMPTY_STRING ) ) {
          aLogger.info( aResult.message() );
        }
        break;
      case WARNING:
        aLogger.warning( aResult.message() );
        break;
      case ERROR:
        aLogger.error( aResult.message() );
        break;
      default:
        throw new TsNotAllEnumsUsedRtException();
    }
  }

  // ------------------------------------------------------------------------------------
  // Private constructors
  //

  private LoggerWrapper( org.jboss.logging.Logger aSource ) {
    source = TsNullArgumentRtException.checkNull( aSource );
  }

  private LoggerWrapper( String aName ) {
    source = Logger.getLogger( TsNullArgumentRtException.checkNull( aName ) );
  }

  private LoggerWrapper( Class<?> aClass ) {
    source = Logger.getLogger( TsNullArgumentRtException.checkNull( aClass ) );
  }

  // ------------------------------------------------------------------------------------
  // AbstractBasicLogger
  //

  @Override
  public boolean isSeverityOn( ELogSeverity aSeverity ) {
    TsNullArgumentRtException.checkNull( aSeverity );
    return source.isEnabled( ts2j( aSeverity ) );
  }

  // ------------------------------------------------------------------------------------
  // ILogger
  //

  @Override
  public void log( ELogSeverity aLogSeverity, String aMessage, Object... aArgs ) {
    Level level = ts2j( aLogSeverity );
    String msg = String.format( aMessage, aArgs );
    source.log( level, msg );
  }

  @Override
  public void log( ELogSeverity aLogSeverity, Throwable aException, String aMessage, Object... aArgs ) {
    Level level = ts2j( aLogSeverity );
    String msg = String.format( aMessage, aArgs );
    source.log( level, msg, aException );
  }

  @Override
  public void log( ELogSeverity aLogSeverity, Throwable aException ) {
    Level level = ts2j( aLogSeverity );
    source.log( level, TsLibUtils.EMPTY_STRING, aException );
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  private static Level ts2j( ELogSeverity aLogSeverity ) {
    return switch( aLogSeverity ) {
      case DEBUG -> Level.TRACE;
      case ERROR -> Level.ERROR;
      case WARNING -> Level.WARN;
      case INFO -> Level.INFO;
      default -> throw new TsNotAllEnumsUsedRtException();
    };

  }

  // ------------------------------------------------------------------------------------
  // Class API
  //
}
