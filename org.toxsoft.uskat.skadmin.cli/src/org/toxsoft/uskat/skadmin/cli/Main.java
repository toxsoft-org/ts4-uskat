package org.toxsoft.uskat.skadmin.cli;

import static org.toxsoft.uskat.skadmin.cli.IAdminResources.*;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;

/**
 * Класс запуска приложения: консоль администратора s5
 *
 * @author mvk
 */
public class Main {

  private static ILogger logger;

  /**
   * @param aArgs String[] параметры командной строки
   */
  public static void main( String[] aArgs ) {
    // Инициализация журналов компонентов
    LoggerUtils.setLoggerFactory( org.toxsoft.core.log4j.LoggerWrapper::getLogger );
    // Инициализация журнала компонента
    logger = LoggerWrapper.getLogger( Main.class );
    // Слежение за файлом конфигурации журнала log4j.xml
    // 2026-03-22 mvk wildfly 39
    // LoggerWrapper.setScanPropertiesTimeout( 10000 );
    // Формируем командую строку из аргументов среды
    StringBuilder sb = new StringBuilder();
    for( int index = 0, n = aArgs.length; index < n; index++ ) {
      sb.append( aArgs[index] );
      if( index < n - 1 ) {
        sb.append( CHAR_SPACE );
      }
    }
    // Запуск консоли
    AdminConsole console = new AdminConsole( sb.toString() );
    // Запуск основного цикла
    console.run();
    // Завершение работы
    logger.debug( MSG_CONSOLE_FINISHED );
    // Форсированное завершение (могут быть потоки ejb останавливающие выгрузку программы)
    Runtime.getRuntime().halt( 0 );
  }
}
