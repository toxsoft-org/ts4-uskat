package org.toxsoft.uskat.core.logger;

import java.util.*;

import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;

/**
 * Вспомогательные методы доступа к журналам комнентов системы.
 *
 * @author mvk
 */
public final class LoggerUtils {

  /**
   * Идентификатор журнала по умолчанию.
   */
  private static final String DEFAULT_LOGGER_ID = "org.toxsoft"; //$NON-NLS-1$

  /**
   * Фабрика журналов (одна на JVM).
   */
  private static ILoggerFactory factory = aName -> org.toxsoft.core.tslib.utils.logs.impl.LoggerUtils2.stdoutLogger();

  /**
   * Журнал по умолчанию.
   */
  private static ILogger defaultLogger = getLogger( DEFAULT_LOGGER_ID );

  // ------------------------------------------------------------------------------------
  // public API
  //

  /**
   * Установить фабрику журналов компонентов системы.
   *
   * @param aLoggerFactory {@link ILoggerFactory} фабрика журналов компонетов системы.
   * @return {@link ILoggerFactory} фабрика которая была установлена до вызова
   *         {@link #setLoggerFactory(ILoggerFactory)}.
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static ILoggerFactory setLoggerFactory( ILoggerFactory aLoggerFactory ) {
    TsNullArgumentRtException.checkNull( aLoggerFactory );
    ILoggerFactory retValue = factory;
    factory = aLoggerFactory;
    defaultLogger = getLogger( DEFAULT_LOGGER_ID );
    return retValue;
  }

  /**
   * Возвращает журнал компонента.
   *
   * @param aName String имя компонента
   * @return {@link ILogger} журнал
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static ILogger getLogger( String aName ) {
    TsNullArgumentRtException.checkNull( aName );
    return factory.getLogger( aName );
  }

  /**
   * Возвращает журнал компонента.
   *
   * @param aClass Class<?> класс компонента
   * @return {@link ILogger} журнал
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static ILogger getLogger( Class<?> aClass ) {
    TsNullArgumentRtException.checkNull( aClass );
    return factory.getLogger( aClass.getName() );
  }

  /**
   * Определяет, физически логируются ли сообщаения запрошенной важности.
   * <p>
   * Несмотря на то, что любой метод вывода сообщений (как то log(), warn() error() и т.п.) можно вызвать в любой
   * момент, физически не все сообщения могут выводится в лог. Например, когда {@link ILogger} является оболочкой над
   * Log4J, то в текстовый файл лога попадают те сообщения, которые разрешены в xml-файле конфигурации.
   * <p>
   * Данный метод позволяет заранее узнать, будет ли логированое сообщение запрашиваемой важности, и если нет, то не
   * формировать сообщение. Ведь зачастую подготовка удобочитаемого сообщения само может оказаться достаточно
   * ресурсоемким действием.
   *
   * @param aSeverity ELogSeverity - важностть сообщения
   * @return boolean - признак, что сообщен я указанной важности физически выводятся в лог
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static boolean isSeverityOn( ELogSeverity aSeverity ) {
    return defaultLogger.isSeverityOn( aSeverity );
  }

  /**
   * Выводит форматированное текстовое сообщение в лог.
   * <p>
   * Форматирование происходит согласно правилам класса {@link Formatter}.
   *
   * @param aLogSeverity ELogSeverity - важностть сообщения
   * @param aMessage String - текст сообщения
   * @param aArgs Object[] - аргументы
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static void log( ELogSeverity aLogSeverity, String aMessage, Object... aArgs ) {
    defaultLogger.log( aLogSeverity, aMessage, aArgs );
  }

  /**
   * Выводит форматированное текстовое сообщение в лог вместе с информацией об исключении.
   * <p>
   * Форматирование происходит согласно правилам класса {@link Formatter}.
   *
   * @param aLogSeverity ELogSeverity - важностть сообщения
   * @param aException Throwable - исключение, вызвавшее сообщение в лог
   * @param aMessage String - текст сообщения
   * @param aArgs Object[] - аргументы
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static void log( ELogSeverity aLogSeverity, Throwable aException, String aMessage, Object... aArgs ) {
    defaultLogger.log( aLogSeverity, aException, aMessage, aArgs );
  }

  /**
   * Выводит исключение в лог с заданной важностью.
   *
   * @param aLogSeverity ELogSeverity - важностть сообщения
   * @param aException Throwable - исключение, вызвавшее сообщение в лог
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static void log( ELogSeverity aLogSeverity, Throwable aException ) {
    defaultLogger.log( aLogSeverity, aException );
  }

  /**
   * Выводит форматированное текстовое сообщение с важностью {@link ELogSeverity#INFO} в лог.
   * <p>
   * Форматирование происходит согласно правилам класса {@link Formatter}.
   *
   * @param aMessage String - текст сообщения
   * @param aArgs Object[] - аргументы
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static void info( String aMessage, Object... aArgs ) {
    defaultLogger.info( aMessage, aArgs );
  }

  /**
   * Выводит форматированное текстовое сообщение с важностью {@link ELogSeverity#WARNING} в лог.
   * <p>
   * Форматирование происходит согласно правилам класса {@link Formatter}.
   *
   * @param aMessage String - текст сообщения
   * @param aArgs Object[] - аргументы
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static void warning( String aMessage, Object... aArgs ) {
    defaultLogger.warning( aMessage, aArgs );
  }

  /**
   * Выводит форматированное текстовое сообщение с важностью {@link ELogSeverity#WARNING} и информацией об исключении в
   * лог.
   * <p>
   * Форматирование происходит согласно правилам класса {@link Formatter}.
   *
   * @param aException Throwable - исключение, вызвавшее сообщение в лог
   * @param aMessage String - текст сообщения
   * @param aArgs Object[] - аргументы
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static void warning( Throwable aException, String aMessage, Object... aArgs ) {
    defaultLogger.warning( aException, aMessage, aArgs );
  }

  /**
   * Выводит форматированное текстовое сообщение с важностью {@link ELogSeverity#ERROR} в лог.
   * <p>
   * Форматирование происходит согласно правилам класса {@link Formatter}.
   *
   * @param aMessage String - текст сообщения
   * @param aArgs Object[] - аргументы
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static void error( String aMessage, Object... aArgs ) {
    defaultLogger.error( aMessage, aArgs );
  }

  /**
   * Выводит форматированное текстовое сообщение с важностью {@link ELogSeverity#ERROR} и информацией об исключении в
   * лог.
   * <p>
   * Форматирование происходит согласно правилам класса {@link Formatter}.
   *
   * @param aException Throwable - исключение, вызвавшее сообщение в лог
   * @param aMessage String - текст сообщения
   * @param aArgs Object[] - аргументы
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static void error( Throwable aException, String aMessage, Object... aArgs ) {
    defaultLogger.error( aException, aMessage, aArgs );
  }

  /**
   * Выводит искючение с важностью {@link ELogSeverity#ERROR}.
   *
   * @param aException Throwable - исключение, вызвавшее сообщение в лог
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static void error( Throwable aException ) {
    defaultLogger.error( aException );
  }

  /**
   * Выводит форматированное текстовое сообщение с важностью {@link ELogSeverity#DEBUG} в лог.
   * <p>
   * Форматирование происходит согласно правилам класса {@link Formatter}.
   *
   * @param aMessage String - текст сообщения
   * @param aArgs Object[] - аргументы
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static void debug( String aMessage, Object... aArgs ) {
    defaultLogger.debug( aMessage, aArgs );
  }

  /**
   * Выводит форматированное текстовое сообщение с важностью {@link ELogSeverity#DEBUG} и информацией об исключении в
   * лог.
   * <p>
   * Форматирование происходит согласно правилам класса {@link Formatter}.
   *
   * @param aException Throwable - исключение, вызвавшее сообщение в лог
   * @param aMessage String - текст сообщения
   * @param aArgs Object[] - аргументы
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static void debug( Throwable aException, String aMessage, Object... aArgs ) {
    defaultLogger.debug( aException, aMessage, aArgs );
  }

  // ------------------------------------------------------------------------------------
  // private methods
  //

  private LoggerUtils() {
  }
}
