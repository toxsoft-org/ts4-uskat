package org.toxsoft.uskat.skadmin.cli;

import static org.toxsoft.uskat.skadmin.cli.AdminColors.*;
import static org.toxsoft.uskat.skadmin.cli.IAdminAnsiConstants.*;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.legacy.plexy.*;
import org.toxsoft.uskat.skadmin.core.*;

/**
 * Обратный вызов исполняемых команд
 *
 * @author mvk
 */
class AdminCmdCallback
    implements IAdminCmdCallback {

  private final IAdminConsole  console;
  private boolean              cancel;
  private static final ILogger logger = LoggerWrapper.getLogger( AdminConsole.class );

  /**
   * Конструктор
   *
   * @param aConsole {@link IAdminConsole} - консоль в которой выполняется консоль
   * @throws TsNullArgumentRtException аргумент = null
   */
  AdminCmdCallback( IAdminConsole aConsole ) {
    TsNullArgumentRtException.checkNulls( aConsole );
    console = aConsole;
  }

  // ------------------------------------------------------------------------------------
  // Отменить выполнение команды
  // TODO: будет иметь смысл когда консоль будет позволять выполнять команды в отдельных потоках
  //
  public void cancel() {
    cancel = true;
  }

  // ------------------------------------------------------------------------------------
  // Реализация IAdminCmdCallback
  //
  @Override
  public IPlexyValue getValue( IPlexyType aType, IList<IPlexyValue> aPossibleValues,
      IList<ValidationResult> aMessages ) {
    TsNullArgumentRtException.checkNulls( aType, aPossibleValues, aMessages );
    // Выводим сообщения о состоянии выполнения команды. Кроме последнего.
    printMessages( console, aMessages, aMessages.size() - 1 );
    // Последнее используется для вопроса
    String message = TsLibUtils.EMPTY_STRING;
    String colorScheme = COLOR_RESET;
    if( aMessages.size() > 0 ) {
      ValidationResult vr = aMessages.get( aMessages.size() - 1 );
      message = vr.message();
      colorScheme = getColorScheme( vr.type() );
    }
    // Определяем значение по умолчанию
    String defaultValue = TsLibUtils.EMPTY_STRING;
    IOptionSet constraints = aType.dataType().params();
    if( constraints.hasValue( IAvMetaConstants.TSID_DEFAULT_VALUE ) ) {
      IAtomicValue datavalue = constraints.getValue( IAvMetaConstants.TSID_DEFAULT_VALUE );
      defaultValue = datavalue.asString();
    }
    // Считываем значение из терминала
    return console.readValue( aType, colorScheme + message + COLOR_RESET, aPossibleValues, defaultValue, true );
  }

  @Override
  public boolean beforeStart( IList<ValidationResult> aMessages, long aStepsCount, boolean aStartDefault ) {
    TsNullArgumentRtException.checkNull( aMessages );
    // Выводим сообщения о состоянии выполнения команды. Кроме последнего.
    printMessages( console, aMessages, aMessages.size() - 1 );
    // Последнее используется для вопроса
    String message = TsLibUtils.EMPTY_STRING;
    String colorScheme = COLOR_RESET;
    if( aMessages.size() > 0 ) {
      ValidationResult vr = aMessages.get( aMessages.size() - 1 );
      message = vr.message();
      colorScheme = getColorScheme( vr.type() );
    }
    return console.queryConfirm( colorScheme + message + COLOR_RESET, aStartDefault );
  }

  @Override
  public boolean onNextStep( IList<ValidationResult> aMessages, long aCurrStep, long aStepsCount,
      boolean aCancelable ) {
    TsNullArgumentRtException.checkNull( aMessages );
    // Выводим сообщения о состоянии выполнения команды
    printMessages( console, aMessages, aMessages.size() );
    return !cancel;
  }

  @Override
  public void afterEnd( IAdminCmdResult aResults ) {
    // Вывод результата уже делается при синхроном завершении команды
    // TsNullArgumentRtException.checkNull( aResults );
    // printCmdResults( cmdDef, aResults );
  }

  // ------------------------------------------------------------------------------------
  // Внутреннее API
  //
  /**
   * Выводит на экран сообщения о состоянии выполнения команды
   *
   * @param aMessages {@link IList}&lt;{@link ValidationResult}&gt; - список сообщений
   * @param aCount количество выводимых сообщений
   */
  private static void printMessages( IAdminConsole aConsole, IList<ValidationResult> aMessages, int aCount ) {
    TsNullArgumentRtException.checkNulls( aMessages );
    for( int index = 0, n = aCount; index < n; index++ ) {
      ValidationResult item = aMessages.get( index );
      String colorScheme = getColorScheme( item.type() );
      System.out.print( colorScheme + item.message() + COLOR_RESET );
      logger.debug( item.message() );
    }
    aConsole.updatePrompt();
  }

  /**
   * Возвращает цветовую схему для типа сообщения о состоянии
   *
   * @param aType {@link EValidationResultType} тип сообщения
   * @return цветовая схема
   * @throws TsNullArgumentRtException аргумент = null
   */
  private static String getColorScheme( EValidationResultType aType ) {
    TsNullArgumentRtException.checkNulls( aType );
    return switch( aType ) {
      case OK -> COLOR_INFO;
      case WARNING -> COLOR_WARN;
      case ERROR -> COLOR_ERROR;
      default -> throw new TsNotAllEnumsUsedRtException();
    };
  }

}
