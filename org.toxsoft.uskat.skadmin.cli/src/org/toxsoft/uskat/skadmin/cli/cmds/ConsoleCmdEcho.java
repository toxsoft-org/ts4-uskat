package org.toxsoft.uskat.skadmin.cli.cmds;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.uskat.legacy.plexy.impl.PlexyValueUtils.*;
import static org.toxsoft.uskat.skadmin.cli.AdminColors.*;
import static org.toxsoft.uskat.skadmin.cli.IAdminAnsiConstants.*;
import static org.toxsoft.uskat.skadmin.cli.cmds.IAdminResources.*;
import static org.toxsoft.uskat.skadmin.core.plugins.AdminPluginUtils.*;

import org.toxsoft.core.tslib.bricks.strio.IStrioHardConstants;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.legacy.plexy.IPlexyType;
import org.toxsoft.uskat.legacy.plexy.IPlexyValue;
import org.toxsoft.uskat.skadmin.cli.IAdminConsole;
import org.toxsoft.uskat.skadmin.core.IAdminCmdCallback;

/**
 * Команда консоли: 'Эхо'
 *
 * @author mvk
 */
public class ConsoleCmdEcho
    extends AbstractConsoleCmd {

  /**
   * Конструктор
   *
   * @param aConsole {@link IAdminConsole} консоль
   */
  public ConsoleCmdEcho( IAdminConsole aConsole ) {
    super( aConsole );
    // Выводимый текст
    addArg( ECHO_ARG_TEXT_ID, ECHO_ARG_TEXT_ALIAS, ECHO_ARG_TEXT_NAME, PT_LIST_STRING, ECHO_ARG_TEXT_DESCR );
    // Не выводить текст на экран
    addArg( ECHO_ARG_SILENT_ID, ECHO_ARG_SILENT_ALIAS, ECHO_ARG_SILENT_NAME,
        createType( BOOLEAN, ECHO_ARG_SILENT_DEFAULT ), ECHO_ARG_SILENT_DESCR );
    // Перевод строки
    addArg( ECHO_ARG_EOL_ID, ECHO_ARG_EOL_ALIAS, ECHO_ARG_EOL_NAME, createType( BOOLEAN, ECHO_ARG_EOL_DEFAULT ),
        ECHO_ARG_EOL_DESCR );
    // Дополнение пробелами до правой границы экрана
    addArg( ECHO_ARG_SPACE_TRAIL_ID, ECHO_ARG_SPACE_TRAIL_ALIAS, ECHO_ARG_SPACE_TRAIL_NAME,
        createType( BOOLEAN, ECHO_ARG_SPACE_TRAIL_DEFAULT ), ECHO_ARG_SPACE_TRAIL_DESCR );
  }

  // ------------------------------------------------------------------------------------
  // Реализация абстрактных методов AbstractAdminCmd
  //
  @Override
  public String id() {
    return ECHO_CMD_ID;
  }

  @Override
  public String alias() {
    return ECHO_CMD_ALIAS;
  }

  @Override
  public String nmName() {
    return ECHO_CMD_NAME;
  }

  @Override
  public String description() {
    return ECHO_CMD_DESCR;
  }

  @Override
  public IPlexyType resultType() {
    return PT_SINGLE_STRING;
  }

  @Override
  public String resultDescription() {
    return ECHO_RESULT_DESCR;
  }

  @Override
  public IStringList roles() {
    return IStringList.EMPTY;
  }

  @Override
  protected void doExec( IStringMap<IPlexyValue> aArgValues, IAdminCmdCallback aCallback ) {
    // Консоль
    IAdminConsole console = getConsole();
    // Ширина экрана
    int consoleWidth = console.getWidth();
    // Аргументы
    IStringList textList = argStrList( ECHO_ARG_TEXT_ID );
    boolean eol = argSingleValue( ECHO_ARG_EOL_ID ).asBool();
    boolean silent = argSingleValue( ECHO_ARG_SILENT_ID ).asBool();
    boolean spaceTrail = argSingleValue( ECHO_ARG_SPACE_TRAIL_ID ).asBool();
    // Формируем текст результат
    StringBuilder sbText = new StringBuilder();
    for( String item : textList ) {
      sbText.append( item );
    }
    String text = sbText.toString();
    // Текст в верхнем регистре
    String upperText = text.toUpperCase();
    // Разбиваем текст на лексемы
    StringBuilder sb = new StringBuilder();
    // Минимальное количество символов для цветового переключения
    int colorMinCount = Math.max( FORE_COLOR_ID.length(), BACK_COLOR_ID.length() ) + 2;
    // Количество символов в тексте без управляющих последовательностей
    int textLength = 0;
    // Признак необходимости сборосить escape-последовательности цвета
    boolean needColorReset = false;
    for( int index = 0, n = text.length(); index < n; index++ ) {
      char c = text.charAt( index );
      if( c != CHAR_AT || index >= n - colorMinCount ) {
        // Не символ переключения или строка заканчивается. Цветовых переключателей уже быть не может
        sb.append( c );
        textLength++;
        continue;
      }
      // Оставшийся текст
      String leaveText = upperText.substring( index + 1 );
      // Переключатель цвета
      String colorName = findColorName( foreColorIds, leaveText );
      if( colorName != null ) {
        sb.append( foreColorIds.getByKey( colorName ) );
        index += colorName.length();
        needColorReset = true;
        continue;
      }
      colorName = findColorName( backColorIds, leaveText );
      if( colorName != null ) {
        sb.append( backColorIds.getByKey( colorName ) );
        index += colorName.length();
        needColorReset = true;
        continue;
      }
      // Анализ не выявил цветового переключения. Просто добавляем
      sb.append( c );
      textLength++;
    }
    if( spaceTrail ) {
      // Добавление пробелов до конца строки
      for( int index = textLength; index < consoleWidth - 1; index++ ) {
        sb.append( IStrioHardConstants.CHAR_SPACE );
      }
    }
    if( needColorReset ) {
      // Восстанавливаем цвет текста и фона
      sb.append( COLOR_RESET );
    }
    if( eol ) {
      // Добавление конца строки
      sb.append( IStrioHardConstants.CHAR_EOL );
    }
    // Вывод текста на консоль c 0-позиции и без автопереноса
    String value = sb.toString();
    if( !silent ) {
      System.out.println( value );
    }
    resultOk( pvsStr( value ) );
  }

  @Override
  protected IList<IPlexyValue> doPossibleValues( String aArgId, IStringMap<IPlexyValue> aArgValues ) {
    return IList.EMPTY;
  }

  // ------------------------------------------------------------------------------------
  // Внутреннее API
  //
  /**
   * Пытается найти в указанной карте имя цвета с которого начинается указанный текст.
   *
   * @param aColorNames {@link IStringMap} карта имен цветов
   * @param aToken String лексема в которой производится поиск
   * @return String escape-текст цветового переключения. null входные лексемы не являются цветовым переключением
   * @throws TsNullArgumentRtException аргумент = null
   */
  private static String findColorName( IStringMap<String> aColorNames, String aToken ) {
    TsNullArgumentRtException.checkNulls( aColorNames, aToken );
    for( String colorId : aColorNames.keys() ) {
      if( aToken.startsWith( colorId ) ) {
        return colorId;
      }
    }
    return null;
  }

}
