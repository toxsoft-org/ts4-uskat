package org.toxsoft.uskat.skadmin.cli;

import static org.toxsoft.uskat.skadmin.cli.IAdminAnsiConstants.*;

import java.io.File;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.opset.impl.OptionSet;
import org.toxsoft.core.tslib.av.opset.impl.OptionSetKeeper;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.coll.primtypes.IStringMapEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Цвета используемые консолью
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
public class AdminColors {

  /**
   * Цвет сообщения INFO
   */
  public static String COLOR_INFO = readEnvColor( "COLOR_INFO", "FORE_COLOR_GREEN" );

  /**
   * Цвет сообщения WARNING
   */
  public static String COLOR_WARN = readEnvColor( "COLOR_WARN", "FORE_COLOR_YELLOW" );

  /**
   * Цвет сообщения ERROR
   */
  public static String COLOR_ERROR = readEnvColor( "COLOR_ERROR", "FORE_COLOR_BRIGHT_RED" );

  /**
   * Цвет единичного параметра контекста
   */
  public static String COLOR_SINGLE_REF = readEnvColor( "COLOR_SINGLE_REF", "FORE_COLOR_BRIGHT_YELLOW" );

  /**
   * Цвет значения элемента списка параметров контекста
   */
  public static String COLOR_REF_LIST = readEnvColor( "COLOR_REF_LIST", "FORE_COLOR_BRIGHT_YELLOW" );

  /**
   * Цвет идентификатора
   */
  public static String COLOR_ID = readEnvColor( "COLOR_ID", "FORE_COLOR_BRIGHT_WHITE" );

  /**
   * Цвет единичного значения
   */
  public static String COLOR_SINGLE_VALUE = readEnvColor( "COLOR_SINGLE_VALUE", "FORE_COLOR_BRIGHT_CYAN" );

  /**
   * Цвет значения элемента списка
   */
  public static String COLOR_VALUE_LIST = readEnvColor( "COLOR_VALUE_LIST", "FORE_COLOR_CYAN" );

  /**
   * Идентификаторы цвета текста
   */
  public static IStringMap<String> foreColorIds = new StringMap<>();

  /**
   * Идентификаторы фона текста
   */
  public static IStringMap<String> backColorIds = new StringMap<>();

  /**
   * Инициализация карт цветов
   */
  static {
    if( foreColorIds == null || foreColorIds.size() == 0 ) {
      foreColorIds = initForeColorIds();
    }
    if( backColorIds == null || backColorIds.size() == 0 ) {
      backColorIds = initBackColorIdsIds();
    }
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Возвращает ansi-строку цвета для указанного идентификатора сущности
   *
   * @param aEntityID String идентификатор сущности
   * @param aDefaultValue String значение по умолчанию
   * @return String ansi-строка представляющая цвет сущности
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static String readEnvColor( String aEntityID, String aDefaultValue ) {
    TsNullArgumentRtException.checkNulls( aEntityID, aDefaultValue );
    if( foreColorIds == null ) {
      foreColorIds = initForeColorIds();
    }
    if( backColorIds == null ) {
      backColorIds = initBackColorIdsIds();
    }
    String WORK_DIR = "user.dir"; //$NON-NLS-1$
    String S5ADMIN_CFG_FILENAME = "s5admin.cfg"; //$NON-NLS-1$

    // Каталог запуска
    String workDir = System.getProperty( WORK_DIR );
    // Параметры загруженные из файла конфигурации. null: файл не существует
    IOptionSet configFileParams = null;
    String filename = workDir + File.separator + S5ADMIN_CFG_FILENAME;
    File file = new File( filename );
    configFileParams = (file.exists() ? OptionSetKeeper.KEEPER.read( file ) : new OptionSet());

    String colorID = System.getProperty( aEntityID );
    if( colorID == null && configFileParams.hasValue( aEntityID ) ) {
      colorID = configFileParams.getStr( aEntityID );
    }
    if( colorID == null ) {
      return foreColorIds.getByKey( aDefaultValue );
    }
    String retValue = foreColorIds.findByKey( colorID );
    if( retValue == null ) {
      retValue = backColorIds.findByKey( colorID );
    }
    if( retValue == null ) {
      return foreColorIds.getByKey( aDefaultValue );
    }
    return retValue;
  }

  /**
   * Формирует карту цветов текста
   *
   * @return {@link IStringMap} карта цветов текста
   */
  private static IStringMap<String> initForeColorIds() {
    IStringMapEdit<String> colors = new StringMap<>();
    colors.put( "FORE_COLOR_BLACK", CLR_FORE_BLACK );
    colors.put( "FORE_COLOR_RED", CLR_FORE_RED );
    colors.put( "FORE_COLOR_GREEN", CLR_FORE_GREEN );
    colors.put( "FORE_COLOR_YELLOW", CLR_FORE_YELLOW );
    colors.put( "FORE_COLOR_BLUE", CLR_FORE_BLUE );
    colors.put( "FORE_COLOR_MAGENTA", CLR_FORE_MAGENTA );
    colors.put( "FORE_COLOR_CYAN", CLR_FORE_CYAN );
    colors.put( "FORE_COLOR_WHITE", CLR_FORE_WHITE );
    colors.put( "FORE_COLOR_BRIGHT_BLACK", CLR_FORE_BRIGHT_BLACK );
    colors.put( "FORE_COLOR_BRIGHT_RED", CLR_FORE_BRIGHT_RED );
    colors.put( "FORE_COLOR_BRIGHT_GREEN", CLR_FORE_BRIGHT_GREEN );
    colors.put( "FORE_COLOR_BRIGHT_YELLOW", CLR_FORE_BRIGHT_YELLOW );
    colors.put( "FORE_COLOR_BRIGHT_BLUE", CLR_FORE_BRIGHT_BLUE );
    colors.put( "FORE_COLOR_BRIGHT_MAGENTA", CLR_FORE_BRIGHT_MAGENTA );
    colors.put( "FORE_COLOR_BRIGHT_CYAN", CLR_FORE_BRIGHT_CYAN );
    colors.put( "FORE_COLOR_BRIGHT_WHITE", CLR_FORE_BRIGHT_WHITE );
    colors.put( "FORE_COLOR_DEFAULT", COLOR_RESET );
    return colors;
  }

  /**
   * Формирует карту цветов фона
   *
   * @return {@link IStringMap} карта цветов фона
   */
  private static IStringMap<String> initBackColorIdsIds() {
    IStringMapEdit<String> colors = new StringMap<>();
    colors = new StringMap<>();
    colors.put( "BACK_COLOR_BLACK", CLR_BACK_BLACK );
    colors.put( "BACK_COLOR_RED", CLR_BACK_RED );
    colors.put( "BACK_COLOR_GREEN", CLR_BACK_GREEN );
    colors.put( "BACK_COLOR_YELLOW", CLR_BACK_YELLOW );
    colors.put( "BACK_COLOR_BLUE", CLR_BACK_BLUE );
    colors.put( "BACK_COLOR_MAGENTA", CLR_BACK_MAGENTA );
    colors.put( "BACK_COLOR_CYAN", CLR_BACK_CYAN );
    colors.put( "BACK_COLOR_WHITE", CLR_BACK_WHITE );
    colors.put( "BACK_COLOR_BRIGHT_BLACK", CLR_BACK_BRIGHT_BLACK );
    colors.put( "BACK_COLOR_BRIGHT_RED", CLR_BACK_BRIGHT_RED );
    colors.put( "BACK_COLOR_BRIGHT_GREEN", CLR_BACK_BRIGHT_GREEN );
    colors.put( "BACK_COLOR_BRIGHT_YELLOW", CLR_BACK_BRIGHT_YELLOW );
    colors.put( "BACK_COLOR_BRIGHT_BLUE", CLR_BACK_BRIGHT_BLUE );
    colors.put( "BACK_COLOR_BRIGHT_MAGENTA", CLR_BACK_BRIGHT_MAGENTA );
    colors.put( "BACK_COLOR_BRIGHT_CYAN", CLR_BACK_BRIGHT_CYAN );
    colors.put( "BACK_COLOR_BRIGHT_WHITE", CLR_BACK_BRIGHT_WHITE );
    colors.put( "BACK_COLOR_DEFAULT", COLOR_RESET );
    return colors;
  }
}
